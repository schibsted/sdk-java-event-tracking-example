This is an example of how to use the Java API client to send events to the Data Collector.

# Usage
```
mvn clean install -q exec:java -Dexec.mainClass="no.spt.sdk.example.TrackingApplication"
```

This will send an example event to the stage Data Collector using a tracking ID from the stage
Central Identification Service