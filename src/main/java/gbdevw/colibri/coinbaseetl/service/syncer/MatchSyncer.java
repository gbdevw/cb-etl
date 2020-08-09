package gbdevw.colibri.coinbaseetl.service.syncer;

import java.time.Instant;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gbdevw.colibri.coinbaseetl.coinbase.client.CoinbaseClient;
import gbdevw.colibri.coinbaseetl.coinbase.client.CoinbaseTrade;
import gbdevw.colibri.coinbaseetl.coinbase.websocket.utils.CoinbaseWsEventAddresses;
import gbdevw.colibri.coinbaseetl.configuration.CoinbaseProperties;
import gbdevw.colibri.domain.marketevent.MarketEvent.Match;
import gbdevw.colibri.domain.utils.Utils.Currency;
import gbdevw.colibri.domain.utils.Utils.Side;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;

/**
 * Class which fetch the latest trades at the application startup in order
 * to sync. the trade flow.
 */
@RegisterForReflection
@ApplicationScoped
@Startup
public class MatchSyncer {

    /**
     * Logger
     */
    private static Logger LOG = LoggerFactory.getLogger(MatchSyncer.class.getSimpleName());

    /**
     * Client to help using a REST API
     */
    private CoinbaseClient client;

    /**
     * Product supported
     */
    private String product;

    /**
     * Bus to produce Match messages
     */
    private EventBus bus;

    /**
     * Constructor
     * 
     * @param props Coinbase properties
     * @param client Coinbase API client
     * @param bus Bus used to produce Match events
     */
    @Inject
    public MatchSyncer (CoinbaseProperties props, @RestClient CoinbaseClient client, EventBus bus) {
        this.client = client;
        this.bus = bus;
        this.product = props.product;
    }

    /**
     * Fetch the latest trades when a LAST_MATCH event is received and publish a
     * MATCH event for each fetched trade.
     * 
     * @throws Exception
     */
    @ConsumeEvent(value = CoinbaseWsEventAddresses.websocketLastMatchEventAddress, blocking = true)
    public void fetchLatestTrades(Long lastMatchId) {

        try {
            LOG.info("Fetching latest trades");
            
            // Fetch latest trades using Coinbase Pro API
           CoinbaseTrade [] trades = this.client.getLatestTrades(this.product.toLowerCase(), 100);
               
           for(CoinbaseTrade trade : trades) {
                // Convert to match
                Match match = Match.newBuilder()
                .setTimestamp(Instant.parse(trade.time).toEpochMilli())
                .setId(Long.parseLong(trade.trade_id))
                .setUnit(Currency.valueOf(this.product.split("-")[0]))
                .setQuote(Currency.valueOf(this.product.split("-")[1]))
                .setSide(Side.valueOf(trade.side.toUpperCase()))
                .setPrice(Double.parseDouble(trade.price))
                .setSize(Double.parseDouble(trade.size))
                .build();

                // Publish match event
                this.bus.publish(CoinbaseWsEventAddresses.websocketMatchEventAddress, match);
                LOG.trace("Match " + match.getId() + " fetched and published");
           }
        }
        catch(Exception ex) 
        {
            LOG.error("Failed to fetch latest trades", ex);
        }
    }
}