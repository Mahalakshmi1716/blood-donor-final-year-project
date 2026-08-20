from flask_sqlalchemy import SQLAlchemy
from flask_migrate import Migrate
from datetime import datetime, UTC

db = SQLAlchemy()
migrate = Migrate()

def utcnow_naive():
    return datetime.now(UTC).replace(tzinfo=None)
