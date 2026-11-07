
import hashlib
import hmac
import logging
import os
import subprocess
import threading
from datetime import datetime
from pathlib import Path

from dotenv import load_dotenv
from fastapi import (
    BackgroundTasks,
    FastAPI,
    Header,
    HTTPException,
    Request,
)

# --------------------------------------------------
# Configuration
# --------------------------------------------------

BASE_DIR = Path(__file__).resolve().parents[1]

ENV_PATH = Path(__file__).resolve().parent / ".env"
load_dotenv(ENV_PATH)

WEBHOOK_SECRET = os.getenv("GITHUB_WEBHOOK_SECRET")

REPO_PATH = BASE_DIR / "PizzaProject_Tashbaev_Kurbanov_Berdikozhoev"
SCRIPT_PATH = BASE_DIR / "update-arc42-docs.sh"

LOG_DIR = Path(__file__).resolve().parent / "logs"
LOG_DIR.mkdir(parents=True, exist_ok=True)

MAIN_BRANCH = "refs/heads/main"
SCRIPT_TIMEOUT = 1800

app = FastAPI(title="Documentation Automizer")

# --------------------------------------------------
# Logging
# --------------------------------------------------

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)

logger = logging.getLogger(__name__)

# --------------------------------------------------
# Concurrency protection
# --------------------------------------------------

update_lock = threading.Lock()

# --------------------------------------------------
# Background documentation update
# --------------------------------------------------

def update_documentation():
    timestamp = datetime.now().strftime("%Y-%m-%d_%H-%M-%S")
    log_file = LOG_DIR / f"documentation-{timestamp}.log"

    logger.info("Starting documentation update")
    logger.info("Repository: %s", REPO_PATH)
    logger.info("Script: %s", SCRIPT_PATH)
    logger.info("Output log: %s", log_file)

    try:
        with log_file.open(
            "w",
            encoding="utf-8",
            buffering=1,
        ) as output:

            output.write(
                f"Documentation update started: {datetime.now().isoformat()}\n"
            )
            output.flush()

            result = subprocess.run(
                ["bash", str(SCRIPT_PATH)],
                cwd=REPO_PATH,
                stdout=output,
                stderr=subprocess.STDOUT,
                text=True,
                timeout=SCRIPT_TIMEOUT,
            )

            output.write(
                f"\nScript exit code: {result.returncode}\n"
            )
            output.flush()

        if result.returncode != 0:
            logger.error(
                "Documentation update failed "
                "(exit code %s). Log: %s",
                result.returncode,
                log_file,
            )
            return

        logger.info(
            "Documentation update completed successfully. Log: %s",
            log_file,
        )

    except subprocess.TimeoutExpired:
        logger.exception(
            "Documentation update timed out after %s seconds. Log: %s",
            SCRIPT_TIMEOUT,
            log_file,
        )

    except Exception:
        logger.exception(
            "Unexpected error during documentation update. Log: %s",
            log_file,
        )


def run_documentation_update():
    if not update_lock.acquire(blocking=False):
        logger.warning(
            "Documentation update already running. "
            "Skipping duplicate execution."
        )
        return

    try:
        update_documentation()
    finally:
        update_lock.release()


# --------------------------------------------------
# GitHub Webhook
# --------------------------------------------------

@app.post("/github-webhook")
async def github_webhook(
    request: Request,
    background_tasks: BackgroundTasks,
    x_hub_signature_256: str = Header(default=""),
    x_github_event: str = Header(default=""),
):
    # Check secret
    if not WEBHOOK_SECRET:
        logger.error("GITHUB_WEBHOOK_SECRET is not configured")
        raise HTTPException(
            status_code=500,
            detail="Webhook secret is not configured",
        )

    # Read request body
    body = await request.body()

    # Verify GitHub signature
    expected_signature = "sha256=" + hmac.new(
        WEBHOOK_SECRET.encode("utf-8"),
        body,
        hashlib.sha256,
    ).hexdigest()

    if not hmac.compare_digest(
        expected_signature,
        x_hub_signature_256,
    ):
        logger.warning("Invalid GitHub webhook signature")
        raise HTTPException(
            status_code=401,
            detail="Invalid signature",
        )

    # Process only push events
    if x_github_event != "push":
        logger.info("Ignoring GitHub event: %s", x_github_event)
        return {
            "message": "Event ignored",
            "event": x_github_event,
        }

    # Parse GitHub payload
    try:
        payload = await request.json()
    except ValueError:
        raise HTTPException(
            status_code=400,
            detail="Invalid JSON payload",
        )

    # Process only main branch
    ref = payload.get("ref", "")

    if ref != MAIN_BRANCH:
        logger.info("Ignoring push to branch: %s", ref)
        return {
            "message": "Push ignored",
            "reason": "Not the main branch",
            "ref": ref,
        }

    # Check paths
    if not REPO_PATH.is_dir():
        logger.error(
            "Repository directory not found: %s",
            REPO_PATH,
        )
        raise HTTPException(
            status_code=500,
            detail="Repository directory not found",
        )

    if not SCRIPT_PATH.is_file():
        logger.error(
            "Script not found: %s",
            SCRIPT_PATH,
        )
        raise HTTPException(
            status_code=500,
            detail="Documentation script not found",
        )

    # Schedule background task
    background_tasks.add_task(run_documentation_update)

    logger.info(
        "GitHub push received on main. "
        "Documentation update scheduled."
    )

    return {
        "message": "Webhook received successfully",
        "status": "Documentation update scheduled",
        "branch": "main",
    }


# --------------------------------------------------
# Health check
# --------------------------------------------------

@app.get("/health")
async def health_check():
    return {
        "status": "running",
        "repository_exists": REPO_PATH.is_dir(),
        "script_exists": SCRIPT_PATH.is_file(),
        "log_directory": str(LOG_DIR),
        "documentation_update_running": update_lock.locked(),
    }
