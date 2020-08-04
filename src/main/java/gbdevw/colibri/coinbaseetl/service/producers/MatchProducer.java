package gbdevw.colibri.coinbaseetl.service.producers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import gbdevw.colibri.coinbaseetl.coinbase.websocket.utils.CoinbaseWsEventAddresses;
import gbdevw.colibri.coinbaseetl.configuration.ConfluentProperties;
import gbdevw.colibri.domain.marketevent.MarketEvent.Match;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.vertx.ConsumeEvent;

/**
 * This class provides a service to handle match events and send them on a messaging system.
 */
@RegisterForReflection
@ApplicationScoped
@IfBuildProperty(name = "confluent.enabled", stringValue = "true")
@Startup
@Liveness
public class MatchProducer implements AutoCloseable, HealthCheck {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(MatchProducer.class.getSimpleName());

    /**
     * Kafka producer
     */
    private Producer<Long, Match> matchProducer;

    /**
     * Topic to use
     */
    private String topic;

    /**
     * Producer configuration
     */
    private ConfluentProperties producerProps;

    /**
     * Health indicator
     */
    private boolean healthy;

    /**
     * Constructor.
     * 
     * @param config Properties to configure Kafka and the match producer
     */
    public MatchProducer(ConfluentProperties config) throws Exception {

        this.producerProps = config;
        this.topic = config.match.get();
        this.healthy = true;
    }

    /**
     * Start the match producer
     */
    @PostConstruct
    public void start () throws Exception {

        // Stream to read configuration
        InputStream is = null;

        if(this.producerProps.resource.isPresent()) {
            // Read config from resources
            is = this.getClass().getResourceAsStream(this.producerProps.resource.get());
            LOG.info("Loading configuration from resources");
        }
        else {
            // Read config from file
            is = new FileInputStream(new File(this.producerProps.file.get()));
            LOG.info("Loading configuration from file");
        }

        // Load properties
        Properties props = new Properties ();
        props.load(is);
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaProtobufSerializer.class.getName());

        // Create the producer
        this.matchProducer = new KafkaProducer<Long, Match>(props);
    }

    @ConsumeEvent(value = CoinbaseWsEventAddresses.websocketMatchEventAddress, blocking = true)
    public void produceMatchEvent(Match match) {
        try {
            LOG.trace("Processing match event : " + match.getId());
            ProducerRecord<Long, Match> record = new ProducerRecord<Long, Match>(this.topic, match.getId(), match);
            this.matchProducer.send(record).get();
            LOG.info("Match event processed : " + match.getId());
        } catch (Exception ex) {
            LOG.error("An error occured while processing the match event", ex);
            this.healthy = false;
        }
    }

    /**
     * Indicates whether the service is healty or not
     * 
     * @return Service  health
     */
    @Override
    public HealthCheckResponse call() {
        return this.healthy ? HealthCheckResponse.up("Match producer OK") : HealthCheckResponse.down("Match producer KO");
    }

    /**
     * Close all the underlying resources
     * 
     * @throws Exception An exception occured while closing resources
     */
    @Override
    public void close() throws Exception {
        this.matchProducer.close();
    }
}