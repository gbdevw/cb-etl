package gbdevw.colibri.coinbaseetl.configuration;

import java.util.Optional;

import io.quarkus.arc.config.ConfigProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Class which defines properties to interact with the Kafka cluster
 */
@RegisterForReflection
@ConfigProperties(prefix="confluent")
public class ConfluentProperties {

    // Properties

    /**
     * Enable producing messages to the Kafka cluster
     */
    public Boolean enabled = false;

    /**
     * Load the Kafka config. from a file (absolute path)
     */
    public Optional<String> file;

    /**
     * Load the Kafka config. from reosurces
     */
    public Optional<String> resource;

    /**
     * Match topic
     */
    public Optional<String> match;

    /**
     * Ticker topic
     */
    public Optional<String> ticker;
}