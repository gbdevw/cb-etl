package gbdevw.colibri.coinbaseetl.coinbase.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

/**
 * Interface which declare methods to interact with Coinbase Pro API.
 */
@RegisterRestClient(configKey = "coinbase-rest-client")
public interface CoinbaseClient {

    @GET
    @Path("/products/{product}/trades")
    @Consumes("application/json")
    public CoinbaseTrade [] getLatestTrades (@PathParam String product, @QueryParam("limit") Integer limit) ;
}