#!/bin/sh

###
#   Copyright 2013 Bruce Ashton
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
###

#chkconfig: 2345 80 20
#description: BUMSink mail sink

### BEGIN INIT INFO
# Provides:             bumsink
# Required-Start:       $local_fs $remote_fs $network $named
# Required-Stop:        $local_fs $remote_fs $network $named
# Default-Start:        2 3 4 5
# Default-Stop:         0 1 6
# Short-Description:    A mail sink SMTP and POP3 service 
### END INIT INFO


BASEFILE=$(readlink -f $0)
BASEDIR=$(dirname ${BASEFILE})
cd ${BASEDIR}

NAME=bumsink

JAVA=$(which java)
if [ -z ${JAVA} ]; then
    echo "No java in ${PATH}"
    exit 1
fi

JAR=${BASEDIR}/${NAME}.jar
JAVAOPTS="-Djava.awt.headless=true -Djava.net.preferIPv4Stack=true"
LOGFILE=${BASEDIR}/${NAME}.log
OPTS=${NAME}.properties
PIDFILE=${BASEDIR}/${NAME}.pid


check() {
    if [ -r "${PIDFILE}" ]; then
        if read pid < "${PIDFILE}" && ps -p "$pid" > /dev/null 2>&1; then
            return 0
        else
            return 1
        fi
    else
        return 3
    fi
}


start() {
    rc=1
    check
    if [ $? -ne 0 ]; then
        ${JAVA} ${JAVAOPTS} -jar ${JAR} ${OPTS} >> ${LOGFILE} 2>&1 &
        rc=$?
        echo $! > ${PIDFILE}
    else
    	echo "${NAME} is already running."
    	return 1
    fi
    if [ $rc -eq 0 ]; then
    	echo "${NAME} started."
    fi
    return $rc
}


stop() {
    rc=1
    check
    if [ $? -eq 0 ]; then
        kill $(cat ${PIDFILE}) && rm ${PIDFILE}
        rc=$?
    else
    	echo "${NAME} is not running."
    	return 1
    fi
    if [ $rc -eq 0 ]; then
    	echo "${NAME} stopped."
    fi
    return $rc
}


status() {
    check
    rc=$?
    if [ $rc -eq 0 ]; then
        echo "${NAME} is running."
    else
        echo "${NAME} is not running."
    fi
    return $rc
}


case "$1" in
    start)
        start
        rc=$?
        ;;
    stop)
        stop
        rc=$?
        ;;
    restart)
        stop
        start
        rc=$?
        ;;
    force-reload)
        stop
        start
        rc=$?
        ;;
    status)
        status
        rc=$?
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|force-reload|status}"
        rc=3
        ;;
esac

exit $rc
