import json
import os

import functions_framework
from google.cloud import run_v2

from budget import read_notification, service_paths, should_trip

INTERNAL_ONLY = run_v2.IngressTraffic.INGRESS_TRAFFIC_INTERNAL_ONLY


def log(severity, message, **fields):
    print(json.dumps({"severity": severity, "message": message, **fields}))


def close_ingress(client, path):
    service = client.get_service(name=path)
    if service.ingress == INTERNAL_ONLY:
        return False
    service.ingress = INTERNAL_ONLY
    client.update_service(service=service)
    return True


@functions_framework.cloud_event
def guard(cloud_event):
    cost, currency = read_notification(cloud_event.data["message"]["data"])
    limit = float(os.environ["COST_LIMIT"])
    if not should_trip(cost, limit):
        log("INFO", "Cost below limit", cost=cost, limit=limit, currency=currency)
        return
    client = run_v2.ServicesClient()
    paths = service_paths(os.environ["PROJECT_ID"], os.environ["REGION"], os.environ["SERVICES"])
    for path in paths:
        if close_ingress(client, path):
            log("WARNING", "Public ingress closed", service=path, cost=cost, limit=limit, currency=currency)
