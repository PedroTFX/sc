# comando que gerou a keystore do servidor
keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -keystore ./certs/keystore.server
keytool -exportcert -alias server -file ./certs/certServer.cer -keystore ./certs/keystore.server
keytool -importcert -alias server -file ./certs/certServer.cer -keystore ./certs/truststore.client

# comando que gerou a keystore do joao
keytool -genkeypair -alias joao -keyalg RSA -keysize 2048 -keystore ./certs/keystore.joao
keytool -exportcert -alias joao -file ./certs/certJoao.cer -keystore ./certs/keystore.joao
keytool -importcert -alias joao -file ./certs/certJoao.cer -keystore ./certs/truststore.client

# comando que gerou a keystore do cris
keytool -genkeypair -alias cris -keyalg RSA -keysize 2048 -keystore ./certs/keystore.cris
keytool -exportcert -alias cris -file ./certs/certCris.cer -keystore ./certs/keystore.cris
keytool -importcert -alias cris -file ./certs/certCris.cer -keystore ./certs/truststore.client

password

keytool -list -v -keystore cacerts
