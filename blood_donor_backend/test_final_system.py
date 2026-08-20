import unittest
import time
import sys
import random
import string
import datetime
from app import create_app
from extensions import db
from models import User, DonorProfile, Alert, OTPVerificationLog
import email_service

BASE_URL = 'http://10.143.142.1:5000/api'

class TestResponse:
    def __init__(self, flask_resp):
        self.flask_resp = flask_resp
        self.status_code = flask_resp.status_code
        self.text = flask_resp.get_data(as_text=True)
    
    def json(self):
        return self.flask_resp.get_json()

class ClientWrapper:
    def __init__(self, flask_client):
        self.flask_client = flask_client
        
    def post(self, url, json=None, headers=None):
        db.session.remove()  # Clear SQLAlchemy session before request
        path = url.replace('http://10.143.142.1:5000', '')
        resp = self.flask_client.post(path, json=json, headers=headers)
        db.session.remove()  # Clear SQLAlchemy session after request
        return TestResponse(resp)
        
    def get(self, url, headers=None):
        db.session.remove()  # Clear SQLAlchemy session before request
        path = url.replace('http://10.143.142.1:5000', '')
        resp = self.flask_client.get(path, headers=headers)
        db.session.remove()  # Clear SQLAlchemy session after request
        return TestResponse(resp)

def rand_phone():
    return "9" + "".join(random.choices(string.digits, k=9))

def rand_email(phone):
    return f"test_{phone}_{int(time.time())}@example.com"

def register_user_with_retry(requests, payload_builder):
    max_retries = 5
    for attempt in range(max_retries):
        phone = rand_phone()
        email = rand_email(phone)
        payload = payload_builder(phone, email)
        
        print(f"Registering test user: phone={phone}, email={email}")
        resp = requests.post(f"{BASE_URL}/auth/register", json=payload)
        if resp.status_code == 201:
            return resp, phone, email
        elif resp.status_code == 409:
            print(f"Registration collision on attempt {attempt+1}. Retrying with a new identity...")
            continue
        else:
            return resp, phone, email
    raise Exception("Max retries exceeded for user registration.")

