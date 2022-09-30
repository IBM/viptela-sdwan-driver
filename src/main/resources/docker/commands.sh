#!/bin/bash

KEYSTOREDIR=/var/lm/keystore
KEYSTORE=$KEYSTOREDIR/keystore.p12
CERTDIR=/var/viptelasdwandriver/certs
CERTKEY=$CERTDIR/tls.key
CERT=$CERTDIR/tls.crt

cd ${alm_sdwan_directory}
[ ! -f $KEYSTORE ] && openssl pkcs12 -export -inkey $CERTKEY -in $CERT -out $KEYSTORE -password pass:"${SERVER_SSL_KEY_STORE_PASSWORD}" -name "viptela-sdwan-driver"
java $JVM_OPTIONS -jar /data/viptela-sdwan-driver-@project.version@.jar