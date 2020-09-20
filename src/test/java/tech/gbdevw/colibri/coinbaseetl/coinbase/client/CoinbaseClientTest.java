package tech.gbdevw.colibri.coinbaseetl.coinbase.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CoinbaseClientTest {

    private final static Logger LOG = LoggerFactory.getLogger(CoinbaseClientTest.class.getSimpleName());

    @Inject
    @RestClient
    CoinbaseClient client;
    
    @Test
    public void testGetLatestTrades100 () {

        LOG.info("testGetLatestTrades100 - START");

        String product = "btc-usd";
        int limit = 100;

        // Get the 100 latest trades
        CoinbaseTrade [] trades = this.client.getLatestTrades(product, limit);
        assertEquals(trades.length, limit);

        for (CoinbaseTrade coinbaseTrade : trades) {
            assertNotNull(coinbaseTrade.time);
            assertNotNull(coinbaseTrade.price);
            assertNotNull(coinbaseTrade.size);
            assertNotNull(coinbaseTrade.side);
            assertNotNull(coinbaseTrade.trade_id);
        }

        LOG.info("testGetLatestTrades100 - SUCCESS");
    }

    @Test
    public void testGetLatestTrades10 () {

        LOG.info("testGetLatestTrades10 - START");

        String product = "btc-usd";
        int limit = 10;

        // Get the 10 latest trades
        CoinbaseTrade [] trades = this.client.getLatestTrades(product, limit);
        assertEquals(trades.length, limit);

        for (CoinbaseTrade coinbaseTrade : trades) {
            assertNotNull(coinbaseTrade.time);
            assertNotNull(coinbaseTrade.price);
            assertNotNull(coinbaseTrade.size);
            assertNotNull(coinbaseTrade.side);
            assertNotNull(coinbaseTrade.trade_id);
        }

        LOG.info("testGetLatestTrades10 - SUCCESS");
    }

    /**
     * Test what happens when a limit over 100 is aksed (Coinbase limit)
     */
    @Test
    public void testGetLatestTrades101 () {

        LOG.info("testGetLatestTrades101 - START");

        String product = "btc-usd";
        int limit = 101;

        try {
            // Get the 101 latest trades
            CoinbaseTrade [] trades = this.client.getLatestTrades(product, limit);
            assertEquals(trades.length, limit);

            for (CoinbaseTrade coinbaseTrade : trades) {
                assertNotNull(coinbaseTrade.time);
                assertNotNull(coinbaseTrade.price);
                assertNotNull(coinbaseTrade.size);
                assertNotNull(coinbaseTrade.side);
                assertNotNull(coinbaseTrade.trade_id);
            }

            LOG.info("testGetLatestTrades101 - FAIL : An excepiton should have been thrown");
            fail("An excepiton should have been thrown");
        }
        catch(Exception ex) {
            LOG.info("Error while processing request : " + ex.getMessage());
            LOG.info("testGetLatestTrades101 - SUCCESS");
        }
    }

    /**
     * Test what happens when a limit of 0 is aksed (Invalid)
     */
    @Test
    public void testGetLatestTrades0 () {

        LOG.info("testGetLatestTrades0 - START");

        String product = "btc-usd";
        int limit = 0;

        try {
            // Get the 0 latest trades
            CoinbaseTrade [] trades = this.client.getLatestTrades(product, limit);
            assertEquals(trades.length, limit);

            for (CoinbaseTrade coinbaseTrade : trades) {
                assertNotNull(coinbaseTrade.time);
                assertNotNull(coinbaseTrade.price);
                assertNotNull(coinbaseTrade.size);
                assertNotNull(coinbaseTrade.side);
                assertNotNull(coinbaseTrade.trade_id);
            }

            LOG.info("testGetLatestTrades0 - FAIL : An excepiton should have been thrown");
            fail("An excepiton should have been thrown");
        }
        catch(Exception ex) {
            LOG.info("Error while processing request : " + ex.getMessage());
            LOG.info("testGetLatestTrades0 - SUCCESS");
        }
    }

    /**
     * Test what happens when a limit of -1 is aksed (Invalid)
     */
    @Test
    public void testGetLatestTradesN1 () {

        LOG.info("testGetLatestTradesN1 - START");

        String product = "btc-usd";
        int limit = -1;

        try {
            // Get the -1 latest trades
            CoinbaseTrade [] trades = this.client.getLatestTrades(product, limit);
            assertEquals(trades.length, limit);

            for (CoinbaseTrade coinbaseTrade : trades) {
                assertNotNull(coinbaseTrade.time);
                assertNotNull(coinbaseTrade.price);
                assertNotNull(coinbaseTrade.size);
                assertNotNull(coinbaseTrade.side);
                assertNotNull(coinbaseTrade.trade_id);
            }

            LOG.info("testGetLatestTradesN1 - FAIL : An excepiton should have been thrown");
            fail("An excepiton should have been thrown");
        }
        catch(Exception ex) {
            LOG.info("Error while processing request : " + ex.getMessage());
            LOG.info("testGetLatestTradesN1 - SUCCESS");
        }
    }
}