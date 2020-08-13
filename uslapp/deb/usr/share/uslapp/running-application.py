import requests

response = requests.get("<your-url-here>")
jsonResponse = response.json

masters = ["http://"+str(instances['primary_ip'])+":8080/metrics/applications/json/" for instances in jsonResponse]

def getApplicationListFromMaster():
    for master in masters:
        print "trying master with ip ", master
        try:
            r = requests.get(master)
            jsonResponse = r.json
            if len(jsonResponse["gauges"]) == 0:
                continue
            else:
                return jsonResponse["gauges"]
        except Exception as e:
            print "exception while connecting to master"
            continue
    print "All masters are down"
    exit(1)

applicationList = ['dev-usl-event-processor']
flag0 =False

applicationListFromMaster = getApplicationListFromMaster()

if applicationListFromMaster is None:
    print "All masters are down"
    exit(1)

if len(applicationListFromMaster) == 0:
    print "All masters are down"
    exit(1)

for application in applicationListFromMaster:
    if (applicationList[0] in application) and ("status" in application) and (applicationListFromMaster[application]["value"] == "RUNNING"):
        flag0 = True
        # print application,  "Job is running"

if flag0 == False:
    print "USL Spark Job not running"
    exit(1)
print "USL job is running"
exit(0)
