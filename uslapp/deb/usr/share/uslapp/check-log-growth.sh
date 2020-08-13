#!/usr/bin/env bash
while getopts ":l:w:c:t:" ARG; do
   case "$ARG" in
     l) logFile=$OPTARG;;
     w) warningLimit=$OPTARG;;
     c) criticalLimit=$OPTARG;;
     t) tmpFile=$OPTARG;;
   esac
done
if [[ -z ${logFile} ]]
then
    echo "logFile required";
    exit 2;
fi
if [[ -z ${warningLimit} ]]
then
    echo "warningLimit required";
    exit 2;
fi
if [[ -z ${criticalLimit} ]]
then
    echo "criticalLimit required";
    exit 2;
fi
if [[ -z ${tmpFile} ]]
then
    echo "tempFile required";
    exit 2;
fi
tmpFileName=`echo ${logFile}|sed 's/[/]/_/g'`;
if [ -f "${tmpFile}/${tmpFileName}" ]
then
	previousSize=$(cat ${tmpFile}/${tmpFileName});
else
    previousSize=0;
fi
currentSize=$(cat ${logFile}|wc -c)
diff=$((currentSize - previousSize));
echo ${currentSize}>"${tmpFile}/${tmpFileName}";
errorLines=$(tail -n 10 ${logFile});
if [[ ${diff} -gt ${criticalLimit} ]]
then
 echo "LOG GROWTH CRITICAL - ${logFile} grew by ${diff} bytes ${errorLines} | growth=$diff;$warningLimit;$criticalLimit";
 exit 2;
elif [[ ${diff} -gt ${warningLimit} ]]
then
 echo "LOG GROWTH WARNING - ${logFile} grew by ${diff} bytes ${errorLines} | growth=$diff;$warningLimit;$criticalLimit";
 exit 1;
else
 echo "LOG GROWTH OK";
 exit 0;
fi


