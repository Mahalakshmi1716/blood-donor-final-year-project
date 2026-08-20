from flask import Blueprint, request, jsonify, send_file
from utils.auth import token_required
from services.matching import score_and_sort_donors
from models import Alert, AlertHistory, RequestStatusHistory, DigitalCertificate, BloodBank, User, DonorProfile, DonationRecord
from extensions import db
from email_service import send_email
from datetime import datetime, timedelta, UTC
import threading
import time
import uuid
import io

patient_bp = Blueprint('patient', __name__)

def run_alert_escalation(alert_id, compatible_donors):
    from app import create_app
    app = create_app()
    with app.app_context():
        # Late imports of models within database session
        from models import Alert, AlertHistory, RequestStatusHistory, DonorProfile, User
        
        print(f"[ESCALATION ENGINE] Started background escalation thread for Alert ID: {alert_id}")
        
        # Group donors in chunks of 20
        batches = [compatible_donors[i:i+20] for i in range(0, len(compatible_donors), 20)]
        
        for batch_index, batch in enumerate(batches):
            db.session.expire_all()
            alert = Alert.query.get(alert_id)
            if not alert or alert.status in ['DONOR_ACCEPTED', 'TRAVELING', 'IN_PROGRESS', 'COMPLETED', 'CLOSED', 'CANCELLED', 'EXPIRED']:
                print(f"[ESCALATION ENGINE] Alert ID {alert_id} is no longer active (status: {alert.status if alert else 'deleted'}). Exiting.")
                return

            print(f"[ESCALATION ENGINE] Notifying Batch {batch_index + 1} ({len(batch)} donors) for Alert ID: {alert_id}")
            
            # Log escalation step
            status_update = RequestStatusHistory(alert_id=alert_id, status=f"ALERT_SENT_BATCH_{batch_index+1}")
            db.session.add(status_update)
            alert.status = 'ALERT_SENT'
            
            for donor_data in batch:
                donor_id = donor_data['donor_id']
                donor_profile = DonorProfile.query.filter_by(user_id=donor_id).first()
                if donor_profile:
                    donor_profile.total_requests_received += 1
                
                hist = AlertHistory(
                    alert_id=alert_id,
                    donor_id=donor_id,
                    notification_sent=True,
                    sms_sent=True,
                    notification_time=datetime.now(UTC).replace(tzinfo=None),
                    response_status='notified'
                )
                db.session.add(hist)
                
                # Mock SMS and Push notifications
                print(f"[PUSH SIMULATION] Alert ID {alert_id} sent to Donor {donor_data['name']}")
                print(f"[SMS SIMULATION] SMS to {donor_data['phone_number']}: URGENT {alert.priority_level} Alert! Need {alert.units_required} unit(s) of {alert.blood_group} blood at {alert.hospital_name}.")
                # Send Brevo email to donor
                donor_user = User.query.get(donor_id)

                if donor_user and donor_user.email:
                    email_subject = f"🚨 {alert.priority_level} Blood Donation Request"

                    email_message = f"""
                    <html>
                    <body>
                        <h2>🚨 Emergency Blood Donation Request</h2>

                        <p>Hello {donor_data['name']},</p>

                        <p>A patient urgently needs blood donation.</p>

                        <p>
                            <b>Blood Group:</b> {alert.blood_group}<br>
                            <b>Units Required:</b> {alert.units_required}<br>
                            <b>Hospital:</b> {alert.hospital_name}<br>
                            <b>Urgency:</b> {alert.priority_level}
                        </p>

                        <p>
                            If you are available and eligible to donate,
                            please open the Blood Donor Finder app and accept
                            the request.
                        </p>

                        <p>Thank you for helping save a life ❤️</p>

                        <p><b>Blood Donor Finder</b></p>
                    </body>
                    </html>
                    """

                    try:
                        send_email(
                            to_email=donor_user.email,
                            to_name=donor_data['name'],
                            subject=email_subject,
                            message=email_message
                        )
                    except Exception as e:
                        print(f"[ESCALATION EMAIL ERROR] Failed to send email to {donor_user.email}: {e}")
                
            db.session.commit()
            
            # Wait 60 seconds (sleeping in 1s cycles to immediately catch donor acceptances)
            for _ in range(60):
                time.sleep(1)
                db.session.expire_all()
                alert = Alert.query.get(alert_id)
                if not alert or alert.status in ['DONOR_ACCEPTED', 'TRAVELING', 'IN_PROGRESS', 'COMPLETED', 'CLOSED', 'CANCELLED', 'EXPIRED']:
                    print(f"[ESCALATION ENGINE] Alert ID {alert_id} accepted/cancelled during delay. Stopping.")
                    return
                    
        # Escalation Levels 3 & 4: If still unfulfilled, notify hospitals and blood banks
        alert = Alert.query.get(alert_id)
        if alert and alert.status not in ['DONOR_ACCEPTED', 'TRAVELING', 'IN_PROGRESS', 'COMPLETED', 'CLOSED', 'CANCELLED', 'EXPIRED']:
            print(f"[ESCALATION ENGINE] Donors exhausted. Escalating Alert ID {alert_id} to Level 3 & 4 (Nearby Hospitals & Blood Banks).")
            status_update = RequestStatusHistory(alert_id=alert_id, status="ESCALATED_TO_INSTITUTIONS")
            db.session.add(status_update)
            db.session.commit()
            print(f"[ESCALATION BROADCAST] Alerting all fallback hospitals and blood banks for supply of {alert.blood_group}!")


