from models import DonorProfile, User
from extensions import db
from utils.distance import haversine_distance, get_osrm_route
from datetime import datetime, UTC, timedelta
from services.groq_service import generate_match_explanation

# Blood group compatibility mapping
COMPATIBILITY = {
    'A+': ['A+', 'A-', 'O+', 'O-'],
    'O+': ['O+', 'O-'],
    'B+': ['B+', 'B-', 'O+', 'O-'],
    'AB+': ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'],
    'A-': ['A-', 'O-'],
    'O-': ['O-'],
    'B-': ['B-', 'O-'],
    'AB-': ['AB-', 'A-', 'B-', 'O-']
}

def is_donor_blocked(last_donation_date):
    if not last_donation_date:
        return False
    days_since = (datetime.now(UTC).replace(tzinfo=None) - last_donation_date).days
    return days_since < 90

def score_and_sort_donors(required_blood_group, patient_lat, patient_lon, exclude_user_id=None, urgency="High", obfuscate_locations=True):
    # Enforce case-insensitive and clean group naming
    required_blood_group = required_blood_group.strip().upper()
    compatible_types = COMPATIBILITY.get(required_blood_group, [])
    if not compatible_types:
        return []

    # Rule Engine Filtering: Only ELIGIBLE and VERIFIED donors can participate
    query = DonorProfile.query.join(User).filter(
        DonorProfile.blood_group.in_(compatible_types),
        DonorProfile.eligibility_status == 'ELIGIBLE',
        User.verification_status == 'Verified',
        User.is_mock == False
    )

    if exclude_user_id is not None:
        query = query.filter(DonorProfile.user_id != exclude_user_id)

    eligible_donors = query.all()
    scored_donors = []

    # Dynamic distance radius based on urgency
    max_radius_km = 15.0
    if urgency == "Low":
        max_radius_km = 5.0
    elif urgency == "Critical":
        max_radius_km = 50.0

    import random

    for profile in eligible_donors:
        # Step 1: Pre-filter by straight line distance
        haversine_dist = haversine_distance(patient_lat, patient_lon, profile.latitude, profile.longitude)
        if haversine_dist > max_radius_km:
            continue

        # Step 2: Try to get actual road distance and driving duration from OSRM
        osrm_data = get_osrm_route(patient_lat, patient_lon, profile.latitude, profile.longitude)
        
        dist_km = haversine_dist
        duration_mins = haversine_dist * 2.0  # estimate: 2 mins per km if OSRM fails
        
        if osrm_data is not None:
            dist_km, duration_mins = osrm_data
            
        # ---  Scoring Heuristics (Total 100 points) ---
        
        # 1. Blood Match (Max 40 points)
        exact_match = (profile.blood_group == required_blood_group)
        compatible_match = not exact_match
        blood_group_score = 40 if exact_match else 20

        # 2. Distance Score (Max 25 points)
        distance_score = 5
        if dist_km < 2.0:
            distance_score = 25
        elif dist_km < 5.0:
            distance_score = 20
        elif dist_km < 10.0:
            distance_score = 15
        elif dist_km < 25.0:
            distance_score = 10

        # 3. Availability Score (Max 15 points)
        availability_score = 15 if profile.today_availability else 0

        # 4. Eligibility Score (Max 10 points)
        eligibility_score = 10 if profile.eligibility_status == 'ELIGIBLE' else 0

        # 5. Response History Score (Max 5 points)
        response_score = int((profile.response_rate or 0.95) * 5)

        # 6. Verification Score (Max 5 points)
        verification_score = 5 if (profile.user and profile.user.verification_status == 'Verified') else 0

        # Total Match Score (0 - 100)
        match_score = blood_group_score + distance_score + availability_score + eligibility_score + response_score + verification_score

        # ---  Recommendation Layer Predictions ---
        
        # Predict Response Probability
        resp_prob = profile.response_rate or 0.95
        if profile.response_time_average > 0:
            # Degrade probability for longer average response times
            resp_prob *= max(0.4, 1.0 - (profile.response_time_average / 180.0))
        predicted_response_probability = round(max(0.0, min(resp_prob, 1.0)), 2)

        # Predict Donor Acceptance Likelihood
        total_received = profile.total_requests_received or 0
        total_accepted = profile.total_requests_accepted or 0
        acc_rate = 0.95
        if total_received > 0:
            acc_rate = total_accepted / float(total_received)
        # Apply penalty for cancellations
        acc_rate *= max(0.4, 1.0 - (profile.cancellation_count or 0) * 0.1)
        predicted_acceptance_probability = round(max(0.0, min(acc_rate, 1.0)), 2)

        # Predict Availability Score Pattern
        predicted_availability_score = round(0.90 if profile.today_availability else 0.25, 2)

        #  Confidence Score (Combination of trust, match score, and predictions)
        trust_val = (profile.trust_score_computed or 75) / 100.0
        ai_conf = (match_score / 100.0) * 0.5 + trust_val * 0.3 + predicted_response_probability * 0.2
        ai_confidence_score = round(max(0.0, min(ai_conf, 1.0)), 2)

        # Match reason description
        match_reason = f"Blood Group Match: {blood_group_score}/40, Distance: {distance_score}/25, Availability: {availability_score}/15, Verification: {verification_score}/5.  Confidence: {int(ai_confidence_score * 100)}%."

        # Generate  match explanation via Groq or fallback
        explanation = generate_match_explanation(
            donor_name=profile.user.name,
            blood_group=profile.blood_group,
            dist_km=dist_km,
            duration_mins=duration_mins,
            response_rate=profile.response_rate or 0.95,
            is_exact=exact_match
        )

        # Privacy System: Obfuscate donor coordinates by adding a random offset before alert acceptance
        lat_to_use = profile.latitude
        lon_to_use = profile.longitude
        if obfuscate_locations and profile.latitude and profile.longitude:
            # Offset by ~300 to 500 meters randomly to hide exact address
            lat_to_use += random.uniform(-0.003, 0.003)
            lon_to_use += random.uniform(-0.003, 0.003)

        donor_data = {
            'donor_id': profile.user_id,
            'name': profile.user.name,
            'blood_group': profile.blood_group,
            'phone_number': profile.user.phone_number,
            'health_score': profile.health_score,
            'trust_score': profile.trust_score_computed,
            'response_rate': profile.response_rate or 0.95,
            'exact_match': exact_match,
            'compatible_match': compatible_match,
            'final_score': min(max(match_score, 0), 100),
            'match_score': min(max(match_score, 0), 100),
            'blood_group_score': blood_group_score,
            'distance_score': distance_score,
            'availability_score': availability_score,
            'response_score': response_score,
            'eligibility_score': eligibility_score,
            'verification_score': verification_score,
            'predicted_response_probability': predicted_response_probability,
            'predicted_acceptance_probability': predicted_acceptance_probability,
            'predicted_availability_score': predicted_availability_score,
            'ai_confidence_score': ai_confidence_score,
            'match_reason': match_reason,
            'distance_km': round(dist_km, 2),
            'duration_mins': round(duration_mins, 1),
            'match_explanation': explanation,
            'latitude': lat_to_use,
            'longitude': lon_to_use
        }
        
        scored_donors.append(donor_data)

    # Sort by  confidence and match score
    scored_donors.sort(key=lambda x: (x['ai_confidence_score'], x['match_score']), reverse=True)
    return scored_donors
