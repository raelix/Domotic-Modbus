#!/bin/bash

start() {
 java -jar /root/server/raspiServer.jar
}

start
while [ "$?" == "5" ] ;
do
echo "Server restarted command"
 start
done
