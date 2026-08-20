import datetime
from app import create_app
from extensions import db
from models import User, DonorProfile, DonationRecord

def seed_db():
    app = create_app()
    with app.app_context():
        # Drop all tables and recreate them to apply new schema
        db.drop_all()
        db.create_all()

        mock_users = [
            {"name": "palani", "email": "mahathiru1713@gmail.com", "phone": "9080767918", "type": "Donor", "blood": "A+", "lat": 28.6139, "lon": 77.2090, "age": 30, "gender": "Male"},
            {"name": "Priya Sharma", "email": "priya@example.com", "phone": "9876543210", "blood": "O+", "lat": 28.6239, "lon": 77.2190, "type": "Donor", "age": 28, "gender": "Female"},
            {"name": "Arjun Kumar", "email": "arjun@example.com", "phone": "9876543211", "blood": "A+", "lat": 28.6039, "lon": 77.1990, "type": "Donor", "age": 32, "gender": "Male"},
            {"name": "Sunita Devi", "email": "sunita@example.com", "phone": "9876543212", "blood": "B+", "lat": 28.6339, "lon": 77.2090, "type": "Donor", "age": 25, "gender": "Female"},
            {"name": "Rahul Verma", "email": "rahul@example.com", "phone": "9876543213", "blood": "O-", "lat": 28.6439, "lon": 77.2290, "type": "Donor", "age": 40, "gender": "Male"},
            {"name": "Aditya Singh", "email": "aditya@example.com", "phone": "9876543214", "blood": "AB+", "lat": 28.5939, "lon": 77.2190, "type": "Donor", "age": 35, "gender": "Male"},
            {"name": "Kiran Rao", "email": "kiran@example.com", "phone": "9876543215", "blood": "A-", "lat": 28.6139, "lon": 77.2290, "type": "Donor", "age": 29, "gender": "Female"},
            {"name": "Meera Patel", "email": "meera@example.com", "phone": "9876543216", "blood": "B-", "lat": 28.6539, "lon": 77.2090, "type": "Donor", "age": 24, "gender": "Female"},
            {"name": "Vikram Seth", "email": "vikram@example.com", "phone": "9876543217", "blood": "O+", "lat": 28.6200, "lon": 77.2000, "type": "Donor", "age": 45, "gender": "Male"},
            {"name": "Ananya Desai", "email": "ananya@example.com", "phone": "9876543218", "blood": "A+", "lat": 28.6150, "lon": 77.2150, "type": "Donor", "age": 31, "gender": "Female"},
            {"name": "Rohan Gupta", "email": "rohan@example.com", "phone": "9876543219", "blood": "AB-", "lat": 28.6300, "lon": 77.2200, "type": "Donor", "age": 27, "gender": "Male"},
            {"name": "Neha Joshi", "email": "neha@example.com", "phone": "9876543220", "blood": "B+", "lat": 28.6400, "lon": 77.2100, "type": "Donor", "age": 33, "gender": "Female"},
            {"name": "Karan Malhotra", "email": "karan@example.com", "phone": "9876543221", "blood": "O-", "lat": 28.6250, "lon": 77.1950, "type": "Donor", "age": 38, "gender": "Male"},
            {"name": "Sonia Kapoor", "email": "sonia@example.com", "phone": "9876543222", "blood": "A-", "lat": 28.6100, "lon": 77.2250, "type": "Donor", "age": 26, "gender": "Female"},
            {"name": "Aman Nair", "email": "aman@example.com", "phone": "9876543223", "blood": "O+", "lat": 28.6350, "lon": 77.1900, "type": "Donor", "age": 30, "gender": "Male"},
            {"name": "Simran Kaur", "email": "simran@example.com", "phone": "9876543224", "blood": "B-", "lat": 28.6050, "lon": 77.2050, "type": "Donor", "age": 29, "gender": "Female"},
            {"name": "City Hospital", "email": "contact@cityhospital.com", "phone": "9998887776", "type": "Hospital"},
            {"name": "John Doe", "email": "john.doe@example.com", "phone": "9998887775", "type": "Patient", "blood": "AB-", "age": 45, "gender": "Male"}
        ]

        for u in mock_users:
            if User.query.filter_by(phone_number=u['phone']).first():
                continue
            
            user = User(
                name=u['name'],
                email=u.get('email'),
                phone_number=u['phone'],
                user_type=u['type'],
                blood_group=u.get('blood'),
                age=u.get('age'),
                gender=u.get('gender'),
                verified_email=True,
                verified_mobile=True,
                verification_status='Verified',
                hospital_verification_status='Verified'
            )
            user.set_password('password123')
            db.session.add(user)
            db.session.flush()

            if u['type'] == 'Donor':
                is_priya = user.name == "Priya Sharma"
                last_don = datetime.datetime(2026, 5, 2) if is_priya else (datetime.datetime.now(datetime.UTC).replace(tzinfo=None) - datetime.timedelta(days=100))
                avail = False if is_priya else True
                donations_cnt = 3 if is_priya else 1
                
                profile = DonorProfile(
                    user_id=user.id,
                    blood_group=u['blood'],
                    age=u.get('age') or 25,
                    gender=u.get('gender') or 'Other',
                    latitude=u['lat'],
                    longitude=u['lon'],
                    is_available_today=avail,
                    health_score=98,
                    last_donation_date=last_don,
                    donation_count=donations_cnt
                )
                db.session.add(profile)
                db.session.flush()

                if is_priya:
                    db.session.add(DonationRecord(donor_id=profile.id, donation_date=datetime.datetime(2025, 11, 18), hospital_name="City Hospital", location="New Delhi"))
                    db.session.add(DonationRecord(donor_id=profile.id, donation_date=datetime.datetime(2026, 2, 14), hospital_name="Red Cross Blood Center", location="New Delhi"))
                    db.session.add(DonationRecord(donor_id=profile.id, donation_date=datetime.datetime(2026, 5, 2), hospital_name="Metro Hospital", location="New Delhi"))
                else:
                    db.session.add(DonationRecord(donor_id=profile.id, donation_date=last_don, hospital_name="City Clinic", location="New Delhi"))
        
        db.session.commit()
        print("Mock donors seeded successfully!")

if __name__ == "__main__":
    seed_db()
