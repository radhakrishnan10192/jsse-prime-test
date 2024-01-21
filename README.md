## Prime JSK JSSE Test Executor


**Sample command**
```shell
mvn spring-boot:run \
-Dspring-boot.run.jvmArguments="-Djmh.threads=4 \
-Djmh.warmup.iterations=1 \
-Djmh.measurement.time.ms=20000 \
-Djmh.measurement.iterations=3 \
-Djmh.measurement.time.ms=60000"
```

It will internally start's the netty server by default and executed the client calls