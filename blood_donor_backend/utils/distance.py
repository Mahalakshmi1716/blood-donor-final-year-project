import math
import requests

def haversine_distance(lat1, lon1, lat2, lon2):
    """
    Calculate the great circle distance between two points 
    on the earth (specified in decimal degrees)
    Returns distance in kilometers.
    """
    if None in (lat1, lon1, lat2, lon2):
        return float('inf')

    # Convert decimal degrees to radians 
    lon1, lat1, lon2, lat2 = map(math.radians, [lon1, lat1, lon2, lat2])

    # Haversine formula 
    dlon = lon2 - lon1 
    dlat = lat2 - lat1 
    a = math.sin(dlat/2)**2 + math.cos(lat1) * math.cos(lat2) * math.sin(dlon/2)**2
    c = 2 * math.asin(math.sqrt(a)) 
    r = 6371 # Radius of earth in kilometers
    return c * r

def get_osrm_route(lat1, lon1, lat2, lon2):
    """
    Query free public OSRM Routing API for driving distance and travel duration.
    Returns (distance_km, duration_mins) or None on failure.
    """
    if None in (lat1, lon1, lat2, lon2):
        return None
    try:
        url = f"http://router.project-osrm.org/route/v1/driving/{lon1},{lat1};{lon2},{lat2}?overview=false"
        response = requests.get(url, timeout=2.0)
        if response.status_code == 200:
            data = response.json()
            if data.get('routes'):
                route = data['routes'][0]
                distance_km = route['distance'] / 1000.0
                duration_mins = route['duration'] / 60.0
                return round(distance_km, 2), round(duration_mins, 1)
    except Exception:
        pass
    return None
