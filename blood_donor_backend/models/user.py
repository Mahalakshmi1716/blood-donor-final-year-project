from extensions import db, utcnow_naive
from datetime import datetime
from werkzeug.security import generate_password_hash, check_password_hash

class User(db.Model):
    __tablename__ = 'users'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=True)
    phone_number = db.Column(db.String(20), unique=True, nullable=False)
    password_hash = db.Column(db.String(256), nullable=False)
    user_type = db.Column(db.String(20), default='Donor', nullable=False)
    blood_group = db.Column(db.String(5), nullable=True)
    age = db.Column(db.Integer, nullable=True)
    gender = db.Column(db.String(20), nullable=True)
    created_at = db.Column(db.DateTime, default=utcnow_naive)
    
    # Verification & Security
    verified_mobile = db.Column(db.Boolean, default=False)
    verified_email = db.Column(db.Boolean, default=False)
    verification_status = db.Column(db.String(20), default='Unverified') # Verified, Unverified
    hospital_verification_status = db.Column(db.String(30), default='Pending Verification') # Pending Verification, Verified, Rejected
    hospital_license = db.Column(db.String(255), nullable=True)
    verification_date = db.Column(db.DateTime, nullable=True)
    
    # Location coordinates for Hospital Validation
    latitude = db.Column(db.Float, nullable=True)
    longitude = db.Column(db.Float, nullable=True)
    registered_address = db.Column(db.String(255), nullable=True)
    
    # Preferences
    preferred_language = db.Column(db.String(10), default='en') # en, ta, hi
    
    # Relationships
    donor_profile = db.relationship('DonorProfile', backref='user', uselist=False, cascade="all, delete-orphan")

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)
        
    def check_password(self, password):
        return check_password_hash(self.password_hash, password)

    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'email': self.email,
            'phone_number': self.phone_number,
            'user_type': self.user_type,
            'blood_group': self.blood_group,
            'age': self.age,
            'gender': self.gender,
            'is_donor': self.donor_profile is not None,
            'verified_mobile': self.verified_mobile,
            'verified_email': self.verified_email,
            'verification_status': self.verification_status,
            'hospital_verification_status': self.hospital_verification_status,
            'hospital_license': self.hospital_license,
            'verification_date': self.verification_date.isoformat() if self.verification_date else None,
            'latitude': self.latitude,
            'longitude': self.longitude,
            'registered_address': self.registered_address,
            'preferred_language': self.preferred_language
        }
