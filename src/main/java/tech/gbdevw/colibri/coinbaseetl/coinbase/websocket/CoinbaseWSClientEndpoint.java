package tech.gbdevw.colibri.coinbaseetl.coinbase.websocket;

import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.gbdevw.colibri.coinbaseetl.codecs.MatchMessageCodec;
import tech.gbdevw.colibri.coinbaseetl.codecs.TickerMessageCodec;
import tech.gbdevw.colibri.coinbaseetl.coinbase.websocket.utils.CoinbaseWSMsgTypes;
import tech.gbdevw.colibri.coinbaseetl.coinbase.websocket.utils.CoinbaseWSTickHelper;
import tech.gbdevw.colibri.coinbaseetl.coinbase.websocket.utils.CoinbaseWSubscriptionHelper;
import tech.gbdevw.colibri.coinbaseetl.coinbase.websocket.utils.CoinbaseWsEventAddresses;
import tech.gbdevw.colibri.coinbaseetl.coinbase.websocket.utils.CoinbaseWsLastMatchHelper;
import tech.gbdevw.colibri.coinbaseetl.coinbase.websocket.utils.CoinbaseWsMatchHelper;
import tech.gbdevw.colibri.coinbaseetl.configuration.CoinbaseProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;

/**
 * Client endpoint for coinbase websocket feed.
 */