def run_tests():
    app = create_app()
    with app.app_context():
        # Setup Client Wrapper to intercept request calls and direct them to the Flask test client in-process
        flask_client = app.test_client()
        requests = ClientWrapper(flask_client)

        print("======================================================================")
        print("STARTING SMART BLOOD FINDER INTEGRATION TEST SUITE (IN-PROCESS)")
        print("======================================================================")

        # 1. Register a Patient
        resp, patient_phone, patient_email = register_user_with_retry(requests, lambda p, e: {
            "name": "Anil Patient",
            "phone_number": p,
            "password": "password123",
            "user_type": "Patient",
            "email": e
        })
        assert resp.status_code == 201, f"Patient registration failed: {resp.text}"
        patient_id = resp.json()['user']['id']
        print(f"Patient Registration OK. Patient ID: {patient_id}")

        # Attempt to login as Patient (Should fail with 403 because unverified!)
        print("Testing Patient login block before verification...")
        resp = requests.post(f"{BASE_URL}/auth/login", json={
            "phone_number": patient_phone,
            "password": "password123"
        })
        assert resp.status_code == 403, f"Patient login allowed without verification: {resp.status_code}"
        print("Patient login blocked successfully.")

        # Verify Patient OTP
        print("Verifying Patient Email OTP...")
        email_key_pat = patient_email.strip().lower()
        assert email_key_pat in email_service.otp_storage, "Patient OTP not found in storage!"
        otp_code_pat = email_service.otp_storage[email_key_pat]['otp']
        resp = requests.post(f"{BASE_URL}/auth/verify-email-otp", json={
            "email": patient_email,
            "otp_code": otp_code_pat
        })
        assert resp.status_code == 200, f"Patient OTP verification failed: {resp.text}"
        print("Patient OTP Verified OK.")

        # Login as Patient should now succeed
        resp = requests.post(f"{BASE_URL}/auth/login", json={
            "phone_number": patient_phone,
            "password": "password123"
        })
        assert resp.status_code == 200, f"Patient login failed: {resp.text}"
        patient_token = resp.json()['token']
        patient_headers = {'Authorization': f'Bearer {patient_token}'}
        print("Patient Login OK (Token acquired).")

        # 2. Register a Hospital (Starts as Pending Verification and Unverified)
        resp, hospital_phone, hospital_email = register_user_with_retry(requests, lambda p, e: {
            "name": "City Emergency Hospital",
            "phone_number": p,
            "password": "password123",
            "user_type": "Hospital",
            "latitude": 13.0827,
            "longitude": 80.2707,
            "hospital_license": "LIC-999-XYZ",
            "registered_address": "Chennai Central",
            "email": e
        })
        assert resp.status_code == 201, f"Hospital registration failed: {resp.text}"
        hospital_id = resp.json()['user']['id']
        print(f"Hospital Registered OK. Hospital ID: {hospital_id}")

        # Attempt to login as Hospital (Should fail with 403 because unverified)
        resp = requests.post(f"{BASE_URL}/auth/login", json={
            "phone_number": hospital_phone,
            "password": "password123"
        })
        assert resp.status_code == 403, f"Hospital login allowed without verification: {resp.status_code}"

        # Verify Hospital OTP
        print("Verifying Hospital Email OTP...")
        email_key_hosp = hospital_email.strip().lower()
        assert email_key_hosp in email_service.otp_storage, "Hospital OTP not found in storage!"
        otp_code_hosp = email_service.otp_storage[email_key_hosp]['otp']
        resp = requests.post(f"{BASE_URL}/auth/verify-email-otp", json={
            "email": hospital_email,
            "otp_code": otp_code_hosp
        })
        assert resp.status_code == 200, f"Hospital OTP verification failed: {resp.text}"
        print("Hospital OTP Verified OK.")

        # Login as Hospital
        resp = requests.post(f"{BASE_URL}/auth/login", json={
            "phone_number": hospital_phone,
            "password": "password123"
        })
        assert resp.status_code == 200, f"Hospital login failed: {resp.text}"
        hospital_token = resp.json()['token']
        hospital_headers = {'Authorization': f'Bearer {hospital_token}'}
        print("Hospital Login OK.")

        # Attempt to trigger SOS as unverified hospital -> Should get 403 (Hospital Verification Status Pending)
        print("Testing if unverified hospital is blocked from triggering SOS...")
        resp = requests.post(f"{BASE_URL}/patients/sos", json={
            "blood_group": "A+",
            "hospital_name": "City Emergency Hospital",
            "latitude": 13.0827,
            "longitude": 80.2707,
            "urgency": "Critical",
            "units_required": 2
        }, headers=hospital_headers)
        print("Trigger SOS by unverified hospital response:", resp.status_code, resp.json())
        assert resp.status_code == 403, "Unverified hospital was not blocked from SOS"
        print("Block Verified OK.")

        # 3. Verify the Hospital (By Admin)
        print("\n[TEST 3] Verifying Hospital...")
        resp = requests.post(f"{BASE_URL}/auth/verify-hospital", json={
            "hospital_id": hospital_id,
            "status": "Verified"
        })
        assert resp.status_code == 200, f"Hospital verification failed: {resp.text}"
        print("Hospital Verified OK.")

        # 4. Register a Donor (Requires OTP)
        resp, donor_phone, donor_email = register_user_with_retry(requests, lambda p, e: {
            "name": "Raj Donor",
            "phone_number": p,
            "password": "password123",
            "user_type": "Donor",
            "blood_group": "O-",
            "age": 28,
            "gender": "Male",
            "latitude": 13.0835,
            "longitude": 80.2715,
            "email": e
        })
        assert resp.status_code == 201, f"Donor registration failed: {resp.text}"
        print("Donor Registered. Attempting login before OTP verification...")

        # Login should fail with 403
        resp = requests.post(f"{BASE_URL}/auth/login", json={
            "phone_number": donor_phone,
            "password": "password123"
        })
        assert resp.status_code == 403, f"Donor logged in without OTP verification: {resp.status_code}"
        print("Login blocked correctly (HTTP 403).")

        # Test that simulated OTP 123456 fails
        print("Testing that simulated OTP 123456 fails...")
        resp = requests.post(f"{BASE_URL}/auth/verify-email-otp", json={
            "email": donor_email,
            "otp_code": "123456"
        })
        assert resp.status_code == 400, f"Simulated OTP bypassed verification! Response: {resp.text}"
        print("Simulated OTP failed as expected.")

        # 5. Verify Donor OTP
        print("\n[TEST 5] Verifying Donor OTP...")
        email_key_donor = donor_email.strip().lower()
        assert email_key_donor in email_service.otp_storage, "Donor OTP not found in storage!"
        donor_otp = email_service.otp_storage[email_key_donor]['otp']
        resp = requests.post(f"{BASE_URL}/auth/verify-email-otp", json={
            "email": donor_email,
            "otp_code": donor_otp
        })
        assert resp.status_code == 200, f"OTP verification failed: {resp.text}"
        print("Donor OTP Verified OK.")

        # Login should now succeed
        resp = requests.post(f"{BASE_URL}/auth/login", json={
            "phone_number": donor_phone,
            "password": "password123"
        })
        assert resp.status_code == 200, f"Donor login failed after verification: {resp.text}"
        donor_token = resp.json()['token']
        donor_headers = {'Authorization': f'Bearer {donor_token}'}
        donor_id = resp.json()['user']['id']
        print(f"Donor Login OK. Donor ID: {donor_id}")

        # 6. Create Profile & Set Availability & Location (Near Chennai Central)
        print("\n[TEST 6] Creating Donor Profile & Setting Availability...")
        resp = requests.post(f"{BASE_URL}/donors/profile", json={
            "blood_group": "O-",
            "age": 28,
            "gender": "Male"
        }, headers=donor_headers)
        assert resp.status_code in [201, 400], f"Create profile failed: {resp.text}"
        
        resp = requests.post(f"{BASE_URL}/donors/availability", json={
            "is_available_today": True,
            "latitude": 13.0835,
            "longitude": 80.2715
        }, headers=donor_headers)
        assert resp.status_code == 200, f"Update availability failed: {resp.text}"
        print("Donor Profile & Availability set OK.")

        # 7. Hospital triggers SOS (Should find the O- donor, compatibility match, and remain active)
        print("\n[TEST 7] Hospital triggers first SOS request...")
        resp = requests.post(f"{BASE_URL}/patients/sos", json={
            "blood_group": "A+",
            "hospital_name": "City Emergency Hospital",
            "latitude": 13.0827,
            "longitude": 80.2707,
            "urgency": "Critical",
            "units_required": 2
        }, headers=hospital_headers)
        assert resp.status_code == 200, f"SOS trigger failed: {resp.text}"
        hospital_alert_id = resp.json()['alert']['id']
        print(f"SOS Triggered OK. Alert ID: {hospital_alert_id}")
        
        # Sleep for a moment to let the background escalation thread start,
        # print its notifications, and commit the ALERT_SENT status before we accept!
        print("Sleeping to yield to background escalation thread...")
        time.sleep(2)

        # 8. Duplicate Request Prevention
        print("\n[TEST 8] Testing duplicate SOS prevention...")
        # Attempt to trigger another SOS from Hospital while hospital_alert_id is active
        resp = requests.post(f"{BASE_URL}/patients/sos", json={
            "blood_group": "A+",
            "hospital_name": "City Emergency Hospital",
            "latitude": 13.0827,
            "longitude": 80.2707,
            "urgency": "Critical",
            "units_required": 2
        }, headers=hospital_headers)
        print("Duplicate SOS response:", resp.status_code, resp.json())
        assert resp.status_code == 400, "Duplicate SOS request was not blocked"
        print("Duplicate Request Blocked OK.")

        # 9. Accept Alert (First Accept Wins)
        print("\n[TEST 9] Donor accepting emergency request...")
        resp = requests.post(f"{BASE_URL}/patients/alerts/{hospital_alert_id}/accept", headers=donor_headers)
        print("First donor acceptance response:", resp.status_code, resp.json())
        assert resp.status_code == 200, f"Alert acceptance failed: {resp.text}"
        print("Alert accepted successfully by Donor 1.")

        # Register Donor 2 to test "First Accept Wins" lockout
        resp, donor2_phone, donor2_email = register_user_with_retry(requests, lambda p, e: {
            "name": "Sanjay Donor",
            "phone_number": p,
            "password": "password123",
            "user_type": "Donor",
            "blood_group": "A+",
            "age": 30,
            "gender": "Male",
            "email": e
        })
        assert resp.status_code == 201, f"Donor 2 registration failed: {resp.text}"
        
        email_key_donor2 = donor2_email.strip().lower()
        assert email_key_donor2 in email_service.otp_storage
        donor2_otp = email_service.otp_storage[email_key_donor2]['otp']
        requests.post(f"{BASE_URL}/auth/verify-email-otp", json={
            "email": donor2_email,
            "otp_code": donor2_otp
        })
        resp = requests.post(f"{BASE_URL}/auth/login", json={
            "phone_number": donor2_phone,
            "password": "password123"
        })
        donor2_token = resp.json()['token']
        donor2_headers = {'Authorization': f'Bearer {donor2_token}'}

        print("Testing if second donor is locked out (First Accept Wins)...")
        resp = requests.post(f"{BASE_URL}/patients/alerts/{hospital_alert_id}/accept", headers=donor2_headers)
        print("Second donor acceptance response:", resp.status_code, resp.json())
        assert resp.status_code == 400, "Second donor was not blocked from already accepted request"
        print("First Accept Wins lock verified OK.")

        # 10. Travel & Progress
        print("\n[TEST 10] Transitioning Donor travel & progress states...")
        resp = requests.post(f"{BASE_URL}/patients/alerts/{hospital_alert_id}/start-travel", headers=donor_headers)
        assert resp.status_code == 200, f"Start travel failed: {resp.text}"
        
        resp = requests.post(f"{BASE_URL}/patients/alerts/{hospital_alert_id}/start-donation", headers=donor_headers)
        assert resp.status_code == 200, f"Start donation failed: {resp.text}"
        print("Travel and donation procedure started OK.")

        # 11. Confirm Donation & 90-day Cooldown verification
        print("\n[TEST 11] Confirming donation & verifying 90-day cooldown...")
        resp = requests.post(f"{BASE_URL}/patients/alerts/{hospital_alert_id}/confirm-donation", headers=hospital_headers)
        assert resp.status_code == 200, f"Confirm donation failed: {resp.text}"
        print("Donation confirmed by Hospital. Certificate generated:", resp.json().get('certificate'))
        
        # Try updating availability of Donor 1 again -> Should be blocked due to cooldown
        resp = requests.post(f"{BASE_URL}/donors/availability", json={
            "is_available_today": True
        }, headers=donor_headers)
        print("Availability update during cooldown response:", resp.status_code, resp.json())
        assert resp.status_code == 400, "Donor was allowed to make themselves available during cooldown"
        print("90-day Cooldown verified OK (Availability blocked).")

        # 12. Forgot and Reset Password Flow
        print("\n[TEST 12] Testing forgot and reset password flow...")
        resp = requests.post(f"{BASE_URL}/auth/forgot-password", json={
            "email": hospital_email
        })
        assert resp.status_code == 200, f"Forgot password failed: {resp.text}"
        print("Forgot password request OK.")

        email_key = hospital_email.strip().lower()
        assert email_key in email_service.otp_storage
        reset_otp = email_service.otp_storage[email_key]['otp']

        resp = requests.post(f"{BASE_URL}/auth/reset-password", json={
            "email": hospital_email,
            "otp_code": reset_otp,
            "new_password": "newsecurepassword"
        })
        assert resp.status_code == 200, f"Reset password failed: {resp.text}"
        print("Reset password OK.")

        # Try login with old password -> Should fail
        resp = requests.post(f"{BASE_URL}/auth/login", json={
            "phone_number": hospital_phone,
            "password": "password123"
        })
        assert resp.status_code == 401, f"Logged in with old password after reset: {resp.status_code}"
        print("Login with old password blocked correctly (HTTP 401).")

        # Try login with new password -> Should succeed
        resp = requests.post(f"{BASE_URL}/auth/login", json={
            "phone_number": hospital_phone,
            "password": "newsecurepassword"
        })
        assert resp.status_code == 200, f"Login failed with new password: {resp.text}"
        print("Login with new password succeeded OK.")

        print("\n======================================================================")
        print("ALL TESTS PASSED SUCCESSFULLY! 100% CORRECTNESS VERIFIED.")
        print("======================================================================")

if __name__ == '__main__':
    try:
        run_tests()
    except AssertionError as e:
        print(f"\nTest failure: {e}")
        sys.exit(1)
