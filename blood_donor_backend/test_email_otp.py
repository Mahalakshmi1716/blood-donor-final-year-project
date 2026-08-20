import unittest
import time
from app import create_app
from extensions import db
from models import User
import email_service

class TestEmailOTPFlow(unittest.TestCase):
    def setUp(self):
        self.app = create_app()
        self.client = self.app.test_client()
        self.app_context = self.app.app_context()
        self.app_context.push()
        
        # Verify targeted user exists
        self.test_phone = '9080767918'
        self.test_email = 'mahathiru1713@gmail.com'
        self.user = User.query.filter_by(phone_number=self.test_phone).first()
        if not self.user:
            raise RuntimeError(f"Required user with phone {self.test_phone} not found in the DB. Please seed or register first.")
        
        # Reset verified_email to False before tests
        self.user.verified_email = False
        db.session.commit()

    def tearDown(self):
        db.session.rollback()
        self.app_context.pop()

    def test_1_user_lookup(self):
        print("\n--- TEST 1: Existing User Lookup ---")
        user = User.query.filter_by(phone_number=self.test_phone).first()
        self.assertIsNotNone(user)
        self.assertEqual(user.email, self.test_email)
        print(f"PASSED: User '{user.name}' found with email: {user.email}")

    def test_2_unknown_email(self):
        print("\n--- TEST 2: Request OTP for Unknown Email ---")
        resp = self.client.post('/api/auth/send-email-otp', json={
            "email": "nonexistent_email_12345@example.com"
        })
        self.assertEqual(resp.status_code, 404)
        self.assertIn("User with this email was not found", resp.get_json()['message'])
        print("PASSED: Unknown email request blocked with 404.")

    def test_3_request_otp_success(self):
        print("\n--- TEST 3: Request OTP and Confirm Send Result ---")
        # Clear storage
        email_service.otp_storage.clear()
        
        resp = self.client.post('/api/auth/send-email-otp', json={
            "email": self.test_email
        })
        self.assertEqual(resp.status_code, 200)
        self.assertIn("Email verification OTP sent successfully", resp.get_json()['message'])
        
        # Verify it exists in storage
        email_key = self.test_email.strip().lower()
        self.assertIn(email_key, email_service.otp_storage)
        otp_data = email_service.otp_storage[email_key]
        self.assertIsNotNone(otp_data['otp'])
        print(f"PASSED: OTP generated and stored successfully. Simulated Code: {otp_data['otp']}")

    def test_4_wrong_otp(self):
        print("\n--- TEST 4: Verification with Wrong OTP ---")
        # Ensure OTP exists
        self.client.post('/api/auth/send-email-otp', json={"email": self.test_email})
        
        resp = self.client.post('/api/auth/verify-email-otp', json={
            "email": self.test_email,
            "otp_code": "000000" # wrong OTP
        })
        self.assertEqual(resp.status_code, 400)
        self.assertIn("Invalid OTP", resp.get_json()['message'])
        print("PASSED: Wrong OTP rejected with 400.")

    def test_5_expired_otp(self):
        print("\n--- TEST 5: Verification with Expired OTP ---")
        # Trigger OTP
        self.client.post('/api/auth/send-email-otp', json={"email": self.test_email})
        
        # Manually expire in-memory storage
        email_key = self.test_email.strip().lower()
        email_service.otp_storage[email_key]['expires_at'] = time.time() - 1
        
        resp = self.client.post('/api/auth/verify-email-otp', json={
            "email": self.test_email,
            "otp_code": email_service.otp_storage[email_key]['otp']
        })
        self.assertEqual(resp.status_code, 400)
        self.assertIn("OTP expired", resp.get_json()['message'])
        print("PASSED: Expired OTP rejected with 400.")

    def test_6_second_otp_invalidates_first(self):
        print("\n--- TEST 6: Second OTP Request Invalidates First ---")
        # Request OTP 1
        self.client.post('/api/auth/send-email-otp', json={"email": self.test_email})
        email_key = self.test_email.strip().lower()
        otp_1 = email_service.otp_storage[email_key]['otp']
        
        # Request OTP 2
        self.client.post('/api/auth/send-email-otp', json={"email": self.test_email})
        otp_2 = email_service.otp_storage[email_key]['otp']
        
        self.assertNotEqual(otp_1, otp_2)
        
        # Verify first OTP fails
        resp = self.client.post('/api/auth/verify-email-otp', json={
            "email": self.test_email,
            "otp_code": otp_1
        })
        self.assertEqual(resp.status_code, 400)
        
        # Verify second OTP succeeds
        resp = self.client.post('/api/auth/verify-email-otp', json={
            "email": self.test_email,
            "otp_code": otp_2
        })
        self.assertEqual(resp.status_code, 200)
        print("PASSED: Requesting a second OTP successfully invalidated the first.")

    def test_7_successful_verification(self):
        print("\n--- TEST 7: Successful OTP Verification and DB State ---")
        # Trigger OTP
        self.client.post('/api/auth/send-email-otp', json={"email": self.test_email})
        email_key = self.test_email.strip().lower()
        otp_code = email_service.otp_storage[email_key]['otp']
        
        # Verify OTP
        resp = self.client.post('/api/auth/verify-email-otp', json={
            "email": self.test_email,
            "otp_code": otp_code
        })
        self.assertEqual(resp.status_code, 200)
        self.assertIn("Email verified successfully", resp.get_json()['message'])
        
        # Refresh database session and assert state is saved
        db.session.expire_all()
        user = User.query.filter_by(phone_number=self.test_phone).first()
        self.assertTrue(user.verified_email)
        print(f"PASSED: User verified_email updated to: {user.verified_email}")

if __name__ == '__main__':
    unittest.main()
