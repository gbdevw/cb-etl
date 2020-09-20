package tech.gbdevw.colibri.coinbaseetl.coinbase.websocket.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class offering methods that ease the process to subscribe to the
 * coinbase websocket feed. 
 */
public class CoinbaseWSubscriptionHelper {
    
    /**
     * Build a subscribe message to subscribe to the websocket feeds.
     * 
     * @param products Products to support for each channel (ex : BTC-USD)
     * @param channels Channels to subscribe to (Ex : ticker)
     * 
     * @return Subscribe message 
     */
    public static JsonObject buildSubscribeMessage (String [] products, String [] channels) {
        
        // Build JsonObject that contains the message
        JsonObject subscribe = new JsonObject();
        
        // Set message type = subscribe
        subscribe.addProperty("type", CoinbaseWSMsgTypes.SUBSCRIBE);
        
        // Set product to support
        JsonArray products_ids = new JsonArray();
        for(String p : products) { products_ids.add(p); }
        subscribe.add("product_ids", products_ids);

        // Set channels to subscribe to
        JsonArray channels_ids = new JsonArray();
        for(String channel : channels) { channels_ids.add(channel); }
        subscribe.add("channels", channels_ids);
        
        // return the message
        return subscribe;
    }

    /**
     * Check subscription response message to verify if all required channels & products
     * are supported (no more, no less).
     * 
     * @param subscription Subscription response message
     * @param products Products to support
     * @param channels Channels to support
     * @return True if all channels and products are correctly supported. An exception is thrown otherwise
     * @throws Exception An excpetion is thrown if there is a mismatch between the required channels and products and the subscription message.
     */
    public static boolean checkSubscriptionMessage (JsonObject subscription, String [] products, String [] channels) throws Exception {
        
        // Verify that we subscribed to all channels and only the channel we wanted to subscribe to
        // Aslo verify that we support the products we want to support
        
        // Get all the channels from the subscription response
        JsonArray respChannels = Optional.of(subscription.getAsJsonArray("channels")).get();

        // Check that the channel count is OK
        if(respChannels.size() != channels.length) {
            throw new Exception("Channel subscription mismatch with configuration");
        }

        // Compute  channel list
        ArrayList<String> channelList = new ArrayList<>();
        for(String c : channels) { channelList.add(c); }

        // Compute product list
        ArrayList<String> productList = new ArrayList<>();
        for(String p : products) { productList.add(p); }
        
        // Check each record for valid product and channel
        Iterator<JsonElement> content = respChannels.iterator();        
        while(content.hasNext()) {
            
            JsonObject record = content.next().getAsJsonObject();
            
            // Check that the channel subscribed to is required
            String channelName = Optional.of(record.get("name")).get().getAsString();
            if(!channelList.remove(channelName)) {
                // Mismatch between required channels and subscribed channels
                throw new Exception("Mismatch between required channels and subscribed channel : " + channelName);
            }

            // Check product count is OK
            JsonArray subscriptionProducts = Optional.of(record.getAsJsonArray("product_ids")).get();
            if(subscriptionProducts.size() != products.length) {
                throw new Exception("Product mismatch with configuration");
            }
            
            // Check products supported by the channel subscription
            Iterator<JsonElement> supportedProducts = subscriptionProducts.iterator();
            ArrayList<String> pl = new ArrayList<>(productList);
            while(supportedProducts.hasNext()) {
                String productId = supportedProducts.next().getAsString();
                if(!pl.remove(productId)) {
                    // Mismatch between required products and supported products
                    throw new Exception("Mismatch between required products and supported products : " + productId);
                }
            }
        
            // Check that all required products are supported
            if(pl.size() > 0) {
                // Mismatch between required products and supported products
                throw new Exception("Mismatch between required products and supported products : " + pl.toString());
            }
        }

        // Check that all the channels required by configuration have been subscribed to
        if(channelList.size() > 0) {
            // Mismatch between required channels and subscribed channels
            throw new Exception("Mismatch between required channels and subscribed channel : " + channelList.toString());
        }

        return true;
    }
}