@patient_bp.route('/search', methods=['POST'])
@token_required
def search_donors(current_user):
    data = request.get_json()
    required_blood = data.get('blood_group')
    patient_lat = data.get('latitude')
    patient_lon = data.get('longitude')
    urgency = data.get('urgency', 'High')
    
    if not all([required_blood, patient_lat, patient_lon]):
        return jsonify({'message': 'Missing blood group or location data'}), 400
        
    required_blood = required_blood.strip().upper()
    VALID_BLOOD_GROUPS = {'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'}
    if required_blood not in VALID_BLOOD_GROUPS:
        return jsonify({'message': f'Invalid blood group: {required_blood}'}), 400
        
    # Privacy check: obfuscate exact locations for matches publicly displayed before acceptance
    matched_donors = score_and_sort_donors(required_blood, patient_lat, patient_lon, exclude_user_id=current_user.id, urgency=urgency, obfuscate_locations=True)
    
    # Blood bank fallback activation if no donors are found
    fallback_activated = len(matched_donors) == 0
    fallback_banks = []
    if fallback_activated:
        fallback_banks = [bb.to_dict() for bb in BloodBank.query.all()]
        
    return jsonify({
        "donors": matched_donors,
        "fallback_activated": fallback_activated,
        "fallback_blood_banks": fallback_banks
    }), 200


@patient_bp.route('/sos', methods=['POST'])
@token_required
def trigger_sos(current_user):
    data = request.get_json()
    
    required_blood = data.get('blood_group')
    hospital_name = data.get('hospital_name')
    patient_lat = data.get('latitude')
    patient_lon = data.get('longitude')
    urgency = data.get('urgency', 'High')
    units = data.get('units_required', 1)
    contact_number = current_user.phone_number
    
    # Duplicate Request Prevention
    existing_alert = Alert.query.filter(
        Alert.patient_id == current_user.id,
        ~Alert.status.in_(['COMPLETED', 'CLOSED', 'CANCELLED', 'EXPIRED'])
    ).first()
    if existing_alert:
        return jsonify({'message': 'An emergency request is already active.'}), 400

    # Hospital Location & Status Validation
    if current_user.user_type == 'Hospital':
        if current_user.hospital_verification_status != 'Verified':
            return jsonify({'message': 'Only Verified Hospitals can create blood requests.'}), 403
        if current_user.latitude is None or current_user.longitude is None:
            return jsonify({'message': 'Hospital coordinates must be verified before request creation.'}), 400

    # Expiry settings
    now = datetime.now(UTC).replace(tzinfo=None)
    if urgency == 'Critical':
        expiry = now + timedelta(hours=2)
    elif urgency == 'High':
        expiry = now + timedelta(hours=4)
    elif urgency == 'Moderate':
        expiry = now + timedelta(hours=8)
    else:
        expiry = now + timedelta(hours=24)

    # Queue Priority Position
    active_count = Alert.query.filter(~Alert.status.in_(['COMPLETED', 'CLOSED', 'CANCELLED', 'EXPIRED'])).count()

    new_alert = Alert(
        patient_id=current_user.id,
        blood_group=required_blood,
        hospital_name=hospital_name,
        latitude=patient_lat,
        longitude=patient_lon,
        status='CREATED',
        urgency=urgency,
        priority_level=urgency,
        queue_position=active_count + 1,
        units_required=units,
        expiry_time=expiry
    )
    db.session.add(new_alert)
    db.session.commit()
    
    # Status History Log
    db.session.add(RequestStatusHistory(alert_id=new_alert.id, status='CREATED'))
    db.session.commit()

    # Get non-obfuscated scoring details for escalation thread
    matched_donors = score_and_sort_donors(required_blood, patient_lat, patient_lon, exclude_user_id=current_user.id, urgency=urgency, obfuscate_locations=False)
    
    # Fallback system if no donors match
    fallback_activated = len(matched_donors) == 0
    fallback_banks = []
    if fallback_activated:
        fallback_banks = [bb.to_dict() for bb in BloodBank.query.all()]
        new_alert.status = 'CLOSED'
        db.session.commit()
        db.session.add(RequestStatusHistory(alert_id=new_alert.id, status='CLOSED_NO_DONORS'))
        db.session.commit()
    else:
        # Spawn escalation thread
        threading.Thread(target=run_alert_escalation, args=(new_alert.id, matched_donors), daemon=True).start()

    message = f"{urgency.upper()} ALERT: Need {units} unit(s) of {required_blood} blood immediately at {hospital_name}. Please contact {contact_number} if you can help!"
    
    return jsonify({
        "message": "SOS triggered successfully." if not fallback_activated else "SOS triggered. No matching donors; Fallback Activated.",
        "prefilled_message": message,
        "suggested_donors": matched_donors[:5],
        "fallback_activated": fallback_activated,
        "fallback_blood_banks": fallback_banks,
        "alert": new_alert.to_dict()
    }), 200


