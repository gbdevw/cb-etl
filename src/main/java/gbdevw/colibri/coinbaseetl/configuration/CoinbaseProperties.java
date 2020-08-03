package gbdevw.colibri.coinbaseetl.configuration;

import io.quarkus.arc.config.ConfigProperties;

/**
 * Class which defines properties related to Coinbase.
 */
@ConfigProperties(prefix="coinbase")
public class CoinbaseProperties{

    /**
     * URI of the COinbase websocket feed
     */
    public String websocket;

    /**
     * URI of the Coinbase Pro API
     */
    public String api;

    /**
     * Product to support
     */
    public String product;
}