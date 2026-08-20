import os
import random
import requests

# Fallback tips of the day for donors
CURATED_TIPS = [
    "Stay hydrated: Drink at least 3-4 glasses of water before donating to maintain blood pressure.",
    "Boost your iron: Eat iron-rich foods like spinach, beans, or lean meat in the days leading up to your donation.",
    "Avoid fatty foods (like burgers or fries) on the day of donation, as they can interfere with blood testing.",
    "Remember to bring a valid ID and eat a light, healthy meal 2-3 hours before your appointment.",
    "After donating, keep the bandage on for at least 4 hours and avoid heavy lifting for the rest of the day.",
    "If you feel lightheaded after donating, lie down and raise your feet until the feeling passes.",
    "A single blood donation can save up to 3 lives. Thank you for making a difference today!",
    "Avoid alcohol for 24 hours before donating blood to ensure you are fully hydrated."
]

def get_groq_api_key():
    return os.environ.get("GROQ_API_KEY")

def generate_tip_of_the_day():
    api_key = get_groq_api_key()
    if not api_key:
        return random.choice(CURATED_TIPS)

    try:
        url = "https://api.groq.com/openai/v1/chat/completions"
        headers = {
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": "llama3-8b-8192",
            "messages": [
                {
                    "role": "user",
                    "content": "Generate a short, professional, and actionable blood donation health tip of the day for a registered donor. Keep it extremely brief (max 2 sentences, no headers, no introductory text like 'Here is your tip')."
                }
            ],
            "max_tokens": 80,
            "temperature": 0.7
        }
        response = requests.post(url, json=payload, headers=headers, timeout=3.0)
        if response.status_code == 200:
            data = response.json()
            tip = data['choices'][0]['message']['content'].strip()
            # Clean up any potential quotation marks wrapped around the AI's response
            if tip.startswith('"') and tip.endswith('"'):
                tip = tip[1:-1]
            return tip
    except Exception:
        pass

    return random.choice(CURATED_TIPS)

def generate_match_explanation(donor_name, blood_group, dist_km, duration_mins, response_rate, is_exact):
    match_type = "exact blood group match" if is_exact else "compatible Rh-safe blood match"
    resp_pct = int(response_rate * 100)
    
    api_key = get_groq_api_key()
    if not api_key:
        # Standard high-quality local rule fallback
        if is_exact:
            return f"{donor_name} is an exact match ({blood_group}), located {dist_km} km away ({duration_mins} min drive) with a {resp_pct}% response rate."
        else:
            return f"{donor_name} is a compatible Rh-safe match ({blood_group}), located {dist_km} km away ({duration_mins} min drive) with a {resp_pct}% response rate."

    try:
        url = "https://api.groq.com/openai/v1/chat/completions"
        headers = {
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json"
        }
        prompt = (
            f"Explain in exactly one short, professional sentence why donor {donor_name} (blood type {blood_group}) "
            f"is a great match for the patient. Mention they are {dist_km} km away ({duration_mins} min drive), "
            f"have a {resp_pct}% response rate, and are an {match_type}. Keep it under 25 words, reassuring, and do not use quotation marks."
        )
        payload = {
            "model": "llama3-8b-8192",
            "messages": [
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            "max_tokens": 80,
            "temperature": 0.5
        }
        response = requests.post(url, json=payload, headers=headers, timeout=3.0)
        if response.status_code == 200:
            data = response.json()
            explanation = data['choices'][0]['message']['content'].strip()
            if explanation.startswith('"') and explanation.endswith('"'):
                explanation = explanation[1:-1]
            return explanation
    except Exception:
        pass

    # Standard high-quality local rule fallback in case of API failure
    if is_exact:
        return f"{donor_name} is an exact match ({blood_group}), located {dist_km} km away ({duration_mins} min drive) with a {resp_pct}% response rate."
    else:
        return f"{donor_name} is a compatible Rh-safe match ({blood_group}), located {dist_km} km away ({duration_mins} min drive) with a {resp_pct}% response rate."
