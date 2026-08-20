import requests
import random
import string
import time

BASE_URL = 'http://127.0.0.1:5000/api'

def generate_random_phone():
    return '9' + ''.join(random.choices(string.digits, k=9))

def test_auth():
    print("Testing Signup and Login Flow...")
    
    max_retries = 5
    for attempt in range(max_retries):
        test_phone = generate_random_phone()
        test_email = f"test_{test_phone}_{int(time.time())}@example.com"
        
        # 1. Signup / Register
        signup_payload = {
            "name": "Test User",
            "phone_number": test_phone,
            "password": "securepassword",
            "user_type": "Donor",
            "blood_group": "AB+",
            "age": 25,
            "gender": "Female",
            "email": test_email
        }
        print(f"Registering new user with phone: {test_phone}, email: {test_email}")
        resp = requests.post(f"{BASE_URL}/auth/register", json=signup_payload)
        
        if resp.status_code == 201:
            print("Signup Response Code:", resp.status_code)
            print("Signup Response Body:", resp.json())
            
            # OTP verification is required for Donors to activate the account
            print("\nVerifying Donor OTP...")
            otp_resp = requests.post(f"{BASE_URL}/auth/verify-otp", json={
                "phone_number": test_phone,
                "otp_code": "123456"
            })
            print("OTP Verification Response Code:", otp_resp.status_code)
            print("OTP Verification Response Body:", otp_resp.json())
            
            # 2. Login
            login_payload = {
                "phone_number": test_phone,
                "password": "securepassword"
            }
            print("\nLogging in with the registered credentials...")
            resp = requests.post(f"{BASE_URL}/auth/login", json=login_payload)
            print("Login Response Code:", resp.status_code)
            login_data = resp.json()
            print("Login Response Body:", login_data)
            
            # 3. Fetch user details (Verify /auth/me)
            if 'token' in login_data:
                token = login_data['token']
                headers = {'Authorization': f'Bearer {token}'}
                print("\nFetching user details using the login token...")
                resp = requests.get(f"{BASE_URL}/auth/me", headers=headers)
                print("Fetch User Details Response Code:", resp.status_code)
                print("Fetched User Details:", resp.json())
            else:
                print("Login failed, no token received.")
            break
        elif resp.status_code == 409:
            print(f"Collision detected (status 409). Retrying with a new identity...")
            continue
        else:
            print(f"Failed with status code {resp.status_code}: {resp.text}")
            break

if __name__ == '__main__':
    test_auth()

