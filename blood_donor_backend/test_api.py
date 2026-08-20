import requests
import time

BASE_URL = 'http://127.0.0.1:5000/api'

def test_endpoints():
    print("Testing backend endpoints...")
    
    # 1. Register User 1
    resp = requests.post(f"{BASE_URL}/auth/register", json={
        "name": "Yogen Patient",
        "phone_number": "1234567890",
        "password": "password123"
    })
    print("Register Patient:", resp.status_code, resp.json())
    
    # 2. Register User 2 (Donor)
    resp = requests.post(f"{BASE_URL}/auth/register", json={
        "name": "Alex Donor",
        "phone_number": "0987654321",
        "password": "password123"
    })
    print("Register Donor:", resp.status_code, resp.json())
    
    # 3. Login as Donor
    resp = requests.post(f"{BASE_URL}/auth/login", json={
        "phone_number": "0987654321",
        "password": "password123"
    })
    donor_token = resp.json()['token']
    donor_headers = {'Authorization': f'Bearer {donor_token}'}
    
    # 4. Create Donor Profile
    resp = requests.post(f"{BASE_URL}/donors/profile", json={
        "blood_group": "O+"
    }, headers=donor_headers)
    print("Create Profile:", resp.status_code, resp.json())
    
    # 5. Update Availability
    resp = requests.post(f"{BASE_URL}/donors/availability", json={
        "is_available_today": True,
        "latitude": 13.0827,
        "longitude": 80.2707 # Somewhere in Chennai
    }, headers=donor_headers)
    print("Update Availability:", resp.status_code, resp.json())
    
    # 6. Login as Patient
    resp = requests.post(f"{BASE_URL}/auth/login", json={
        "phone_number": "1234567890",
        "password": "password123"
    })
    patient_token = resp.json()['token']
    patient_headers = {'Authorization': f'Bearer {patient_token}'}
    
    # 7. Search Donors (Patient) - Same location to get high score
    resp = requests.post(f"{BASE_URL}/patients/search", json={
        "blood_group": "O+",
        "latitude": 13.0830,
        "longitude": 80.2710
    }, headers=patient_headers)
    print("Search Donors:", resp.status_code, resp.json())
    
    # 8. Trigger SOS (Patient)
    resp = requests.post(f"{BASE_URL}/patients/sos", json={
        "blood_group": "O+",
        "hospital_name": "Apollo Hospitals",
        "latitude": 13.0830,
        "longitude": 80.2710
    }, headers=patient_headers)
    print("Trigger SOS:", resp.status_code, resp.json())

    # 9. Record Donation (Donor)
    resp = requests.post(f"{BASE_URL}/donors/record-donation", json={}, headers=donor_headers)
    print("Record Donation:", resp.status_code, resp.json())

    # 10. Search again, should be blocked
    resp = requests.post(f"{BASE_URL}/patients/search", json={
        "blood_group": "O+",
        "latitude": 13.0830,
        "longitude": 80.2710
    }, headers=patient_headers)
    print("Search after donation (should be empty):", resp.status_code, resp.json())

if __name__ == '__main__':
    # Give server a second to start
    time.sleep(2)
    test_endpoints()
