package tech.gbdevw.colibri.coinbaseetl.coinbase.websocket.utils;

import java.time.Instant;
import java.util.Optional;

import com.google.gson.JsonObject;

import tech.gbdevw.colibri.domain.Match;
import tech.gbdevw.colibri.domain.Currency;
import tech.gbdevw.colibri.domain.Side;

/**
 * Class which provides methods to extract data from a Match message received
 * from the Coinbase feed
 */
public class CoinbaseWsMatchHelper {
    
    /**
     * Return the match from the match message
     * @param msg match message
     * @return the match data
     * @throws NoSuchElementException if the message does not contain the expected properties
     */
    public static Match getMatch (JsonObject msg) {
        return Match.newBuilder()
            .setTimestamp(Instant.parse(Optional.of(msg.get("time")).orElseThrow().getAsString()).toEpochMilli())
            .setId(Optional.of(msg.get("trade_id")).orElseThrow().getAsLong())
            .setUnit(Currency.valueOf(Optional.of(msg.get("product_id")).orElseThrow().getAsString().split("-")[0]))
            .setQuote(Currency.valueOf(Optional.of(msg.get("product_id")).orElseThrow().getAsString().split("-")[1]))
            .setSide(Side.valueOf(Optional.of(msg.get("side")).orElseThrow().getAsString().toUpperCase()))
            .setPrice(Optional.of(msg.get("price")).orElseThrow().getAsDouble())
            .setSize(Optional.of(msg.get("size")).orElseThrow().getAsDouble())
            .build();
    }
}