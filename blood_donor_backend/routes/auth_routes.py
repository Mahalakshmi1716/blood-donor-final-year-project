from flask import Blueprint, request, jsonify, current_app
from extensions import db
from models import User, OTPVerificationLog, VerificationLog, DonorProfile
import jwt
import datetime

from utils.auth import token_required
from email_service import verify_otp as verify_email_otp_code, send_generated_otp


auth_bp = Blueprint('auth', __name__)


# ============================================================
# REGISTER
# ============================================================

@auth_bp.route('/register', methods=['POST'])
def register():

    data = request.get_json()
    print("[AUTH REGISTER] Request received")
    if data:
        print(f"[AUTH REGISTER] Email: {data.get('email')}")
        print(f"[AUTH REGISTER] Role: {data.get('user_type', 'Donor')}")

    if not data or not data.get('name') or not data.get('phone_number') or not data.get('password'):
        print("[AUTH REGISTER] Validation: FAIL (Missing required fields)")
        return jsonify({
            'message': 'Missing required fields'
        }), 400
    
    print("[AUTH REGISTER] Validation: PASS")

    phone_number = data.get('phone_number').strip()

    email = data.get('email')

    if email:
        email = email.strip()
        if not email:
            email = None
    else:
        email = None

    # Check existing phone
    if User.query.filter_by(phone_number=phone_number).first():
        print("[AUTH REGISTER] Database: FAIL (Phone already registered)")
        return jsonify({
            'message': 'This phone number is already registered. Please login.'
        }), 409

    # Check existing email
    if email and User.query.filter_by(email=email).first():
        print("[AUTH REGISTER] Database: FAIL (Email already registered)")
        return jsonify({
            'message': 'This email address is already registered.'
        }), 409

    print("[AUTH REGISTER] Database: PASS")

    user_type = data.get('user_type', 'Donor')
    blood_group = data.get('blood_group')
    age = data.get('age')
    gender = data.get('gender')

    # Hospital fields
    latitude = data.get('latitude')
    longitude = data.get('longitude')
    registered_address = data.get('registered_address')
    hospital_license = data.get('hospital_license')

    # Default verification status
    verification_status = 'Unverified'

    hospital_verification_status = (
        'Pending Verification'
        if user_type == 'Hospital'
        else 'Verified'
    )

    # Create user
    new_user = User(
        name=data['name'],
        email=email,
        phone_number=phone_number,
        user_type=user_type,
        blood_group=blood_group,
        age=age,
        gender=gender,
        verification_status=verification_status,
        hospital_verification_status=hospital_verification_status,
        hospital_license=hospital_license,
        latitude=latitude,
        longitude=longitude,
        registered_address=registered_address
    )

    new_user.set_password(data['password'])

    new_user.verified_email = False
    new_user.verified_mobile = False

    db.session.add(new_user)
    db.session.flush()
    print("[AUTH REGISTER] User creation: PASS")

    # Create donor profile
    if user_type == 'Donor':

        new_profile = DonorProfile(
            user_id=new_user.id,
            blood_group=blood_group or 'O+',
            age=age or 25,
            gender=gender or 'Male',
            is_available_today=False,
            today_availability=False,
            eligibility_status='ELIGIBLE',
            latitude=latitude,
            longitude=longitude
        )

        db.session.add(new_profile)

    # Send email OTP if email is provided
    email_otp_msg = None
    if email:
        try:
            email_otp_sent = send_generated_otp(email, new_user.name)
            if email_otp_sent:
                email_otp_msg = "Email verification OTP sent successfully."
                print("[AUTH REGISTER] OTP: PASS")
            else:
                email_otp_msg = "Failed to send email OTP. Please verify email settings."
                print("[AUTH REGISTER] OTP: FAIL (Failed to send)")
        except Exception as e:
            email_otp_msg = f"Failed to send email OTP: {str(e)}"
            print(f"[EMAIL OTP ERROR] {e}")
            print("[AUTH REGISTER] OTP: FAIL (Exception)")

    db.session.commit()

    resp_message = 'User registered successfully.'
    if email_otp_msg:
        resp_message += f' {email_otp_msg}'

    print(f"[AUTH REGISTER] Response: 201 Created")
    return jsonify({
        'message': resp_message,
        'user': new_user.to_dict(),
        'requires_verification': True
    }), 201


# ============================================================
# VERIFY PHONE OTP
# ============================================================

