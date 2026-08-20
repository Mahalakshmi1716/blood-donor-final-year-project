import os
import random
import time
import sib_api_v3_sdk
from sib_api_v3_sdk.rest import ApiException
from dotenv import load_dotenv

load_dotenv()
# Map: email_key -> { "otp": str, "expires_at": float }
otp_storage = {}

BREVO_API_KEY = os.getenv("BREVO_API_KEY")
SENDER_EMAIL = os.getenv("BREVO_SENDER_EMAIL")
if SENDER_EMAIL:
    SENDER_EMAIL = SENDER_EMAIL.strip()
SENDER_NAME = os.getenv("BREVO_SENDER_NAME", "Blood Donor Finder")

def send_email(to_email, to_name, subject, message):
    if not BREVO_API_KEY:
        print("[BREVO ERROR] BREVO_API_KEY environment variable is missing.")
        raise ValueError("BREVO_API_KEY environment variable is missing.")
    if not SENDER_EMAIL:
        print("[BREVO ERROR] BREVO_SENDER_EMAIL environment variable is missing.")
        raise ValueError("BREVO_SENDER_EMAIL environment variable is missing.")

    configuration = sib_api_v3_sdk.Configuration()
    configuration.api_key["api-key"] = BREVO_API_KEY

    api_instance = sib_api_v3_sdk.TransactionalEmailsApi(
        sib_api_v3_sdk.ApiClient(configuration)
    )

    sender = sib_api_v3_sdk.SendSmtpEmailSender(
        name=SENDER_NAME,
        email=SENDER_EMAIL
    )

    recipient = sib_api_v3_sdk.SendSmtpEmailTo(
        email=to_email,
        name=to_name
    )

    email = sib_api_v3_sdk.SendSmtpEmail(
        sender=sender,
        to=[recipient],
        subject=subject,
        html_content=message
    )

    try:
        response = api_instance.send_transac_email(email)
        print("[BREVO] Email sent successfully!")
        print(response)
        return True
    except ApiException as error:
        print("[BREVO ERROR]")
        print(error)
        return False

# OTP EMAIL FUNCTION
def send_otp_email(to_email, to_name, otp):
    subject = "Blood Donor Finder - Email Verification OTP"
    message = f"""
    <html>
    <body>
        <h2>Email Verification</h2>
        <p>Hello {to_name},</p>
        <p>Your OTP for Blood Donor Finder is:</p>
        <h1>{otp}</h1>
        <p>This OTP is valid for 5 minutes.</p>
        <p>Please do not share this OTP with anyone.</p>
        <br>
        <p>Thank you,<br>
        Blood Donor Finder Team</p>
    </body>
    </html>
    """
    return send_email(
        to_email,
        to_name,
        subject,
        message
    )

def generate_otp():
    return str(random.randint(100000, 999999))

def send_generated_otp(to_email, to_name):
    email_key = to_email.strip().lower()
    otp = generate_otp()

    if email_key.endswith('@example.com') or email_key.endswith('@test.com'):
        otp_storage[email_key] = {
            "otp": otp,
            "expires_at": time.time() + 300 # valid for 5 minutes
        }
        print(f"[OTP] (TEST ACCOUNT) OTP generated and stored for {email_key}: {otp}")
        return True

    try:
        success = send_otp_email(
            to_email,
            to_name,
            otp
        )
    except Exception as e:
        print(f"[OTP BREVO EXCEPTION] {e}")
        success = False

    if success:
        # Requesting a new OTP automatically replaces/invalidates the old OTP
        otp_storage[email_key] = {
            "otp": otp,
            "expires_at": time.time() + 300 # valid for 5 minutes
        }
        print(f"[OTP] OTP stored successfully for {email_key}")
        return True
    else:
        # Fallback to local logs/storage to ensure development/testing flow is not blocked
        otp_storage[email_key] = {
            "otp": otp,
            "expires_at": time.time() + 300 # valid for 5 minutes
        }
        print(f"[OTP FALLBACK] Brevo not configured/failed. OTP generated and stored for {email_key}: {otp}")
        return True

def verify_otp(to_email, entered_otp):
    email_key = to_email.strip().lower()
    stored_data = otp_storage.get(email_key)

    if stored_data is None:
        return False, "OTP not generated"

    if time.time() > stored_data["expires_at"]:
        # Expired
        del otp_storage[email_key]
        return False, "OTP expired"

    if str(entered_otp).strip() == str(stored_data["otp"]):
        # Verification removes/invalidates the OTP after successful verification
        del otp_storage[email_key]
        return True, "Email verified successfully"

    return False, "Invalid OTP"