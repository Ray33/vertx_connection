package httpclientpool;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class HttpClientPoolNoDelayTest {

    protected final static String HC_RESPONCSE_STRING = "Server is running";   
    
    protected HttpRequest hcRequest= HttpRequest.request().withPath("/hc").withMethod("GET");
    protected static JsonObject config = new JsonObject();
    protected static Vertx vertx;


    protected HttpResponse hcResponse = HttpResponse.response().withBody(HC_RESPONCSE_STRING).withStatusCode(200);

    private HttpClient getClient(){
        int idleTimeoutSecs = 2;  // TimeUnit.SECONDS
        int connectTimeoutMillis = 2000; // TimeUnit.MILLISECONDS

        return vertx.createHttpClient(new HttpClientOptions().setDefaultHost("localhost")
                .setDefaultPort(mockServerRule.getPort())
                .setIdleTimeout(idleTimeoutSecs)
                .setMaxPoolSize(2)
                .setMaxWaitQueueSize(0)
                .setConnectTimeout(connectTimeoutMillis)
        );
    }
    
    public static void initVertx(TestContext context) throws Exception {
        if (vertx==null){        	
            vertx = Vertx.vertx();
            DeploymentOptions options = new DeploymentOptions();
            config.put("host","localhost");
            config.put("port",9002);
            options.setConfig(config);
            
            vertx.deployVerticle(NubesClientVerticle.class.getName(),options,
                    context.asyncAssertSuccess());
        }
    }

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    private MockServerClient mockServer;

    
    // test with no delay will get successful response (see test in class HttpClientPoolTest)
    // Connection timeout is 2 seconds, response from server is immediate.
    @Test
    public void testNoDelay(TestContext context) throws Exception {
    	initVertx(context);
    	
    	//setting the mock server reply   	
        mockServer.when(hcRequest).respond(hcResponse);
        

        AtomicInteger count = new AtomicInteger();
        for (int i = 1; i <= 4; i++) {
            final int t = i;
            try {
                Async async = context.async();
                System.out.println("Request (#"+t+")" );//+ response
                getClient().get("/hc", response -> {
                    System.out.println("RESPONSE (#"+t+") is OK" );//+ response
                    async.complete();
                    
                }).exceptionHandler(ex -> {
                	System.out.println("EXCEPTION_"+t +" --->"+ex.getLocalizedMessage() + " == "+ex.getClass());
                   
                }).end();

            } catch (Throwable ex) {
                System.err.println("GOT_AN_EXCEPTION");
                ex.printStackTrace();
            }
        }

    }

}