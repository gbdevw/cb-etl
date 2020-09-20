package tech.gbdevw.colibri.coinbaseetl.coinbase.websocket.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test class for CoinbaseWebsocketClient 
 */
public class CoinbaseWSSubscriptionHelperTest {

    private final static Logger LOG = LoggerFactory.getLogger(CoinbaseWSSubscriptionHelperTest.class.getSimpleName());

    /**
     * Test of method testBuildSubscribeMessage.
     * 
     * The method test that the SUBSCRIBE request contains onyl the data required
     * by configuration.
     */
    @Test
    public void testBuildSubscribeMessage () {

        LOG.info("testBuildSubscribeMessage - START");

        try {

            // Test parameters
            String [] channels = new String[] {"match","ticker","heartbeat"};
            String [] products = new String[] {"BTC-USD","ETH-EUR"};
            
            // Build subscribe message
            JsonObject msg = CoinbaseWSubscriptionHelper.buildSubscribeMessage(products, channels);

            // Verify message type
            assertEquals(msg.get("type").getAsString(), CoinbaseWSMsgTypes.SUBSCRIBE);

            // Parse channels from message
            JsonArray cs = msg.get("channels").getAsJsonArray();
            Iterator<JsonElement> channelIterator = cs.iterator();
            String [] actual = new String [cs.size()];
            for(int c0 = 0; channelIterator.hasNext(); c0++) {
                actual[c0] = channelIterator.next().getAsString();
            }
            Arrays.sort(channels);
            Arrays.sort(actual);

            // Verify channels
            assertArrayEquals(channels, actual);

            // Parse products from message
            JsonArray ps = msg.get("product_ids").getAsJsonArray();
            Iterator<JsonElement> productIterator = ps.iterator();
            actual = new String [ps.size()];
            for(int c1 = 0; productIterator.hasNext(); c1++) {
                actual[c1] = productIterator.next().getAsString();
            }
            Arrays.sort(products);
            Arrays.sort(actual);

            // Verify products
            assertArrayEquals(products, actual);

            LOG.info("testBuildSubscribeMessage - SUCCESS");
        }
        catch(Exception ex) {
            LOG.error("An unexpected error occured", ex);
            fail("An unexpected error occured");
        }
    }

    /**
     * test of method checkSubscriptionMessage.
     * 
     * The test checks if the data contained in the subscription response strictly match the
     * required configuration.
     */
    @Test
    public void testCheckSubscriptionMessageOK () {

        LOG.info("testCheckSubscriptionMessageOK - START");

        try {

            // Test parameters
            String [] channels = new String[] {"match","ticker"};
            String [] products = new String[] {"BTC-USD","ETH-EUR"};

            // Parse test message
            InputStream in = this.getClass().getResourceAsStream("/messages/CoinbaseSubscriptionMessage.json");
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            JsonObject subscriptions = JsonParser.parseReader(reader).getAsJsonObject();
            boolean result = CoinbaseWSubscriptionHelper.checkSubscriptionMessage(subscriptions, products, channels);
            assertTrue(result);

            LOG.info("testCheckSubscriptionMessageOK - SUCESS");
        }
        catch(Exception ex) {
            LOG.info("testCheckSubscriptionMessageOK - FAILURE");
            fail(ex.getMessage());
        }
    }

    /**
     * test of method checkSubscriptionMessage.
     * 
     * The test checks that the data contained in the subscription response do not match the
     * required configuration (different product from those required).
     */
    @Test
    public void testCheckSubscriptionMessageKODifferentProduct () {

        LOG.info("testCheckSubscriptionMessageKODifferentProduct - START");
        
        try {

            // Test parameters
            String [] channels = new String[] {"match","ticker"};
            String [] products = new String[] {"BTC-USD","ETH-EUR"};

            // Parse test message
            InputStream in = this.getClass().getResourceAsStream("/messages/CoinbaseSubscriptionMessageDifferentProducts.json");
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            JsonObject subscriptions = JsonParser.parseReader(reader).getAsJsonObject();
            CoinbaseWSubscriptionHelper.checkSubscriptionMessage(subscriptions, products, channels);

            LOG.info("testCheckSubscriptionMessageKODifferentProduct - FAILURE");
            fail("checkSubscriptionMessage should throw an exception");
        }
        catch(Exception ex) {
            LOG.info("testCheckSubscriptionMessageKODifferentProduct - SUCCESS");
        }
    }

