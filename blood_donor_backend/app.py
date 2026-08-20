from flask import Flask, request, jsonify
from config import Config
from extensions import db, migrate
import threading
import time
import os
from datetime import datetime, UTC

from email_service import send_generated_otp, verify_otp


def run_scheduler_loop(app):
    from models import Alert, DonorProfile, RequestStatusHistory

    last_reset_date = datetime.now(UTC).replace(tzinfo=None).date()

    print("[BACKGROUND SCHEDULER] Starting background scheduler loop...")

    while True:
        with app.app_context():
            now = datetime.now(UTC).replace(tzinfo=None)

            # 1. Daily Availability Reset
            current_date = now.date()

            if current_date > last_reset_date:
                print(
                    f"[BACKGROUND SCHEDULER] Resetting all donor "
                    f"availabilities for the new day: {current_date}"
                )

                try:
                    donors = DonorProfile.query.all()

                    for donor in donors:
                        donor.today_availability = False
                        donor.is_available_today = False
                        donor.availability_updated_at = now

                    db.session.commit()
                    last_reset_date = current_date

                except Exception as e:
                    print(
                        f"[BACKGROUND SCHEDULER] Daily reset failed: {e}"
                    )
                    db.session.rollback()

            # 2. Donor Auto Reactivation
            try:
                inactive_donors = DonorProfile.query.filter_by(
                    eligibility_status='INELIGIBLE'
                ).all()

                for donor in inactive_donors:
                    if donor.last_donation_date:
                        days_since = (
                            now - donor.last_donation_date
                        ).days

                        if days_since >= 90:
                            print(
                                f"[BACKGROUND SCHEDULER] "
                                f"Auto-reactivating Donor ID "
                                f"{donor.user_id} "
                                f"(completed 90 days)."
                            )

                            donor.eligibility_status = 'ELIGIBLE'

                db.session.commit()

            except Exception as e:
                print(
                    f"[BACKGROUND SCHEDULER] "
                    f"Donor auto-reactivation failed: {e}"
                )
                db.session.rollback()

            # 3. Request Expiry System
            try:
                active_alerts = Alert.query.filter(
                    ~Alert.status.in_([
                        'COMPLETED',
                        'CLOSED',
                        'CANCELLED',
                        'EXPIRED'
                    ])
                ).all()

                for alert in active_alerts:
                    if alert.expiry_time and now >= alert.expiry_time:

                        print(
                            f"[BACKGROUND SCHEDULER] "
                            f"Alert ID {alert.id} has expired. "
                            f"Updating status."
                        )

                        alert.status = 'EXPIRED'

                        db.session.add(
                            RequestStatusHistory(
                                alert_id=alert.id,
                                status='EXPIRED'
                            )
                        )

                        print(
                            f"[PUSH SIMULATION] Request Expired: "
                            f"Notifying Patient ID {alert.patient_id}."
                        )

                db.session.commit()

            except Exception as e:
                print(
                    f"[BACKGROUND SCHEDULER] "
                    f"Request expiry check failed: {e}"
                )
                db.session.rollback()

        # Check every 10 seconds
        time.sleep(10)


def create_app(config_class=Config):

    app = Flask(__name__)

    app.config.from_object(config_class)

    # CORS
    # CORS
    from flask_cors import CORS

    CORS(
        app,
        resources={r"/api/*": {"origins": [
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "http://localhost:5174",
            "http://127.0.0.1:5174"
        ]}}
    )

    # Database
    db.init_app(app)
    migrate.init_app(app, db)

    # Import models
    import models

    # Register blueprints
    from routes.auth_routes import auth_bp
    from routes.donor_routes import donor_bp
    from routes.patient_routes import patient_bp
    from routes.chat_routes import chat_bp

    app.register_blueprint(
        auth_bp,
        url_prefix='/api/auth'
    )

    app.register_blueprint(
        donor_bp,
        url_prefix='/api/donors'
    )

    app.register_blueprint(
        patient_bp,
        url_prefix='/api/patients'
    )

    app.register_blueprint(
        chat_bp,
        url_prefix='/api/chat'
    )

    # ==========================================================
    # SEND OTP API
    # ==========================================================

    @app.route('/api/auth/send-otp', methods=['POST'])
    def send_otp():

        data = request.get_json()

        if not data:
            return jsonify({
                "message": "Request body is required"
            }), 400

        email = data.get("email")
        name = data.get("name", "User")

        if not email:
            return jsonify({
                "message": "Email is required"
            }), 400

        try:
            success = send_generated_otp(
                email,
                name
            )

            if success:
                return jsonify({
                    "message": "OTP sent successfully"
                }), 200

            return jsonify({
                "message": "Failed to send OTP"
            }), 500

        except Exception as e:

            print(f"[OTP ERROR] {e}")

            return jsonify({
                "message": "Failed to send OTP",
                "error": str(e)
            }), 500

    # ==========================================================
    # VERIFY OTP API
    # ==========================================================

    @app.route('/api/auth/verify-otp', methods=['POST'])
    def verify_otp_api():

        data = request.get_json()

        if not data:
            return jsonify({
                "message": "Request body is required"
            }), 400

        email = data.get("email")
        entered_otp = data.get("otp")

        if not email or not entered_otp:
            return jsonify({
                "message": "Email and OTP are required"
            }), 400

        try:
            verified, reason = verify_otp(
                email,
                entered_otp
            )

            if verified:
                return jsonify({
                    "message": "OTP verified successfully",
                    "verified": True
                }), 200

            return jsonify({
                "message": reason,
                "verified": False
            }), 400

        except Exception as e:

            print(f"[OTP VERIFICATION ERROR] {e}")

            return jsonify({
                "message": "OTP verification failed",
                "error": str(e)
            }), 500

    # ==========================================================
    # ROOT API
    # ==========================================================

    @app.route('/')
    def index():

        return {
            "status": "ok",
            "message": "Smart Blood Donor Finder API is running"
        }

    # ==========================================================
    # BACKGROUND SCHEDULER
    # ==========================================================

    if (
        not app.debug
        or os.environ.get('WERKZEUG_RUN_MAIN') == 'true'
    ):
        threading.Thread(
            target=run_scheduler_loop,
            args=(app,),
            daemon=True
        ).start()

    return app


# ==============================================================
# RUN APPLICATION
# ==============================================================

if __name__ == '__main__':

    app = create_app()

    app.run(
        host='0.0.0.0',
        debug=True,
        port=5000
    )