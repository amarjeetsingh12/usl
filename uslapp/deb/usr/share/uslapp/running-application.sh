#!/bin/bash

sleep 30
exec 2>&1
exec /usr/lib/nagios/plugins/nsca_wrapper -H `hostname` -S 'USLRunningApplication'  -b /usr/sbin/send_nsca -c /etc/send_nsca.cfg -C "/usr/bin/python /usr/share/uslapp/running-application.py" >> /var/log/nsca.log 2>&1
