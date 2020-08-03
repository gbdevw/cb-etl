package gbdevw.colibri.coinbaseetl.coinbase.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.ContainerProvider;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gbdevw.colibri.coinbaseetl.configuration.CoinbaseProperties;
import io.quarkus.runtime.Startup;
import io.quarkus.vertx.ConsumeEvent;

@ApplicationScoped
@Startup
@Liveness
public class CoinbaseWSContainer implements AutoCloseable, HealthCheck {

    /*********************************************************************************************/
    /* MEMBERS */
    /*********************************************************************************************/

    /**
     * Logger
     */
    private final static Logger LOG = LoggerFactory.getLogger(CoinbaseWSContainer.class.getSimpleName());

    /**
     * Coinbase websocket client endpoint
     */
    CoinbaseWSClientEndpoint cbwsce;

    /**
     * Session
     */
    private Session session;

    /**
     * URI of the websocket feed.
     */
    private String dest;

    /**
     * Retry counter
     */
    private AtomicInteger retry;

    /**
     * Max. retry
     */
    private int maxRetry;

    /**
     * Retry throttle in ms
     */
    private int retryThrottle;

    /**
     * Health indicator
     */
    private boolean isRunning;

    /*********************************************************************************************/
    /* Constructor & Factory */
    /*********************************************************************************************/

    /**
     * CoinbaseWSContainer constructor
     */
    CoinbaseWSContainer(CoinbaseProperties config, CoinbaseWSClientEndpoint wsc) {

        // Setup the websocket client
        this.cbwsce = wsc;

        // Setup dest. URI
        this.dest = config.websocket;

        // Setup retry & throttle
        this.retryThrottle = 4500;
        this.maxRetry = 3;

        // Setup health indicator
        this.isRunning = false;

        // Setup retry counter
        this.retry = new AtomicInteger(0);
    }

    /**
     * CoinbaseWebsocketContainer factory
     * 
     * @param env Configuration
     * @param wsc Websocket client to manage
     * 
     * @throws Exception Error during initialization
     */
    @PostConstruct
    public void start () throws Exception {

        try {

            LOG.info("Starting websocket");

            // Build URL
            URI destination = new URI(this.dest);
            LOG.info("URL : " + destination.toString());

            // Setup the websocket container
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

            // Connect to the feed
            this.session = null;
            while (this.session == null && (this.retry.get() < this.maxRetry)) {
                try {
                    // Connect to the feed
                    this.session = container.connectToServer(this.cbwsce, destination);
                } catch (IOException e) {
                    // Log error, increase retry and throttle
                    LOG.error("Failed to connect to " + this.dest + " - Attempt : "
                            + this.retry.incrementAndGet(), e);
                    Thread.sleep(this.retryThrottle);
                }
            }

            // The bean failed too many times
            if (this.retry.get() >= this.maxRetry) {
                throw new IOException("Failed to connect to " + this.dest);
            }

            // Reset retry counter
            this.retry.set(0);

            // Set isRunning -> true
            this.isRunning = true;
        } catch (Exception e) {
            // Log error & throw exception to stop
            LOG.error(e.getMessage(), e);
            throw e;
        }
    }

    /*********************************************************************************************/
    /* Websocket close event management */
    /*********************************************************************************************/

    /**
     * Event handler used when a close event is fired by the underlying websocket
     * client.
     */
    @ConsumeEvent("coinbase/websocket/close")
    public void consumeCloseEvent(CloseReason reason) {
        LOG.info("Websocket closed : " + Optional.of(reason.getReasonPhrase()).orElse("UNKNOWN"));
        this.isRunning = false;
    }

    /*********************************************************************************************/
    /* UTILITY */
    /*********************************************************************************************/

    /**
     * Close the websocket container
     */
    @Override
    @PreDestroy
    public void close() throws Exception {
        if (this.session != null) {
            this.session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Closed by container"));
        }
    }

    /**
     * Service healthcheck
     * 
     * @return Tell if the service is healthy or not
     */
    @Override
    public HealthCheckResponse call() {
        return this.isRunning ? HealthCheckResponse.up("Coinbase websocket feed OK") : HealthCheckResponse.down("Coinbase websocket feed KO");
    }
}