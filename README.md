# Vert.x 3 pool exception Application

Unit tests which demonstrates Vert.x pooling issue.<br/>
When a HttpConnection is being closed, it can't be re-used in the pool correctly.<br/>

## Building

```
mvn clean package
```

## Testing

run via jUnit the class:<br/>
HttpClientPoolNoDelayTest - to see a valid behavior<br/>
HttpClientPoolTest - to see the error with the following output: <br/>
Request (#1)<br/>
Request (#2)<br/>
Request (#3)<br/>
EXCEPTION_3 --->Connection was closed == class io.vertx.core.VertxException <br/>
EXCEPTION_2 --->Connection was closed == class io.vertx.core.VertxException <br/>
EXCEPTION_1 --->Connection was closed == class io.vertx.core.VertxException <br/>
waiting for 10 seconds before next request<br/>
reset the mock server to have 0 delay<br/>
Sending a request now should be fine<br/>
This exception should not occur: EXCEPTION: --->Connection was closed == class io.vertx.core.VertxException<br/>


