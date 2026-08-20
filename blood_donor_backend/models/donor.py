from extensions import db, utcnow_naive
from datetime import datetime

class DonorProfile(db.Model):
    __tablename__ = 'donor_profiles'
    
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    blood_group = db.Column(db.String(5), nullable=False)
    age = db.Column(db.Integer, nullable=True)
    gender = db.Column(db.String(20), nullable=True)
    health_score = db.Column(db.Integer, default=50) # Score out of 100
    donation_count = db.Column(db.Integer, default=0)  # Real donation counter
    response_rate = db.Column(db.Float, default=0.95)
    
    # Daily dynamic location & availability
    is_available_today = db.Column(db.Boolean, default=False)
    today_availability = db.Column(db.Boolean, default=False)
    availability_updated_at = db.Column(db.DateTime, default=utcnow_naive)
    latitude = db.Column(db.Float, nullable=True)
    longitude = db.Column(db.Float, nullable=True)
    state = db.Column(db.String(100), nullable=True)
    district = db.Column(db.String(100), nullable=True)
    city = db.Column(db.String(100), nullable=True)
    
    # 90-day block logic & eligibility status
    last_donation_date = db.Column(db.DateTime, nullable=True)
    eligibility_status = db.Column(db.String(20), default='ELIGIBLE') # ELIGIBLE, INELIGIBLE

    # Response Analytics
    total_requests_received = db.Column(db.Integer, default=0)
    total_requests_accepted = db.Column(db.Integer, default=0)
    total_requests_rejected = db.Column(db.Integer, default=0)
    total_requests_ignored = db.Column(db.Integer, default=0)
    response_time_average = db.Column(db.Float, default=0.0) # in minutes
    cancellation_count = db.Column(db.Integer, default=0)
    
    # Trust Score (0-100)
    trust_score = db.Column(db.Integer, default=75)

    donations = db.relationship('DonationRecord', backref='donor', lazy=True, cascade="all, delete-orphan")

    @property
    def trust_score_computed(self):
        """Dynamic Trust Score calculation out of 100 based on verification, donations, response, and cancellation rates."""
        score = 50 # Base score
        
        try:
            from models.user import User
            user = db.session.get(User, self.user_id)
            if user and user.verification_status == 'Verified':
                score += 15
        except Exception:
            if self.user and self.user.verification_status == 'Verified':
                score += 15
            
        # Donation bonus
        score += min((self.donation_count or 0) * 5, 20)
        
        # Response rate bonus
        if self.response_rate:
            score += int(self.response_rate * 15)
            
        # Cancellation penalty
        score -= min((self.cancellation_count or 0) * 10, 30)
        
        return max(0, min(score, 100))

    def to_dict(self):
        # Format last_donation_date as a human-readable string
        last_donated_str = None
        if self.last_donation_date:
            last_donated_str = self.last_donation_date.strftime("%b %d, %Y")  # e.g. "May 2, 2026"

        return {
            'id': self.id,
            'user_id': self.user_id,
            'name': self.user.name if self.user else None,
            'phone_number': self.user.phone_number if self.user else None,
            'blood_group': self.blood_group,
            'age': self.age,
            'gender': self.gender,
            'health_score': self.health_score,
            'donation_count': self.donation_count,
            'trust_score': self.trust_score_computed,
            'response_rate': self.response_rate,
            'is_available_today': self.today_availability, # Sync today_availability to frontend
            'today_availability': self.today_availability,
            'availability_updated_at': self.availability_updated_at.isoformat() if self.availability_updated_at else None,
            'latitude': self.latitude,
            'longitude': self.longitude,
            'state': self.state,
            'district': self.district,
            'city': self.city,
            'last_donation_date': last_donated_str,
            'eligibility_status': self.eligibility_status,
            'total_requests_received': self.total_requests_received,
            'total_requests_accepted': self.total_requests_accepted,
            'total_requests_rejected': self.total_requests_rejected,
            'total_requests_ignored': self.total_requests_ignored,
            'response_time_average': self.response_time_average,
            'cancellation_count': self.cancellation_count,
            'donations': [d.to_dict() for d in sorted(self.donations, key=lambda x: x.donation_date)]
        }


class DonationRecord(db.Model):
    __tablename__ = 'donation_records'
    
    id = db.Column(db.Integer, primary_key=True)
    donor_id = db.Column(db.Integer, db.ForeignKey('donor_profiles.id'), nullable=False)
    donation_date = db.Column(db.DateTime, nullable=False, default=utcnow_naive)
    hospital_name = db.Column(db.String(200), nullable=True)
    location = db.Column(db.String(200), nullable=True)

    def to_dict(self):
        return {
            'id': self.id,
            'donor_id': self.donor_id,
            'donation_date': f"{self.donation_date.strftime('%b')} {self.donation_date.day}, {self.donation_date.year}",
            'hospital_name': self.hospital_name,
            'location': self.location
        }
