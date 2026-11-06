
import hashlib
import hmac
import logging
import os
import subprocess
from pathlib import Path

from dotenv import load_dotenv
from fastapi import BackgroundTasks, FastAPI, Header, HTTPException, Request

# --------------------------------------------------
# Configuration
# --------------------------------------------------

BASE_DIR = Path(__file__).resolve().parents[1]

ENV_PATH = Path(__file__).resolve().parent / ".env"
load_dotenv(ENV_PATH)

WEBHOOK_SECRET = os.getenv("GITHUB_WEBHOOK_SECRET")

REPO_PATH = BASE_DIR / "PizzaProject_Tashbaev_Kurbanov_Berdikozhoev"
SCRIPT_PATH = BASE_DIR / "update-arc42-docs.sh"

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
# Background documentation update
# --------------------------------------------------

def update_documentation():
    logger.info("Starting documentation update")
    logger.info("Repository: %s", REPO_PATH)
    logger.info("Script: %s", SCRIPT_PATH)

    try:
        result = subprocess.run(
            ["bash", str(SCRIPT_PATH)],
            cwd=REPO_PATH,
            capture_output=True,
            text=True,
            timeout=1800,
        )

        logger.info("Script return code: %s", result.returncode)

        if result.stdout:
            logger.info("Script stdout:\n%s", result.stdout)

        if result.stderr:
            logger.error("Script stderr:\n%s", result.stderr)

        if result.returncode != 0:
            logger.error("Documentation update failed")
            return

        logger.info("Documentation update completed successfully")

    except subprocess.TimeoutExpired:
        logger.exception("Documentation update timed out")

    except Exception:
        logger.exception("Unexpected error during documentation update")


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
        WEBHOOK_SECRET.encode(),
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
        return {"message": "Event ignored"}

    # Check paths
    if not REPO_PATH.is_dir():
        logger.error("Repository directory not found: %s", REPO_PATH)
        raise HTTPException(
            status_code=500,
            detail=f"Repository directory not found: {REPO_PATH}",
        )

    if not SCRIPT_PATH.is_file():
        logger.error("Script not found: %s", SCRIPT_PATH)
        raise HTTPException(
            status_code=500,
            detail=f"Script not found: {SCRIPT_PATH}",
        )

    # Schedule background task
    background_tasks.add_task(update_documentation)

    logger.info("GitHub push received. Documentation update scheduled.")

    return {
        "message": "Webhook received successfully",
        "status": "Documentation update scheduled",
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
    }