    /**
     * test of method checkSubscriptionMessage.
     * 
     * The test checks that the data contained in the subscription response do not match the
     * required configuration (different channels from those required).
     */
    @Test
    public void testCheckSubscriptionMessageKODifferentChannels () {

        LOG.info("testCheckSubscriptionMessageKODifferentChannels - START");
    
        try {

            // Test parameters
            String [] channels = new String[] {"match","ticker"};
            String [] products = new String[] {"BTC-USD","ETH-EUR"};

            // Parse test message
            InputStream in = this.getClass().getResourceAsStream("/messages/CoinbaseSubscriptionMessageDifferentChannel.json");
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            JsonObject subscriptions = JsonParser.parseReader(reader).getAsJsonObject();
            CoinbaseWSubscriptionHelper.checkSubscriptionMessage(subscriptions, products, channels);

            LOG.info("testCheckSubscriptionMessageKODifferentChannels - FAILURE");
            fail("checkSubscriptionMessage should throw an exception");
        }
        catch(Exception ex) {
            LOG.info("testCheckSubscriptionMessageKODifferentChannels - SUCCESS");
        }
    }

    /**
     * test of method checkSubscriptionMessage.
     * 
     * The test checks that the data contained in the subscription response do not match the
     * required configuration (missing channels from those required).
     */
    @Test
    public void testCheckSubscriptionMessageKOMissingChannels () {

        LOG.info("testCheckSubscriptionMessageKOMissingChannels - START");
        
        try {

            // Test parameters
            String [] channels = new String[] {"match","ticker"};
            String [] products = new String[] {"BTC-USD","ETH-EUR"};

            // Parse test message
            InputStream in = this.getClass().getResourceAsStream("/messages/CoinbaseSubscriptionMessageMissingChannel.json");
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            JsonObject subscriptions = JsonParser.parseReader(reader).getAsJsonObject();
            CoinbaseWSubscriptionHelper.checkSubscriptionMessage(subscriptions, products, channels);

            LOG.info("testCheckSubscriptionMessageKOMissingChannels - FAILURE");
            fail("checkSubscriptionMessage should throw an exception");
        }
        catch(Exception ex) {
            LOG.info("testCheckSubscriptionMessageKOMissingChannels - SUCCESS");
        }
    }

    /**
     * test of method checkSubscriptionMessage.
     * 
     * The test checks that the data contained in the subscription response do not match the
     * required configuration (missing product from those required).
     */
    @Test
    public void testCheckSubscriptionMessageKOMissingProduct () {

        LOG.info("testCheckSubscriptionMessageKOMissingProduct - START");
        
        try {

            // Test parameters
            String [] channels = new String[] {"match","ticker"};
            String [] products = new String[] {"BTC-USD","ETH-EUR"};

            // Parse test message
            InputStream in = this.getClass().getResourceAsStream("/messages/CoinbaseSubscriptionMessageMissingProduct.json");
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            JsonObject subscriptions = JsonParser.parseReader(reader).getAsJsonObject();
            CoinbaseWSubscriptionHelper.checkSubscriptionMessage(subscriptions, products, channels);

            LOG.info("testCheckSubscriptionMessageKOMissingProduct - FAILURE");
            fail("checkSubscriptionMessage should throw an exception");
        }
        catch(Exception ex) {
            LOG.info("testCheckSubscriptionMessageKOMissingProduct - SUCCESS");
        }
    }
}