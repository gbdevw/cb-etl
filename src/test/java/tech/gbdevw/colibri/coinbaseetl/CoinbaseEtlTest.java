package tech.gbdevw.colibri.coinbaseetl;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CoinbaseEtlTest {

    private final static Logger LOG = LoggerFactory.getLogger(CoinbaseEtlTest.class.getSimpleName());

    @Test
    public void testWebsocketStartup() throws Exception {

        // Wait 5 sec
        LOG.info("Start application live test");
        Thread.sleep(5000);

        // Check if services are OK
        LOG.info("Test application health");
        given().when().get("/health").then().statusCode(200);
        LOG.info("SUCCESS");
    }
}