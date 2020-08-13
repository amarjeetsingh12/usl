#!/bin/bash

sleep 30
exec 2>&1
exec /usr/lib/nagios/plugins/nsca_wrapper -H `hostname` -S 'USLSparkAliveWorkersCount'  -b /usr/sbin/send_nsca -c /etc/send_nsca.cfg -C "/usr/bin/python /usr/share/uslapp/spark-worker-counts.py" >> /var/log/nsca.log 2>&1
