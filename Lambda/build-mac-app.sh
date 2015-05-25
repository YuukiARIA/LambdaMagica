#!/bin/sh

APP_NAME="LambdaMagica.app"
CONTENTS=${APP_NAME}/Contents
EXECUTABLE=${CONTENTS}/MacOS/run.sh

ant jar

mkdir -p ${CONTENTS}/MacOS ${CONTENTS}/Resources

cat <<'EOF' > ${EXECUTABLE}
#!/bin/sh
DIR=`dirname $0`
java -Dapple.laf.useScreenMenuBar=true -jar $DIR/lm.jar &
EOF

chmod +x ${EXECUTABLE}

cp lambda-magica/lm.jar ${CONTENTS}/MacOS

cat <<EOF > ${APP_NAME}/Contents/Info.plist
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
  <dict>
    <key>CFBundleExecutable</key>
    <string>run.sh</string>
    <key>CFBundleIconFile</key>
    <string></string>
    <key>CFBundleIdentifier</key>
    <string></string>
    <key>CFBundleDisplayName</key>
    <string>LambdaMagica</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>LambdaMagica</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string></string>
    <key>CFBundleSignature</key>
    <string>????</string>
    <key>CFBundleVersion</key>
    <string>1</string>
    <key>NSHumanReadableCopyright</key>
    <string>Copyright (C) YuukiARIA</string>
  </dict>
</plist>
EOF
