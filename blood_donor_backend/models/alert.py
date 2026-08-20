from extensions import db, utcnow_naive
from datetime import datetime

class Alert(db.Model):
    __tablename__ = 'alerts'
    
    id = db.Column(db.Integer, primary_key=True)
    patient_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    blood_group = db.Column(db.String(10), nullable=False)
    hospital_name = db.Column(db.String(255), nullable=False)
    latitude = db.Column(db.Float, nullable=True)
    longitude = db.Column(db.Float, nullable=True)
    status = db.Column(db.String(20), default='CREATED') # CREATED, MATCHING, ALERT_SENT, DONOR_ACCEPTED, TRAVELING, IN_PROGRESS, COMPLETED, EXPIRED, CANCELLED, CLOSED
    urgency = db.Column(db.String(20), default='High') # Critical, High, Moderate, Normal
    priority_level = db.Column(db.String(20), default='High') # Critical, High, Moderate, Normal
    queue_position = db.Column(db.Integer, nullable=True)
    units_required = db.Column(db.Integer, default=1)
    timestamp = db.Column(db.DateTime, default=utcnow_naive)
    expiry_time = db.Column(db.DateTime, nullable=True)
    cancellation_reason = db.Column(db.String(255), nullable=True)
    
    # Route estimation fields after donor acceptance
    accepted_by_donor_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=True)
    estimated_arrival_time = db.Column(db.DateTime, nullable=True)
    travel_distance = db.Column(db.Float, nullable=True)
    travel_duration = db.Column(db.Float, nullable=True)

    # Relationships
    patient = db.relationship('User', foreign_keys=[patient_id])
    accepted_donor = db.relationship('User', foreign_keys=[accepted_by_donor_id])

    def to_dict(self):
        return {
            'id': self.id,
            'patient_id': self.patient_id,
            'patient_name': self.patient.name if self.patient else "Unknown",
            'blood_group': self.blood_group,
            'hospital_name': self.hospital_name,
            'latitude': self.latitude,
            'longitude': self.longitude,
            'status': self.status,
            'urgency': self.urgency,
            'priority_level': self.priority_level,
            'queue_position': self.queue_position,
            'units_required': self.units_required,
            'timestamp': self.timestamp.isoformat(),
            'expiry_time': self.expiry_time.isoformat() if self.expiry_time else None,
            'cancellation_reason': self.cancellation_reason,
            'accepted_by_donor_id': self.accepted_by_donor_id,
            'accepted_donor_name': self.accepted_donor.name if self.accepted_donor else None,
            'estimated_arrival_time': self.estimated_arrival_time.isoformat() if self.estimated_arrival_time else None,
            'travel_distance': self.travel_distance,
            'travel_duration': self.travel_duration
        }

class AlertHistory(db.Model):
    __tablename__ = 'alert_histories'
    
    id = db.Column(db.Integer, primary_key=True)
    alert_id = db.Column(db.Integer, db.ForeignKey('alerts.id', ondelete='CASCADE'), nullable=False)
    donor_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    notification_sent = db.Column(db.Boolean, default=False)
    sms_sent = db.Column(db.Boolean, default=False)
    notification_time = db.Column(db.DateTime, default=utcnow_naive)
    response_status = db.Column(db.String(20), default='ignored') # notified, accepted, declined, ignored
    response_time = db.Column(db.DateTime, nullable=True)

    alert = db.relationship('Alert', backref=db.backref('history_records', lazy=True, cascade="all, delete-orphan"))
    donor = db.relationship('User')

    def to_dict(self):
        return {
            'id': self.id,
            'alert_id': self.alert_id,
            'donor_id': self.donor_id,
            'donor_name': self.donor.name if self.donor else "Unknown",
            'notification_sent': self.notification_sent,
            'sms_sent': self.sms_sent,
            'notification_time': self.notification_time.isoformat() if self.notification_time else None,
            'response_status': self.response_status,
            'response_time': self.response_time.isoformat() if self.response_time else None
        }

class RequestStatusHistory(db.Model):
    __tablename__ = 'request_status_histories'
    
    id = db.Column(db.Integer, primary_key=True)
    alert_id = db.Column(db.Integer, db.ForeignKey('alerts.id', ondelete='CASCADE'), nullable=False)
    status = db.Column(db.String(50), nullable=False)
    timestamp = db.Column(db.DateTime, default=utcnow_naive)

    def to_dict(self):
        return {
            'id': self.id,
            'alert_id': self.alert_id,
            'status': self.status,
            'timestamp': self.timestamp.isoformat()
        }

class DigitalCertificate(db.Model):
    __tablename__ = 'digital_certificates'
    
    id = db.Column(db.Integer, primary_key=True)
    certificate_id = db.Column(db.String(100), unique=True, nullable=False)
    donor_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    hospital_name = db.Column(db.String(255), nullable=False)
    donation_date = db.Column(db.DateTime, default=utcnow_naive)
    blood_group = db.Column(db.String(10), nullable=False)
    qr_code_content = db.Column(db.String(255), nullable=False)

    donor = db.relationship('User')

    def to_dict(self):
        return {
            'id': self.id,
            'certificate_id': self.certificate_id,
            'donor_id': self.donor_id,
            'donor_name': self.donor.name if self.donor else "Unknown",
            'hospital_name': self.hospital_name,
            'donation_date': self.donation_date.strftime("%b %d, %Y"),
            'blood_group': self.blood_group,
            'qr_code_content': self.qr_code_content
        }

class BloodBank(db.Model):
    __tablename__ = 'blood_banks'
    
    id = db.Column(db.Integer, primary_key=True)
    blood_bank_name = db.Column(db.String(255), nullable=False)
    location = db.Column(db.String(255), nullable=False)
    latitude = db.Column(db.Float, nullable=True)
    longitude = db.Column(db.Float, nullable=True)
    contact_number = db.Column(db.String(20), nullable=False)
    availability_status = db.Column(db.String(50), default='Available')

    def to_dict(self):
        return {
            'id': self.id,
            'blood_bank_name': self.blood_bank_name,
            'location': self.location,
            'latitude': self.latitude,
            'longitude': self.longitude,
            'contact_number': self.contact_number,
            'availability_status': self.availability_status
        }

class VerificationLog(db.Model):
    __tablename__ = 'verification_logs'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    event_type = db.Column(db.String(50), nullable=False) # e.g. OTP_SENT, VERIFIED
    details = db.Column(db.String(255), nullable=True)
    timestamp = db.Column(db.DateTime, default=utcnow_naive)

class NotificationLog(db.Model):
    __tablename__ = 'notification_logs'
    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    title = db.Column(db.String(100), nullable=False)
    body = db.Column(db.String(255), nullable=False)
    status = db.Column(db.String(20), default='SENT')
    timestamp = db.Column(db.DateTime, default=utcnow_naive)

class OTPVerificationLog(db.Model):
    __tablename__ = 'otp_verification_logs'
    id = db.Column(db.Integer, primary_key=True)
    phone_number = db.Column(db.String(20), nullable=False)
    otp_code = db.Column(db.String(10), nullable=False)
    is_used = db.Column(db.Boolean, default=False)
    expires_at = db.Column(db.DateTime, nullable=False)
    timestamp = db.Column(db.DateTime, default=utcnow_naive)
