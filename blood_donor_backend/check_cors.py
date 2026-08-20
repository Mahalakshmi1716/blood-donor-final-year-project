import requests

url = "http://127.0.0.1:5000/api/auth/me"
headers = {
    "Origin": "http://localhost:5173",
    "Access-Control-Request-Method": "GET",
    "Access-Control-Request-Headers": "authorization,content-type"
}

try:
    response = requests.options(url, headers=headers)
    print("OPTIONS status:", response.status_code)
    print("Response headers:")
    for k, v in response.headers.items():
        print(f"  {k}: {v}")
except Exception as e:
    print("Error:", e)
