package gbdevw.colibri.coinbaseetl.service.syncer;

import java.time.Instant;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gbdevw.colibri.coinbaseetl.coinbase.websocket.utils.CoinbaseWsEventAddresses;
import gbdevw.colibri.coinbaseetl.configuration.CoinbaseProperties;
import gbdevw.colibri.domain.marketevent.MarketEvent.Match;
import gbdevw.colibri.domain.utils.Utils.Currency;
import gbdevw.colibri.domain.utils.Utils.Side;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;

/**
 * Class which fetch the latest trades at the application startup in order
 * to sync. the trade flow.
 */
@RegisterForReflection
@ApplicationScoped
public class MatchSyncer {

    /**
     * Logger
     */
    private static Logger LOG = LoggerFactory.getLogger(MatchSyncer.class.getSimpleName());

    /**
     * Client to help using a REST API
     */
    private WebClient client;

    /**
     * API Base url
     */
    private String api;

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
     * @param vertx Used to API client
     * @param bus Bus used to produce Match events
     */
    public MatchSyncer (CoinbaseProperties props, Vertx vertx, EventBus bus) {
        this.client = WebClient.create(vertx);
        this.bus = bus;
        this.api = props.api;
        this.product = props.product.toLowerCase();
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
            HttpResponse<Buffer> response = this.client
                .getAbs(this.api + "/products/" + this.product + "/trades?limit=100")
                .ssl(true).sendAndAwait();
                
            if(response.statusCode() == 200) {

                // Get the results as a Json Array
                JsonArray array = JsonParser.parseString(response.bodyAsString()).getAsJsonArray();

                // Parse the array and get matches
                for(int c = 0; c < array.size(); c++) {

                    // Extract the JSON Object
                    JsonObject trade = array.get(c).getAsJsonObject();
                    LOG.trace(trade.toString());

                    // Convert to match
                    Match match = Match.newBuilder()
                    .setTimestamp(Instant.parse(Optional.of(trade.get("time")).orElseThrow().getAsString()).toEpochMilli())
                    .setId(Optional.of(trade.get("trade_id")).orElseThrow().getAsLong())
                    .setUnit(Currency.valueOf(this.product.toUpperCase().split("-")[0]))
                    .setQuote(Currency.valueOf(this.product.toUpperCase().split("-")[1]))
                    .setSide(Side.valueOf(Optional.of(trade.get("side")).orElseThrow().getAsString().toUpperCase()))
                    .setPrice(Optional.of(trade.get("price")).orElseThrow().getAsDouble())
                    .setSize(Optional.of(trade.get("size")).orElseThrow().getAsDouble())
                    .build();

                    // Publish match event
                    this.bus.publish(CoinbaseWsEventAddresses.websocketMatchEventAddress, match);
                    LOG.trace("Match " + match.getId() + " fetched and published");
                }
            }
            else {
                throw new Exception("Error in HTTP Response : Code = " + response.statusCode() + " , Reason : " + response.statusMessage());
            }
        }
        catch(Exception ex) 
        {
            LOG.error("Failed to fetch latest trades", ex);
            throw new RuntimeException(ex);
        }
    }
}