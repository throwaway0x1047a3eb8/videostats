import sys
import requests
from datetime import datetime

now = int(str(datetime.timestamp(datetime.now())).replace('.', '')[:13])

# Change the timestamp to 55s ago to debug.
now = now - 55000

duration = float(sys.argv[1]) if len(sys.argv) > 1 else 10.5

r = requests.post(
    'http://127.0.0.1:4567/videos',
    json={"duration": duration, "timestamp": now}
)