@patient_bp.route('/alerts/<int:alert_id>/accept', methods=['POST'])
@token_required
def accept_alert(current_user, alert_id):
    # Safe database locking
    alert = db.session.query(Alert).filter_by(id=alert_id).with_for_update().first()
    
    if not alert:
        return jsonify({'message': 'Alert not found'}), 404
        
    if alert.status in ['DONOR_ACCEPTED', 'TRAVELING', 'IN_PROGRESS', 'COMPLETED', 'CLOSED', 'CANCELLED', 'EXPIRED']:
        return jsonify({'message': 'Request Already Fulfilled'}), 400

    hist = AlertHistory.query.filter_by(alert_id=alert_id, donor_id=current_user.id).first()
    if not hist:
        hist = AlertHistory(
            alert_id=alert_id,
            donor_id=current_user.id,
            notification_sent=True,
            sms_sent=True,
            notification_time=datetime.now(UTC).replace(tzinfo=None)
        )
        db.session.add(hist)
        
    hist.response_status = 'accepted'
    hist.response_time = datetime.now(UTC).replace(tzinfo=None)
    
    alert.status = 'DONOR_ACCEPTED'
    alert.accepted_by_donor_id = current_user.id
    
    db.session.add(RequestStatusHistory(alert_id=alert_id, status='DONOR_ACCEPTED'))
    
    # Update Donor Analytics
    profile = current_user.donor_profile
    if profile:
        profile.total_requests_accepted += 1
        time_diff = (hist.response_time - hist.notification_time).total_seconds() / 60.0
        old_avg = profile.response_time_average or 0.0
        old_count = max(0, profile.total_requests_accepted - 1)
        profile.response_time_average = (old_avg * old_count + time_diff) / (old_count + 1)
        
        total_recv = profile.total_requests_received or 1
        profile.response_rate = profile.total_requests_accepted / float(total_recv)
        
    # Mark other matching alert notifications as ignored
    other_histories = AlertHistory.query.filter(AlertHistory.alert_id == alert_id, AlertHistory.donor_id != current_user.id).all()
    for other_h in other_histories:
        other_h.response_status = 'ignored'
        other_profile = DonorProfile.query.filter_by(user_id=other_h.donor_id).first()
        if other_profile:
            other_profile.total_requests_ignored += 1
            total_recv = other_profile.total_requests_received or 1
            other_profile.response_rate = other_profile.total_requests_accepted / float(total_recv)
            
    db.session.commit()
    
    return jsonify({
        'message': 'Alert accepted successfully.',
        'alert': alert.to_dict()
    }), 200


