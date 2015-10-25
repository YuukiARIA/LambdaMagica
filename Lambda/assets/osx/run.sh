#!/bin/sh
CD=$(cd `dirname $0`; pwd)
APP_ROOT=$(cd ${CD}/../../..; pwd)
java \
  -Xdock:name="LambdaMagica"         \
  -Dapple.laf.useScreenMenuBar=true  \
  -Duser.dir="${APP_ROOT}"           \
  -jar ${CD}/lm.jar &
