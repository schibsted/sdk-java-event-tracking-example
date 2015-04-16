package no.spt.sdk.example;

import no.spt.sdk.Options;
import no.spt.sdk.client.DataTrackingClient;
import no.spt.sdk.identity.IdentityCallback;
import no.spt.sdk.models.TrackingIdentity;

import java.util.HashMap;
import java.util.Map;

import static no.spt.sdk.models.Makers.*;

public class TrackingApplication {

    public static void main(String[] args) {

        // The unique client ID provided by SPT
        final String clientId = "JavaSdkExample123";

        // The url to the data collector endpoint
        final String dataTrackerUrl = "http://elb-test-insight-data-collector-884169598.eu-west-1.elb.amazonaws.com/api/v1/track";

        // The url to the Central Identification Service (CIS) endpoint
        final String CISUrl = "http://stage-identity.spid.se/api/v1/identify";

        // The url to the error report collector endpoint
        final String errorReportingUrl = "http://elb-test-insight-data-collector-884169598.eu-west-1.elb.amazonaws.com/api/v1/track";

        // The maximum size of the activity queue waiting to be sent to the data collector. If the queue reaches
        // this size, any additional activities will be dropped to prevent memory problems.
        final int maxActivityQueueSize = 10000;

        // The amount of milliseconds before a request is marked as timed out
        final int sendTimeout = 1000;

        // The amount of times to retry the request
        final int sendRetries = 2;

        // Create the Options object that the DataTrackingClient will use
        final Options options = new Options.Builder(clientId)
            .setDataCollectorUrl(dataTrackerUrl)
            .setCISUrl(CISUrl)
            .setErrorReportingUrl(errorReportingUrl)
            .setMaxQueueSize(maxActivityQueueSize)
            .setTimeout(sendTimeout)
            .setRetries(sendRetries)
            .build();

        // Create the DataTrackingClient with the Options object and optional configurations
        final DataTrackingClient client = new DataTrackingClient.Builder()
            .withOptions(options)
            .withAutomaticActivitySender()
            .build();

        // Create a map with user identifiers. This could be the user's IP-address, SPiD login id or some other identifier
        Map<String, String> identifiers = new HashMap<String, String>();
        identifiers.put("clientIp", "127.0.0.1");
        identifiers.put("userId", "urn:spid.no:user:abc123");

        // We don't want the tracking client to block our application so we first identify the user asynchronously and
        // then track the activity using an IdentityCallback.
        // The activity we want to track is a user who sends a message to another user.
        client.identifyActorAsync(identifiers, new IdentityCallback() {
            @Override
            public void onSuccess(TrackingIdentity trackingId) {
                client.track(activity("Send",
                    provider("Organization", "urn:spid.no:" + clientId)
                        .build(),
                    actor(trackingId)
                        .displayName("User with visitor ID " + trackingId.getVisitorId())
                        .build(),
                    object("Content", "urn:spid.no:message:abc123")
                        .title("Example message title")
                        .build())
                    .target(target("Person", "urn:example@email.com")
                        .build())
                    .build());
            }
        });

        // When closing the application we should also close the DataTrackingClient to allow internal queues to flush
        client.close();

        // The DataTrackingClient keeps track of stats
        System.out.println(client.getStats().toString());
    }

}
