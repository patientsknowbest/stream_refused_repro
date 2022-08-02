# stream refused repro

Repro for a bug we've seen with okhttp and ingress-nginx

Start nginx:
```bash
nginx -c  $(pwd)/nginx.conf -g "daemon off;"
```
- Compile & run the java app in another terminal
```bash
mvn package 
java -jar target/my-app-1-jar-with-dependencies.jar
```
- You might need to stop and restart the app once to make it reproduce the error:
```
okhttp3.internal.http2.StreamResetException: stream was reset: REFUSED_STREAM
	at okhttp3.internal.http2.Http2Stream.takeHeaders(Http2Stream.kt:148)
...
```