import requests

response = requests.get("<your-ip-here>")
jsonResponse = response.json

masters = ["http://"+str(instances['primary_ip'])+":8080/metrics/master/json/" for instances in jsonResponse]

flag = False
for master in masters:
    print "trying master with ip ", master
    try:
        r = requests.get(master)
        jsonResponse = r.json
        noOfAliveWorkers = jsonResponse["gauges"]["master.aliveWorkers"]["value"]
        if noOfAliveWorkers==0:
            continue
        if noOfAliveWorkers < 18:
            print "Spark Workers Count: " , noOfAliveWorkers, "is less than required count."
            exit(1)
        flag=True
        break
    except Exception as e:
        print "exception while connecting to master"

if flag==False:
    print "All masters are down"
    exit(1)
print "All Spark workes are running"
exit(0)
