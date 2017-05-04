#!/bin/bash
# This script will generate trust store certs for different environments

askEnvironment() {
    read -p "Which environment? \"dev\" or \"release\": [dev]" ENV
    ENV=${ENV:-dev}
}

askHost() {
    read -p "Enter address: [$HOST]" CERTHOST
    CERTHOST=${CERTHOST:-$HOST}
}

getBouncyCastle() {
    if [ ! -f bcprov-jdk15on-156.jar ]
    then
        wget -O bcprov-jdk15on-156.jar https://www.bouncycastle.org/download/bcprov-jdk15on-156.jar
    fi
}

generateCertificate() {
    getBouncyCastle
    openssl s_client -connect $CERTHOST:443 </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-OCEND CERTIFICATE-/p' > chatstore.cert
    keytool -import -noprompt -trustcacerts -alias $CERTHOST -file chatstore.cert -keystore chatkey.store -storepass whisper -storetype BKS -providerclass org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath bcprov-jdk15on-156.jar
}

moveCertificate() {
    MOVE_PATH="./app/src/debug/res/raw/"
    if [[ $ENV == "release" ]]
    then
        MOVE_PATH="./app/src/release/res/raw/"
    fi
    mv chatkey.store $MOVE_PATH
    rm chatstore.cert
}

while true; do
    askEnvironment

    if [[ $ENV == "dev" ]] || [[ $ENV == "release" ]]
    then
        HOST="token-chat-service-development.herokuapp.com"
        if [[ $ENV == "release" ]]
        then
            HOST="chat.service.tokenbrowser.com"
        fi

        askHost
        generateCertificate
        moveCertificate
        echo "Done"
        break
    fi
done

exit 0
