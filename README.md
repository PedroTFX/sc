tarefas:

gerar chaves do client e server com a keytool do java
	usar keutool do java apra gerar chaves
	ler secret key d keystroe e pulbic key do truststore
	gerar ligacao com o ssl e a chave

# Keystore

Uma keystore é um ficheiro protegido por password que contém chaves (públicas ou privadas).

Cada entidade (servidor, cliente1, cliente2, etc) vai precisar de uma keystore com a sua chave privada:
keytool -genkeypair -alias <keyName> -keyalg RSA -keysize 2048 storetype JCEKS -keystore <keystoreFilename>

keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -storetype JCEKS -keystore server.keystore
keytool -genkeypair -alias joao -keyalg RSA -keysize 2048 -storetype JCEKS -keystore joao.keystore
keytool -genkeypair -alias cristiano -keyalg RSA -keysize 2048 -storetype JCEKS -keystore cristiano.keystore
keytool -genkeypair -alias mario -keyalg RSA -keysize 2048 -storetype JCEKS -keystore mario.keystore

Cada um destes comandos vai gerar uma keystore (protegida por password) com uma chave privada lá dentro.
Podemos ver as chaves dentro de uma keystore com este comando:
keytool -list -storetype JCEKS -keystore <keystore filename>

keytool -list -storetype JCEKS -keystore server.keystore
keytool -list -storetype JCEKS -keystore joao.keystore
keytool -list -storetype JCEKS -keystore cristiano.keystore
keytool -list -storetype JCEKS -keystore  mario.keystore

Depois, temos que extrair/gerar chaves públicas a partir de cada uma daquelas chaves privadas:
keytool -exportcert -alias <keyName> -storetype JCEKS -keystore <keystoreFilename> -file <publicCertificateFilename>

keytool -exportcert -alias server -storetype JCEKS -keystore server.keystore -file serverRSApub.cer
keytool -exportcert -alias joao -storetype JCEKS -keystore joao.keystore -file joaoRSApub.cer
keytool -exportcert -alias cristiano -storetype JCEKS -keystore cristiano.keystore -file cristianoRSApub.cer
keytool -exportcert -alias mario -storetype JCEKS -keystore mario.keystore -file marioRSApub.cer

Finalmente, temos que colocar todas estas chaves públicas numa "truststore", que não é nada mais do que uma keystore com chaves públicas protegida por uma password partilhada entre as entidades que lhe têm que aceder:
keytool -import -alias <keyName> -keystore <keystoreFilename> -file <publicCertificateFilename>

keytool -import -alias server -keystore truststore.keystore -file serverRSApub.cer
keytool -import -alias joao -keystore truststore.keystore -file joaoRSApub.cer
keytool -import -alias cristiano -keystore truststore.keystore -file cristianoRSApub.cer
keytool -import -alias mario -keystore truststore.keystore -file marioRSApub.cer


Depois confirmamos que todas as chaves estão na truststore:

keytool -list -v -keystore truststore.keystore



tentar abrir o ssl socket sem trust store

sacar a chave privada da keystore e a publica do certificado publico do client

fazer load quando for falar com outros clientes

perguntar sobre talk, sacamos a chave publica, ciframos e fazermos um pedido de tallk normal?



# TODO
- Acabámos de adicionar a extensão à DB para poder saber que ficheiro ler: porto.jpg, porto.png, porto.jpeg?
- Agora temos que continuar a fazer o "view wine"/"list wine" e temos que usar a extensão que vem da DB para saber que ficheiro ler do disco.
- Continuar com as outras funções


ver a chave publica do client da truststore
fazer as coisas em cima do exemplo