@auth_bp.route('/verify-otp', methods=['POST'])
def verify_otp():

    data = request.get_json()

    phone_number = data.get('phone_number')
    otp_code = data.get('otp_code')

    if not phone_number or not otp_code:
        return jsonify({
            'message': 'Missing phone number or OTP code'
        }), 400

    otp_record = OTPVerificationLog.query.filter_by(
        phone_number=phone_number,
        otp_code=otp_code,
        is_used=False
    ).order_by(
        OTPVerificationLog.timestamp.desc()
    ).first()

    if not otp_record or otp_record.expires_at < datetime.datetime.now(datetime.UTC).replace(tzinfo=None):

        return jsonify({
            'message': 'Invalid or expired OTP'
        }), 400

    otp_record.is_used = True

    user = User.query.filter_by(
        phone_number=phone_number
    ).first()

    if not user:

        return jsonify({
            'message': 'User not found'
        }), 404

    user.verified_mobile = True

    # If email is already verified, verify complete account
    if user.verified_email:
        user.verification_status = 'Verified'
        user.verification_date = datetime.datetime.now(datetime.UTC).replace(tzinfo=None)

    db.session.add(
        VerificationLog(
            user_id=user.id,
            event_type='OTP_VERIFIED',
            details='Mobile verified via OTP'
        )
    )

    db.session.commit()

    return jsonify({
        'message': 'Mobile number verified successfully',
        'user': user.to_dict()
    }), 200


# ============================================================
# VERIFY EMAIL OTP
# ============================================================

@auth_bp.route('/verify-email-otp', methods=['POST'])
def verify_email_otp():

    data = request.get_json()

    email = data.get('email')
    otp_code = data.get('otp_code')

    if not email or not otp_code:
        return jsonify({
            'message': 'Missing email or OTP code'
        }), 400

    email = email.strip().lower()

    # Find user
    user = User.query.filter_by(
        email=email
    ).first()

    if not user:
        return jsonify({
            'message': 'User with this email was not found'
        }), 404

    # Check OTP using Brevo email OTP system
    try:
        otp_valid, reason = verify_email_otp_code(
            email,
            otp_code
        )
    except Exception as error:
        print('[EMAIL OTP ERROR]')
        print(error)
        return jsonify({
            'message': 'Unable to verify email OTP'
        }), 500

    if not otp_valid:
        return jsonify({
            'message': reason
        }), 400

    # Mark email verified and complete account verification
    user.verified_email = True
    user.verification_status = 'Verified'
    user.verification_date = datetime.datetime.now(datetime.UTC).replace(tzinfo=None)

    # Audit log
    db.session.add(
        VerificationLog(
            user_id=user.id,
            event_type='EMAIL_OTP_VERIFIED',
            details='Email and account verified using OTP'
        )
    )

    db.session.commit()

    # Generate JWT authorization token
    token = jwt.encode(
        {
            'user_id': user.id,
            'exp': datetime.datetime.now(datetime.UTC).replace(tzinfo=None)
                   + datetime.timedelta(days=30)
        },
        current_app.config['SECRET_KEY'],
        algorithm='HS256'
    )

    return jsonify({
        'message': 'Email verified successfully',
        'token': token,
        'user': user.to_dict()
    }), 200


# ============================================================
# SEND EMAIL OTP (FOR EXISTING USERS)
# ============================================================

@auth_bp.route('/send-email-otp', methods=['POST'])
def send_email_otp():

    data = request.get_json()

    if not data or not data.get('email'):
        return jsonify({
            'message': 'Missing email address'
        }), 400

    email = data.get('email').strip().lower()

    # Find user by email
    user = User.query.filter_by(
        email=email
    ).first()

    if not user:
        return jsonify({
            'message': 'User with this email was not found'
        }), 404

    # Generate and send a new OTP using Brevo
    try:
        success = send_generated_otp(user.email, user.name)
        if not success:
            return jsonify({
                'message': 'Failed to send email OTP. Please verify email settings.'
            }), 500
    except ValueError as e:
        return jsonify({
            'message': str(e)
        }), 500
    except Exception as error:
        print('[SEND EMAIL OTP ERROR]')
        print(error)
        return jsonify({
            'message': 'Unable to send email OTP due to internal server error.'
        }), 500

    return jsonify({
        'message': 'Email verification OTP sent successfully'
    }), 200


# ============================================================
# HOSPITAL VERIFICATION
# ============================================================

@auth_bp.route('/verify-hospital', methods=['POST'])
def verify_hospital():

    data = request.get_json()

    hospital_id = data.get('hospital_id')
    status = data.get('status', 'Verified')

    if not hospital_id:

        return jsonify({
            'message': 'Hospital ID is required'
        }), 400

    user = User.query.get(hospital_id)

    if not user or user.user_type != 'Hospital':

        return jsonify({
            'message': 'Hospital user not found'
        }), 404

    user.hospital_verification_status = status
    user.verification_date = datetime.datetime.now(datetime.UTC).replace(tzinfo=None)

    db.session.commit()

    return jsonify({
        'message': f'Hospital verification status updated to {status}',
        'user': user.to_dict()
    }), 200


# ============================================================
# LOGIN
# ============================================================

