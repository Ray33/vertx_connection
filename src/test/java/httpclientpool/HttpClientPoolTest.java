package httpclientpool;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.Delay;
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
public class HttpClientPoolTest{

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




//    Connection timeout is 2 seconds, response from server is 3 seconds.
    //Expected: 
    @Test
    public void withDelayTest(TestContext context) throws Exception {
    	initVertx(context);
    	//Set a response with 3 seconds delay
        HttpResponse hcDelayResponse = HttpResponse.response().withBody(HC_RESPONCSE_STRING)
                .withStatusCode(200)
                .withDelay(new Delay(TimeUnit.SECONDS, 3));
        // setting the mock server reply
        mockServer.when(hcRequest).respond(hcDelayResponse);

        AtomicInteger count = new AtomicInteger();
        for (int i = 1; i <= 3; i++) {
            final int t = i;
            try {
                Async async = context.async();
                System.out.println("Request (#"+t+")" );
                getClient().get("/hc", response -> {
                	System.out.println("RESPONSE (#"+t+") is OK" );
                }).exceptionHandler(ex -> {
                	System.out.println("EXCEPTION_"+t +" --->"+ex.getLocalizedMessage() + " == "+ex.getClass());
                    
                    async.complete();
                    
                }).end();

            } catch (Throwable ex) {
                System.err.println("GOT AN EXCEPTION ");
                ex.printStackTrace();
            }
        }
        
        System.out.println("waiting for 10 seconds before next request" );
        Thread.sleep(10000);
        
        hcDelayResponse = HttpResponse.response().withBody(HC_RESPONCSE_STRING)
                .withStatusCode(200)
                .withDelay(new Delay(TimeUnit.SECONDS, 0));
        System.out.println("reset the mock server to have 0 delay" );
        mockServer.when(hcRequest).respond(hcDelayResponse);
        Async async = context.async();
        System.out.println("Sending a request now should be fine" );
        getClient().get("/hc", response -> {
        	System.out.println("RESPONSE is OK !" );
        }).exceptionHandler(ex -> {
        	System.out.println("This exception should not occur: EXCEPTION: --->"+ex.getLocalizedMessage() + " == "+ex.getClass());
            
            async.complete();
            
        }).end();
    }

}