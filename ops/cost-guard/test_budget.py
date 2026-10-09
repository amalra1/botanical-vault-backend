import base64
import json
import unittest

from budget import read_notification, service_paths, should_trip


def encoded(payload):
    return base64.b64encode(json.dumps(payload).encode("utf-8"))


class BudgetTest(unittest.TestCase):
    def test_reads_cost_and_currency(self):
        self.assertEqual(read_notification(encoded({"costAmount": 10.5, "currencyCode": "BRL"})), (10.5, "BRL"))

    def test_missing_cost_counts_as_zero(self):
        self.assertEqual(read_notification(encoded({}))[0], 0.0)

    def test_trips_at_or_above_the_limit(self):
        self.assertTrue(should_trip(10, 10))
        self.assertTrue(should_trip(10.01, 10))
        self.assertFalse(should_trip(9.99, 10))

    def test_builds_service_paths(self):
        self.assertEqual(
            service_paths("p", "us-east1", "api, web ,"),
            ["projects/p/locations/us-east1/services/api", "projects/p/locations/us-east1/services/web"],
        )


if __name__ == "__main__":
    unittest.main()