@patient_bp.route('/alerts/<int:alert_id>/decline', methods=['POST'])
@token_required
def decline_alert(current_user, alert_id):
    hist = AlertHistory.query.filter_by(alert_id=alert_id, donor_id=current_user.id).first()
    if not hist:
        hist = AlertHistory(
            alert_id=alert_id,
            donor_id=current_user.id,
            notification_sent=True,
            sms_sent=True,
            notification_time=datetime.now(UTC).replace(tzinfo=None)
        )
        db.session.add(hist)
        
    hist.response_status = 'declined'
    hist.response_time = datetime.now(UTC).replace(tzinfo=None)
    
    profile = current_user.donor_profile
    if profile:
        profile.total_requests_rejected += 1
        total_recv = profile.total_requests_received or 1
        profile.response_rate = profile.total_requests_accepted / float(total_recv)
        
    db.session.commit()
    return jsonify({'message': 'Alert declined successfully'}), 200


@patient_bp.route('/alerts/<int:alert_id>/start-travel', methods=['POST'])
@token_required
def start_travel(current_user, alert_id):
    alert = Alert.query.get(alert_id)
    if not alert:
        return jsonify({'message': 'Alert not found'}), 404
        
    alert.status = 'TRAVELING'
    alert.travel_distance = 4.8
    alert.travel_duration = 15.0
    alert.estimated_arrival_time = datetime.now(UTC).replace(tzinfo=None) + timedelta(minutes=15)
    
    db.session.add(RequestStatusHistory(alert_id=alert_id, status='TRAVELING'))
    db.session.commit()
    
    return jsonify({
        'message': 'Traveling status updated.',
        'alert': alert.to_dict()
    }), 200


@patient_bp.route('/alerts/<int:alert_id>/start-donation', methods=['POST'])
@token_required
def start_donation(current_user, alert_id):
    alert = Alert.query.get(alert_id)
    if not alert:
        return jsonify({'message': 'Alert not found'}), 404
        
    alert.status = 'IN_PROGRESS'
    db.session.add(RequestStatusHistory(alert_id=alert_id, status='IN_PROGRESS'))
    db.session.commit()
    
    return jsonify({
        'message': 'Donation procedure started.',
        'alert': alert.to_dict()
    }), 200


@patient_bp.route('/alerts/<int:alert_id>/confirm-donation', methods=['POST'])
@token_required
def confirm_donation(current_user, alert_id):
    alert = Alert.query.get(alert_id)
    if not alert:
        return jsonify({'message': 'Alert not found'}), 404
        
    alert.status = 'COMPLETED'
    db.session.add(RequestStatusHistory(alert_id=alert_id, status='COMPLETED'))
    
    donor_id = alert.accepted_by_donor_id
    if donor_id:
        donor_profile = DonorProfile.query.filter_by(user_id=donor_id).first()
        if donor_profile:
            # Post Donation Automation
            donor_profile.last_donation_date = datetime.now(UTC).replace(tzinfo=None)
            donor_profile.eligibility_status = 'INELIGIBLE'
            donor_profile.today_availability = False
            donor_profile.is_available_today = False
            donor_profile.donation_count += 1
            
            db.session.add(DonationRecord(
                donor_id=donor_profile.id,
                donation_date=datetime.now(UTC).replace(tzinfo=None),
                hospital_name=alert.hospital_name,
                location="Hospital Coordinates"
            ))
            
            # Generate Digital Donation Certificate
            cert_id = str(uuid.uuid4())[:18].upper()
            qr_content = f"https://verify.bloodfinder.in/cert/{cert_id}"
            cert = DigitalCertificate(
                certificate_id=cert_id,
                donor_id=donor_id,
                hospital_name=alert.hospital_name,
                donation_date=datetime.now(UTC).replace(tzinfo=None),
                blood_group=donor_profile.blood_group,
                qr_code_content=qr_content
            )
            db.session.add(cert)
            db.session.commit()
            
            alert.status = 'CLOSED'
            db.session.add(RequestStatusHistory(alert_id=alert_id, status='CLOSED'))
            db.session.commit()
            
            return jsonify({
                'message': 'Donation completed. Cooldown lock active. Certificate generated.',
                'certificate': cert.to_dict()
            }), 200
            
    db.session.commit()
    return jsonify({'message': 'Donation confirmed.'}), 200


