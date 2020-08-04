### Coinbase ETL

This application fetches trades and ticker events from the Coinbase websocket feed and pushes those events to a Kafka messaging system.

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

### Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `coinbase-etl-1.0.0-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/coinbase-etl-1.0.0-runner.jar`.

### Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker`.

You can then execute your native executable with: `./target/coinbase-etl-1.0.0-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.

### Configuration

| ENV. VARIABLES | DEFAULT/EXAMPLE | DESCRIPTION | COMMENT |
| --- | --- | --- | --- | --- |
| APPLICATION_NAME | coinbase-etl | Defines the application name | /
| APPLICATION_PROFILE | prod | Override application profile | /
| APPLICATION_HOST | 0.0.0.0 | Interfaces to listen on | /
| COINBASE_WSS_URI | wss://ws-feed.pro.coinbase.com | URI of the Coinbase websocket feed | Default to production
| COINBASE_API_URI | https://api.pro.coinbase.com | URI of the Coinbase websocket feed | Default to production
| COINBASE_PRODUCT | BTC-USD | Product to watch | https://api.pro.coinbase.com/products |
| KAFKA_ENABLED | false | Enable Kafka producers | Set to true to activate - If enabled, one of KAFKA_CONFIG_PATH or KAFKA_CONFIG_RESOURCEmust be defined |
| KAFKA_CONFIG_PATH | /config/kafka.properties | Absolute path to a property file which contains Kafka configuration | Mandatory if KAFKA_ENABLED=true - Value will be considered before KAFKA_CONFIG_RESOURCE |
| KAFKA_CONFIG_RESOURCE | kafka/kafka.properties | Relative path to a property file located in resources which contians Kafka configuration | Mandatory if KAFKA_ENABLED=true - Value will be considered before KAFKA_CONFIG_RESOURCE |
| KAFKA_TOPICS_MATCHES | coinbase-btc-usd-matches | Topic to publish trades data | Mandatory if KAFKA_ENABLED=true |
| KAFKA_TOPICS_MATCHES | coinbase-btc-usd-tickers | Topic to publish ticker data | Mandatory if KAFKA_ENABLED=true |
