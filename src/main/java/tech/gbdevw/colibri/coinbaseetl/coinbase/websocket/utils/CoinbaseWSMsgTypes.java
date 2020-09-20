package tech.gbdevw.colibri.coinbaseetl.coinbase.websocket.utils;

/**
 * Utility class that describe message types from coinbase websocket feed
 */
public class CoinbaseWSMsgTypes {
    public static final String ERROR = "error";
    public static final String SUBSCRIBE = "subscribe";
    public static final String SUBSCRIPTIONS = "subscriptions";
    public static final String TICKER = "ticker";
    public static final String MATCH = "match";
    public static final String HEARTBEAT = "heartbeat";
    public static final String LASTMATCH = "last_match";
}