@auth_bp.route('/login', methods=['POST'])
def login():

    data = request.get_json()
    print("[AUTH LOGIN] Request received")

    identifier = data.get('phone_number')
    password = data.get('password')

    if not identifier or not password:
        print("[AUTH LOGIN] Validation: FAIL (Missing fields)")
        return jsonify({
            'message': 'Identifier and password are required'
        }), 400

    user = User.query.filter(
        (User.phone_number == identifier) |
        (User.email == identifier)
    ).first()

    if not user:
        print("[AUTH LOGIN] User lookup: NOT FOUND")
        return jsonify({
            'message': 'This phone number or email is not registered.'
        }), 404

    print("[AUTH LOGIN] User lookup: FOUND")

    if not user.check_password(password):
        print("[AUTH LOGIN] Password verification: FAIL")
        return jsonify({
            'message': 'Incorrect password. Please check your password and try again.'
        }), 401

    print("[AUTH LOGIN] Password verification: PASS")

    # Verification check for all user types
    if user.verification_status != 'Verified':
        if user.email:
            try:
                send_generated_otp(user.email, user.name)
            except Exception as e:
                print(f"[LOGIN OTP ERROR] {e}")

        print("[AUTH LOGIN] Verification status: UNVERIFIED (Redirecting to OTP)")
        return jsonify({
            'message': 'Account is inactive. Please complete Email OTP Verification.',
            'unverified': True,
            'phone_number': user.phone_number,
            'email': user.email
        }), 403

    print("[AUTH LOGIN] Verification status: VERIFIED")

    token = jwt.encode(
        {
            'user_id': user.id,
            'exp': datetime.datetime.now(datetime.UTC).replace(tzinfo=None)
                   + datetime.timedelta(days=30)
        },
        current_app.config['SECRET_KEY'],
        algorithm='HS256'
    )

    print("[AUTH LOGIN] Token creation: PASS")
    return jsonify({
        'token': token,
        'user': user.to_dict()
    }), 200


# ============================================================
# UPDATE PROFILE
# ============================================================

@auth_bp.route('/update', methods=['POST'])
@token_required
def update_profile(current_user):

    data = request.get_json()

    if 'blood_group' in data:
        current_user.blood_group = data['blood_group']

    if 'age' in data:
        current_user.age = data['age']

    if 'gender' in data:
        current_user.gender = data['gender']

    if 'preferred_language' in data:
        current_user.preferred_language = data['preferred_language']

    db.session.commit()

    return jsonify({
        'message': 'Profile updated',
        'user': current_user.to_dict()
    }), 200


# ============================================================
# GET CURRENT USER
# ============================================================

@auth_bp.route('/me', methods=['GET'])
@token_required
def get_me(current_user):

    return jsonify({
        'user': current_user.to_dict()
    }), 200


# ============================================================
# FORGOT PASSWORD
# ============================================================

@auth_bp.route('/forgot-password', methods=['POST'])
def forgot_password():

    data = request.get_json()
    print("[AUTH RESET] Email lookup request received")

    email = data.get('email')

    if not email:
        return jsonify({
            'message': 'Email address is required'
        }), 400

    email = email.strip().lower()

    user = User.query.filter_by(
        email=email
    ).first()

    if not user:
        print("[AUTH RESET] Email lookup: NOT FOUND")
        return jsonify({
            'message': 'User with this email was not found.'
        }), 404

    print("[AUTH RESET] Email lookup: FOUND")

    # Call Brevo email OTP service to send OTP to registered email
    success = send_generated_otp(user.email, user.name)
    if not success:
        print("[AUTH RESET] OTP request: FAIL")
        return jsonify({
            'message': 'Failed to send OTP to your email. Please try again.'
        }), 500

    print("[AUTH RESET] OTP request: PASS")
    return jsonify({
        'message': 'Password reset OTP sent successfully to your email.'
    }), 200


# ============================================================
# RESET PASSWORD
# ============================================================

@auth_bp.route('/reset-password', methods=['POST'])
def reset_password():

    data = request.get_json()

    email = data.get('email')
    otp_code = data.get('otp_code')
    new_password = data.get('new_password')

    if not email or not otp_code or not new_password:

        return jsonify({
            'message': 'Missing email, OTP code, or new password'
        }), 400

    email = email.strip().lower()

    # Verify the OTP using existing email OTP mechanism
    try:
        otp_valid, reason = verify_email_otp_code(email, otp_code)
    except Exception as e:
        print(f"[RESET PASSWORD ERROR] {e}")
        return jsonify({
            'message': 'Unable to verify OTP'
        }), 500

    if not otp_valid:
        return jsonify({
            'message': reason
        }), 400

    user = User.query.filter_by(
        email=email
    ).first()

    if not user:

        return jsonify({
            'message': 'User not found'
        }), 404

    user.set_password(new_password)

    db.session.add(
        VerificationLog(
            user_id=user.id,
            event_type='PASSWORD_RESET',
            details='Password reset via Email OTP'
        )
    )

    db.session.commit()

    return jsonify({
        'message': 'Password reset successful'
    }), 200


# ============================================================
# REFRESH TOKEN
# ============================================================

@auth_bp.route('/refresh-token', methods=['POST'])
@token_required
def refresh_token(current_user):

    token = jwt.encode(
        {
            'user_id': current_user.id,
            'exp': datetime.datetime.now(datetime.UTC).replace(tzinfo=None)
                   + datetime.timedelta(days=30)
        },
        current_app.config['SECRET_KEY'],
        algorithm='HS256'
    )

    return jsonify({
        'token': token,
        'user': current_user.to_dict()
    }), 200