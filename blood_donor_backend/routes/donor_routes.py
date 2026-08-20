from flask import Blueprint, request, jsonify
from extensions import db
from models import DonorProfile, DonationRecord
from utils.auth import token_required
from datetime import datetime, UTC

donor_bp = Blueprint('donor', __name__)

@donor_bp.route('/profile', methods=['POST'])
@token_required
def create_profile(current_user):
    data = request.get_json()
    
    if current_user.donor_profile:
        return jsonify({'message': 'Donor profile already exists'}), 400
        
    if not data or not data.get('blood_group'):
        return jsonify({'message': 'Blood group is required'}), 400

    last_donation_date = None
    if data.get('last_donation_date'):
        try:
            last_donation_date = datetime.strptime(data['last_donation_date'], "%Y-%m-%d")
        except ValueError:
            pass

    eligibility = 'ELIGIBLE'
    avail = False
    if last_donation_date:
        days_since = (datetime.now(UTC).replace(tzinfo=None) - last_donation_date).days
        if days_since < 90:
            eligibility = 'INELIGIBLE'
            avail = False
        else:
            eligibility = 'ELIGIBLE'

    new_profile = DonorProfile(
        user_id=current_user.id,
        blood_group=data['blood_group'],
        age=data.get('age'),
        gender=data.get('gender'),
        last_donation_date=last_donation_date,
        eligibility_status=eligibility,
        is_available_today=avail,
        today_availability=avail,
        availability_updated_at=datetime.now(UTC).replace(tzinfo=None)
    )
    
    db.session.add(new_profile)
    db.session.flush()

    if last_donation_date:
        record = DonationRecord(
            donor_id=new_profile.id,
            donation_date=last_donation_date,
            hospital_name="Initial Record",
            location="Previously Recorded"
        )
        db.session.add(record)

    db.session.commit()
    
    return jsonify({'message': 'Profile created successfully', 'profile': new_profile.to_dict()}), 201

@donor_bp.route('/profile', methods=['GET'])
@token_required
def get_profile(current_user):
    if not current_user.donor_profile:
        return jsonify({'message': 'Donor profile not found'}), 404
    return jsonify({'profile': current_user.donor_profile.to_dict()}), 200

@donor_bp.route('/availability', methods=['POST'])
@token_required
def update_availability(current_user):
    data = request.get_json()
    profile = current_user.donor_profile
    
    if not profile:
        return jsonify({'message': 'Donor profile not found'}), 404
        
    # Check if we are trying to make available
    is_avail = data.get('is_available_today') or data.get('today_availability')
    if is_avail:
        # Strictly prevent making available if in cooldown or eligibility status is INELIGIBLE
        if profile.eligibility_status == 'INELIGIBLE':
            return jsonify({'message': 'Strict cooldown active. Cannot change status to Available.'}), 400
        if profile.last_donation_date:
            days_since = (datetime.now(UTC).replace(tzinfo=None) - profile.last_donation_date).days
            if days_since < 90:
                profile.eligibility_status = 'INELIGIBLE'
                return jsonify({'message': 'Strict cooldown active. Cannot change status to Available.'}), 400
                
    if 'is_available_today' in data or 'today_availability' in data:
        val = data.get('is_available_today') if 'is_available_today' in data else data.get('today_availability')
        profile.is_available_today = val
        profile.today_availability = val
        profile.availability_updated_at = datetime.now(UTC).replace(tzinfo=None)
        
    if 'latitude' in data and 'longitude' in data:
        profile.latitude = data['latitude']
        profile.longitude = data['longitude']
        
    if 'state' in data:
        profile.state = data['state']
    if 'district' in data:
        profile.district = data['district']
    if 'city' in data:
        profile.city = data['city']
        
    db.session.commit()
    return jsonify({"message": "Availability updated", "profile": profile.to_dict()}), 200

@donor_bp.route('/record-donation', methods=['POST'])
@token_required
def record_donation(current_user):
    profile = current_user.donor_profile
    if not profile:
        return jsonify({'message': 'Donor profile not found'}), 404

    data = request.get_json() or {}
    last_donation_date_str = data.get('last_donation_date')

    if not last_donation_date_str:
        return jsonify({'message': 'Last donation date is required.'}), 400

    try:
        # Expect date format YYYY-MM-DD
        donation_date = datetime.strptime(last_donation_date_str, "%Y-%m-%d")
    except ValueError:
        return jsonify({'message': 'Invalid date format. Use YYYY-MM-DD.'}), 400

    if donation_date > datetime.now(UTC).replace(tzinfo=None):
        return jsonify({'message': 'Donation date cannot be in the future.'}), 400

    # Dynamic eligibility computation based on the date of last donation
    days_since = (datetime.now(UTC).replace(tzinfo=None) - donation_date).days
    
    if days_since < 90:
        profile.eligibility_status = 'INELIGIBLE'
        profile.is_available_today = False
        profile.today_availability = False
    else:
        profile.eligibility_status = 'ELIGIBLE'

    profile.last_donation_date = donation_date
    profile.donation_count = (profile.donation_count or 0) + 1
    profile.availability_updated_at = datetime.now(UTC).replace(tzinfo=None)

    # Log the donation history entry
    record = DonationRecord(
        donor_id=profile.id,
        donation_date=donation_date,
        hospital_name="Recorded Donation",
        location="Self Reported"
    )
    db.session.add(record)
    db.session.commit()

    return jsonify({
        "message": "Donation recorded successfully.",
        "last_donation_date": profile.last_donation_date.strftime("%Y-%m-%d"),
        "donation_count": profile.donation_count,
        "eligibility_status": profile.eligibility_status
    }), 200

@donor_bp.route('/tip-of-the-day', methods=['GET'])
@token_required
def get_tip_of_the_day(current_user):
    from services.groq_service import generate_tip_of_the_day
    tip = generate_tip_of_the_day()
    return jsonify({'tip': tip}), 200

