package com.playtika.test.pubsub;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.GenericContainer;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Slf4j
public class PubSubResourcesGenerator {

    private final TransportChannelProvider channelProvider;
    private final CredentialsProvider credentialsProvider;
    private final TopicAdminClient topicAdminClient;
    private final SubscriptionAdminClient subscriptionAdminClient;
    private final PubsubProperties properties;

    private final String projectId;

    public PubSubResourcesGenerator(@Qualifier(PubsubProperties.BEAN_NAME_EMBEDDED_GOOGLE_PUBSUB) GenericContainer pubsub,
                                    PubsubProperties properties) throws IOException {
        this.properties = properties;
        this.projectId = properties.getProjectId();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(properties.getHost(), pubsub.getMappedPort(properties.getPort())).usePlaintext().build();
        channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
        credentialsProvider = NoCredentialsProvider.create();
        topicAdminClient = topicAdminClient();
        subscriptionAdminClient = subscriptionAdminClient();
    }

    @PostConstruct
    protected void init() {
        log.info("Creating topics and subscriptions.");
        properties.getTopicsAndSubscriptions().forEach(this::createTopicAndSubscription);
        log.info("Creating topics and subscriptions created.");
    }

    private void createTopicAndSubscription(TopicAndSubscription ts) {
        createTopic(ts.getTopic());

        if (ts.getSubscription() != null) {
            createSubscription(ts.getTopic(), ts.getSubscription());
        }
    }

    public Subscription createSubscription(String topicName, String subscriptionName) {
        ProjectTopicName topic = ProjectTopicName.of(projectId, topicName);
        ProjectSubscriptionName subscription = ProjectSubscriptionName.of(projectId, subscriptionName);

        try {
            log.info("Creating subscription: {}", subscription);
            return subscriptionAdminClient
                    .createSubscription(subscription, topic, PushConfig.getDefaultInstance(), 100);
        } catch (AlreadyExistsException e) {
            return subscriptionAdminClient.getSubscription(subscription);
        }
    }

    public Topic createTopic(String topicName) {
        ProjectTopicName topic = ProjectTopicName.of(projectId, topicName);
        try {
            log.info("Creating topic: {}", topic);
            return topicAdminClient.createTopic(topic);
        } catch (AlreadyExistsException e) {
            return topicAdminClient.getTopic(topic);
        }
    }

    public Publisher createPublisher(String topicName) throws IOException {
        return Publisher.newBuilder(ProjectTopicName.of(projectId, topicName))
                .setChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build();
    }

    private TopicAdminClient topicAdminClient() throws IOException {
        return TopicAdminClient.create(
                TopicAdminSettings.newBuilder()
                        .setTransportChannelProvider(channelProvider)
                        .setCredentialsProvider(credentialsProvider).build());
    }


    private SubscriptionAdminClient subscriptionAdminClient() throws IOException {
        return SubscriptionAdminClient.create(SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build());

    }

    public Subscription getSubscription(ProjectSubscriptionName projectSubscriptionName) {
        return subscriptionAdminClient.getSubscription(projectSubscriptionName);
    }
}