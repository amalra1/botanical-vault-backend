import base64
import json

COST_FIELD = "costAmount"
CURRENCY_FIELD = "currencyCode"


def read_notification(encoded_data):
    payload = json.loads(base64.b64decode(encoded_data).decode("utf-8"))
    return float(payload.get(COST_FIELD, 0)), payload.get(CURRENCY_FIELD)


def should_trip(cost_amount, cost_limit):
    return cost_amount >= cost_limit


def service_paths(project, region, services):
    return [
        f"projects/{project}/locations/{region}/services/{name.strip()}"
        for name in services.split(",")
        if name.strip()
    ]