@patient_bp.route('/alerts/<int:alert_id>/cancel', methods=['POST'])
@token_required
def cancel_alert(current_user, alert_id):
    alert = Alert.query.get(alert_id)
    if not alert:
        return jsonify({'message': 'Alert not found'}), 404
        
    data = request.get_json() or {}
    reason = data.get('reason', 'Cancelled')
    
    alert.status = 'CANCELLED'
    alert.cancellation_reason = reason
    db.session.add(RequestStatusHistory(alert_id=alert_id, status='CANCELLED'))
    
    histories = AlertHistory.query.filter_by(alert_id=alert_id).all()
    for h in histories:
        h.response_status = 'ignored'
        
    if alert.accepted_by_donor_id:
        donor_profile = DonorProfile.query.filter_by(user_id=alert.accepted_by_donor_id).first()
        if donor_profile:
            donor_profile.cancellation_count += 1
            
    db.session.commit()
    return jsonify({'message': 'Alert request cancelled successfully.'}), 200


@patient_bp.route('/alerts', methods=['GET'])
@token_required
def get_alerts(current_user):
    # Enforce Eligibility Filter: Ineligible donors cannot receive/view active alerts
    if current_user.user_type == 'Donor':
        profile = current_user.donor_profile
        if profile and profile.eligibility_status != 'ELIGIBLE':
            return jsonify({"alerts": []}), 200

    alerts = Alert.query.filter(Alert.status.in_(['CREATED', 'MATCHING', 'ALERT_SENT', 'DONOR_ACCEPTED', 'TRAVELING', 'IN_PROGRESS'])).order_by(Alert.timestamp.desc()).all()
    return jsonify({"alerts": [alert.to_dict() for alert in alerts]}), 200


@patient_bp.route('/alerts/<int:alert_id>/export', methods=['GET'])
@token_required
def export_report(current_user, alert_id):
    alert = Alert.query.get(alert_id)
    if not alert:
        return jsonify({'message': 'Alert not found'}), 404
        
    # Generate CSV Report
    output = io.StringIO()
    output.write("Smart Blood Donor Finder - Emergency Report\n")
    output.write(f"Alert ID, {alert.id}\n")
    output.write(f"Urgency, {alert.urgency}\n")
    output.write(f"Blood Group, {alert.blood_group}\n")
    output.write(f"Hospital Name, {alert.hospital_name}\n")
    output.write(f"Status, {alert.status}\n")
    output.write(f"Units Requested, {alert.units_required}\n")
    output.write(f"Created Time, {alert.timestamp.isoformat()}\n")
    
    mem_file = io.BytesIO()
    mem_file.write(output.getvalue().encode('utf-8'))
    mem_file.seek(0)
    
    return send_file(
        mem_file,
        mimetype='text/csv',
        as_attachment=True,
        download_name=f"alert_report_{alert_id}.csv"
    )


@patient_bp.route('/hospital-analytics', methods=['GET'])
@token_required
def get_hospital_analytics(current_user):
    # Query aggregated stats for hospital dashboard
    total = Alert.query.filter_by(patient_id=current_user.id).count()
    active = Alert.query.filter_by(patient_id=current_user.id).filter(Alert.status.in_(['CREATED', 'ALERT_SENT', 'DONOR_ACCEPTED', 'TRAVELING', 'IN_PROGRESS'])).count()
    completed = Alert.query.filter_by(patient_id=current_user.id, status='COMPLETED').count()
    expired = Alert.query.filter_by(patient_id=current_user.id, status='EXPIRED').count()
    cancelled = Alert.query.filter_by(patient_id=current_user.id, status='CANCELLED').count()
    
    success_rate = 100 if total == 0 else int((completed / float(total)) * 100)
    
    return jsonify({
        'total_requests': total,
        'active_requests': active,
        'completed_requests': completed,
        'expired_requests': expired,
        'cancelled_requests': cancelled,
        'success_rate': success_rate,
        'average_match_time_mins': 4.5,
        'average_response_time_mins': 12.0,
        'critical_requests_count': Alert.query.filter_by(patient_id=current_user.id, urgency='Critical').count()
    }), 200


@patient_bp.route('/analytics', methods=['GET'])
@token_required
def get_patient_analytics(current_user):
    total = Alert.query.filter_by(patient_id=current_user.id).count()
    completed = Alert.query.filter_by(patient_id=current_user.id, status='COMPLETED').count()
    pending = Alert.query.filter_by(patient_id=current_user.id).filter(Alert.status.in_(['CREATED', 'ALERT_SENT'])).count()
    
    return jsonify({
        'total_requests': total,
        'completed_requests': completed,
        'pending_requests': pending,
        'average_match_time_mins': 4.5,
        'history': [a.to_dict() for a in Alert.query.filter_by(patient_id=current_user.id).all()]
    }), 200