@RegisterForReflection
@ClientEndpoint
@ApplicationScoped
public class CoinbaseWSClientEndpoint {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CoinbaseWSClientEndpoint.class.getSimpleName());

    /**
     * Bus to publish events
     */
    private EventBus bus;

    /**
     * Products to support for each channel
     */
    private Set<String> products;
    
    /**
     * Channels to subscribe to
     */
    private Set<String> channels;

    /**
     * Constructor for CoinbaseWSClientEndpoint class
     * 
     * @param bus Event bus
     * @param configuration Configuration
     */
    public CoinbaseWSClientEndpoint(EventBus bus, CoinbaseProperties configuration) {
        
        // Register event bus & codecs
        this.bus = bus;
        this.bus.registerCodec(new MatchMessageCodec());
        this.bus.registerCodec(new TickerMessageCodec());

        // Load product
        this.products = new HashSet<>();
        products.add(configuration.product);
        LOG.info("Supported product : " + configuration.product);

        // Load channels
        this.channels = new HashSet<>();
        channels.add("ticker");
        LOG.info("Supported channel : ticker");
        channels.add("matches");
        LOG.info("Supported channel : matches");
    }

    /**
     * Called when the websocket is connected
     * 
     * @param session session object
     */
    @OnOpen
    public void onOpen(Session session) {
        LOG.info("Connected to feed : " + session.getRequestURI().toString());

        try {
            // Build subscribe message
            JsonObject subscribe = CoinbaseWSubscriptionHelper.buildSubscribeMessage(this.products.toArray(new String [this.products.size()]), this.channels.toArray(new String [this.channels.size()]));

            // Subscribe to channels
            session.getBasicRemote().sendText(subscribe.toString());
        }
        catch (Exception ex) {
            try {
                // Log error & close session
                LOG.error("An error occured while subscribing to websocket channels", ex);
                session.close(new CloseReason(CloseCodes.PROTOCOL_ERROR, "An error occured while subscribing to channels"));
            } catch (IOException e) {
                // Log closing error & publish close event
                LOG.error("Websocket could not be closed normally", e);
                this.bus.publish(CoinbaseWsEventAddresses.websocketCloseEventAddress, new CloseReason(CloseCodes.CLOSED_ABNORMALLY, "Websocket could not be closed normally"));
            }
        }
    }

    /**
     * Called when a message is received from the feed
     * 
     * @param session session object
     * @param message Text message received from the feed
     */
    @OnMessage
    public void onMessage(Session session, String message) {
        try {

            // Parse message & get message type (or exception if not present)
            JsonObject raw = JsonParser.parseString(message).getAsJsonObject();
            String msgType = Optional.ofNullable(raw.get("type")).get().getAsString();
            LOG.info("Message received from Coinbase feed : " + msgType);
            LOG.trace(message);

            // Dispatch or process if msgType is "subscription" or "error"
            switch(msgType) {
  
                case CoinbaseWSMsgTypes.SUBSCRIPTIONS :
                    // Verify subscription
                    CoinbaseWSubscriptionHelper.checkSubscriptionMessage(raw, this.products.toArray(new String [this.products.size()]), this.channels.toArray(new String [this.channels.size()]));
                    LOG.info("Subscription OK");
                    break;

                case CoinbaseWSMsgTypes.TICKER :
                    // Publish tick event
                    this.bus.publish(CoinbaseWsEventAddresses.websocketTickEventAddress, CoinbaseWSTickHelper.getTicker(raw), new DeliveryOptions().setCodecName(TickerMessageCodec.class.getName()));
                    break;

                case CoinbaseWSMsgTypes.MATCH :
                    // Publish match event
                    this.bus.publish(CoinbaseWsEventAddresses.websocketMatchEventAddress, CoinbaseWsMatchHelper.getMatch(raw), new DeliveryOptions().setCodecName(MatchMessageCodec.class.getName()));
                    break;

                case CoinbaseWSMsgTypes.LASTMATCH :
                    // publish last_match event & publish match event
                    this.bus.publish(CoinbaseWsEventAddresses.websocketLastMatchEventAddress, CoinbaseWsLastMatchHelper.getLastTradeId(raw));
                    this.bus.publish(CoinbaseWsEventAddresses.websocketMatchEventAddress, CoinbaseWsLastMatchHelper.getLastMatch(raw), new DeliveryOptions().setCodecName(MatchMessageCodec.class.getName()));
                    break;
                
                case CoinbaseWSMsgTypes.ERROR :
                    // Process error
                    throw new Exception(Optional.ofNullable(raw.get("message")).get().getAsString());

                default:
                    // Unsupported message type
                    LOG.warn("Unsupported message type (" + msgType + ") processed");
                    LOG.warn("Unspported message : " + raw.toString());
                    break;
            }
        }
        // An error occured while parsing the message
        catch(NoSuchElementException ex) {
            try {
                // Log error & close session
                LOG.error("Expected property not found in the message", ex);
                LOG.error("Message : ", message);
                session.close(new CloseReason(CloseCodes.PROTOCOL_ERROR, "An error occured while processing a message from the feed"));
            } catch (IOException e) {
                // Log closing error & publish close event
                LOG.error("Websocket could not be closed normally", e);
                this.bus.publish(CoinbaseWsEventAddresses.websocketCloseEventAddress, new CloseReason(CloseCodes.CLOSED_ABNORMALLY, "Websocket could not be closed normally"));
            }
        }
        catch(Exception ex) {
            try {
                // An error occured while processing the message - Log & close
                LOG.error("An error occured while processing a message from the feed", ex);
                LOG.error("Message : ", message);
                session.close(new CloseReason(CloseCodes.PROTOCOL_ERROR, "An error occured while processing a message from the feed"));
            }
            catch (IOException e) {
                // Log closing error & publish close event
                LOG.error("Websocket could not be closed normally", e);
                this.bus.publish(CoinbaseWsEventAddresses.websocketCloseEventAddress, new CloseReason(CloseCodes.CLOSED_ABNORMALLY, "Websocket could not be closed normally"));
            }
        }
    }

    /**
     * Called when a error message is received from the websocket feed
     * 
     * @param session websocket session object
     * @param error error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        try {
            // Log error and close websocket from client side
            LOG.error("Websocket error received from feed", error);
            session.close(new CloseReason(CloseCodes.PROTOCOL_ERROR, "Websocket error received from feed"));
        } catch (IOException e) {
            // Log closing error & publish close event
            LOG.error("Websocket could not be closed normally", e);
            this.bus.publish(CoinbaseWsEventAddresses.websocketCloseEventAddress, new CloseReason(CloseCodes.CLOSED_ABNORMALLY, "Websocket could not be closed normally"));
        }
    }

    /**
     * Called when the websocket is closed
     * 
     * @param session websocket session object
     * @param reason close reason
     */
    @OnClose
    public void onClose (Session session, CloseReason reason) {
        if(reason.getCloseCode() == CloseCodes.NORMAL_CLOSURE) {
            LOG.info("Websocket closed : " + reason.toString());
        }
        else {
            LOG.error("Websocket closed : " + reason.toString());
        }

        // Publish close event
        this.bus.publish(CoinbaseWsEventAddresses.websocketCloseEventAddress, reason);
    }
}