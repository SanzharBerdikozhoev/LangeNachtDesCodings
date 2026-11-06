import hashlib
import hmac
import os
import subprocess
from pathlib import Path

from dotenv import load_dotenv
from fastapi import FastAPI, Header, HTTPException, Request

load_dotenv()

WEBHOOK_SECRET = os.getenv("GITHUB_WEBHOOK_SECRET")

app = FastAPI()

BASE_DIR = Path(__file__).resolve().parents[1]

REPO_PATH = BASE_DIR / "PizzaProject_Tashbaev_Kurbanov_Berdikozhoev"
SCRIPT_PATH = BASE_DIR / "update-arc42-docs.sh"


@app.post("/github-webhook")
async def github_webhook(
    request: Request,
    x_hub_signature_256: str = Header(default=""),
    x_github_event: str = Header(default=""),
):
    if not WEBHOOK_SECRET:
        raise HTTPException(
            status_code=500,
            detail="Webhook secret is not configured",
        )

    body = await request.body()

    expected_signature = "sha256=" + hmac.new(
        WEBHOOK_SECRET.encode(),
        body,
        hashlib.sha256,
    ).hexdigest()

    if not hmac.compare_digest(
        expected_signature,
        x_hub_signature_256,
    ):
        raise HTTPException(status_code=401, detail="Invalid signature")

    if x_github_event != "push":
        return {"message": "Event ignored"}

    if not SCRIPT_PATH.exists():
        raise HTTPException(
            status_code=500,
            detail=f"Script not found: {SCRIPT_PATH}",
        )

    import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

logger.info("BASE_DIR: %s", BASE_DIR)
logger.info("REPO_PATH: %s", REPO_PATH)
logger.info("SCRIPT_PATH: %s", SCRIPT_PATH)

if not REPO_PATH.is_dir():
    raise HTTPException(
        status_code=500,
        detail=f"Project directory not found: {REPO_PATH}"
    )

    try:
        result = subprocess.run(
            ["bash", str(SCRIPT_PATH)],
            cwd=REPO_PATH,
            capture_output=True,
            text=True,
            timeout=600,
        )

        logger.info("STDOUT:\n%s", result.stdout)
        logger.info("STDERR:\n%s", result.stderr)
        logger.info("RETURN CODE: %s", result.returncode)

        if result.returncode != 0:
            raise HTTPException(
                status_code=500,
                detail={
                    "error": "Script execution failed",
                    "stderr": result.stderr,
                    "stdout": result.stdout,
                    "returncode": result.returncode,
                },
            )

    except subprocess.TimeoutExpired:
        logger.exception("Script timeout")
        raise HTTPException(
            status_code=500,
            detail="Script execution timed out"
        )

    except OSError:
        logger.exception("Failed to start script")
        raise HTTPException(
            status_code=500,
            detail="Could not start documentation script"
        )

    return {
        "message": "Documentation update completed",
        "output": result.stdout,
    }
