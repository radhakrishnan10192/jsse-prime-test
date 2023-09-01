## JSSE Test Utility

### Run Modes
* jmh-client-calls
* start-server
* client-lnp
* sslr

#### jmh-client-calls
Microbenchmark tests for the HTTPS client to server call.

**Sample command**
```shell
mvn spring-boot:run \
-Dspring-boot.run.jvmArguments="-Dtest.mode=jmh-client-calls \
-Djmh.threads=4 \
-Djmh.warmup.iterations=1 \
-Djmh.measurement.time.ms=20000 \
-Djmh.measurement.iterations=3 \
-Djmh.measurement.time.ms=60000" -Pmock-jsse
```

#### start-server
To start a test downstream server. Will be useful to test with different JDks in the client and server.

**Sample command**
```shell
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dtest.mode=start-server" -Pmock-jsse
```

#### client-lnp
Perform lnp on client HTTPS calls to server. 

**Sample command**
```shell
mvn spring-boot:run \
-Dspring-boot.run.jvmArguments="-Dtest.mode=client-lnp \
-Dconcurrent.users=4 \
-Dexecution.time.secs=1 \
-Dwarmup.time.secs=20000 \
-Ddelay.between.call.ms=100" -Pmock-jsse
```

#### sslr
Performs an SSL resumption test. 

**Sample command**
```shell
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dtest.mode=sslr -Dstart.embedded.server=true" -Pmock-jsse
```
**Sample output**
```shell
Session Resumed: true, Cipher suite: TLS_RSA_WITH_AES_128_CBC_SHA, Protocol: TLSv1
```



