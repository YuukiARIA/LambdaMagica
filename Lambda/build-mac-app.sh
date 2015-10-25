#!/bin/sh

CD=$(cd `dirname $0`; pwd)

JAR=${CD}/lambda-magica/lm.jar
if [ ! -e ${JAR} ]; then
  echo "Error: lm.jar not found. Run 'ant jar' first."
  exit 1
fi

OUTPUT_DIR=${CD}/product
APP_ROOT=${OUTPUT_DIR}/LambdaMagica.app

if [ -e ${APP_ROOT} ]; then
  echo "Deleting existing ${APP_ROOT}"
  rm -r ${APP_ROOT}
fi

CONTENTS=${APP_ROOT}/Contents
EXECUTABLE=${CONTENTS}/MacOS/run.sh

mkdir -p ${CONTENTS}/MacOS ${CONTENTS}/Resources

cp assets/osx/run.sh ${EXECUTABLE}
chmod +x ${EXECUTABLE}

cp ${JAR} ${CONTENTS}/MacOS
cp assets/osx/Info.plist ${CONTENTS}
