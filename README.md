# Vert.x 3 pool exception Application

Unit tests which demonstrates Vert.x pooling issue.
When a HttpConnection is being closed, it can't be used in the pool correctly.

## Building

```
mvn clean package
```

## Testing

run via jUnit the class:
HttpClientPoolNoDelayTest - to see a valid behavior
HttpClientPoolTest - to see the issue

