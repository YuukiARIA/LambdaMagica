#!/bin/sh

CD=$(cd `dirname $0`; pwd)

OUTPUT_DIR=${CD}/product
APP_ROOT=${OUTPUT_DIR}/LambdaMagica.app

CONTENTS=${APP_ROOT}/Contents
EXECUTABLE=${CONTENTS}/MacOS/run.sh

ant jar

mkdir -p ${CONTENTS}/MacOS ${CONTENTS}/Resources

cp assets/osx/run.sh ${EXECUTABLE}
chmod +x ${EXECUTABLE}

cp lambda-magica/lm.jar ${CONTENTS}/MacOS
cp assets/osx/Info.plist ${CONTENTS}
