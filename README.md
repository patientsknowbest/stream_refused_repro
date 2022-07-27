# stream refused repro

Repro for a bug we've seen in our aidbox setup

Steps:
- Get a license for aidbox https://aidbox.app and set the env var AIDBOX_LICENSE
- Bring up docker containers in a terminal 
```bash
cd scripts 
docker-compose up 
```
- Compile & run the java app in another terminal
```bash
mvn package 
java -jar target/my-app-1-jar-with-dependencies.jar
```
- You might need to stop and restart the app once to make it reproduce the error:
```
Caused by: okhttp3.internal.http2.StreamResetException: stream was reset: REFUSED_STREAM
	at okhttp3.internal.http2.Http2Stream.takeResponseHeaders(Http2Stream.java:153)
...
```

Through some experimentation, this is almost certainly a problematic interaction with the nginx 
reverse proxy configuration / OkHttp. If the harness is configured to talk direct to aidbox
then no stream reset issues (it doesn't crash at all).

It might be related to http2, if OkHttp is restricted to http/1.1 then a different failure mode
is observed.