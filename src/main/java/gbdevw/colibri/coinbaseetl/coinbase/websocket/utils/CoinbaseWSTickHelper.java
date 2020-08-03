package gbdevw.colibri.coinbaseetl.coinbase.websocket.utils;

import java.time.Instant;
import java.util.Optional;

import com.google.gson.JsonObject;

import gbdevw.colibri.domain.marketevent.MarketEvent.Ticker;
import gbdevw.colibri.domain.utils.Utils.Currency;
import gbdevw.colibri.domain.utils.Utils.Side;

/**
 * Class which provides methods to extract data from a Ticker message received from the Coinbase feed
 */
public class CoinbaseWSTickHelper {

    /**
     * Return the ticker from the ticker message
     * @param msg ticker message
     * @return the ticker data
     * @throws NoSuchElementException if the message does not contain the expected properties
     */
    public static Ticker getTicker (JsonObject msg) {
        return Ticker.newBuilder()
            .setTimestamp(Instant.parse(Optional.of(msg.get("time")).orElseThrow().getAsString()).toEpochMilli())
            .setId(Optional.of(msg.get("sequence")).orElseThrow().getAsLong())
            .setUnit(Currency.valueOf(Optional.of(msg.get("product_id")).orElseThrow().getAsString().split("-")[0]))
            .setQuote(Currency.valueOf(Optional.of(msg.get("product_id")).orElseThrow().getAsString().split("-")[1]))
            .setBestAsk(Optional.of(msg.get("best_ask")).orElseThrow().getAsDouble())
            .setBestBid(Optional.of(msg.get("best_bid")).orElseThrow().getAsDouble())
            .setLastTradeSide(Side.valueOf(Optional.of(msg.get("side")).orElseThrow().getAsString().toUpperCase()))
            .setLastTradePrice(Optional.of(msg.get("price")).orElseThrow().getAsDouble())
            .setLastTradeSize(Optional.of(msg.get("last_size")).orElseThrow().getAsDouble())
            .build();
    }
}