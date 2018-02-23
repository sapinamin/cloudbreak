package com.sequenceiq.it.cloudbreak.newway;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.cloudbreak.client.CloudbreakClient.CloudbreakClientBuilder;
import com.sequenceiq.it.IntegrationTestContext;

public class CloudbreakClient extends Entity {
    public static final String CLOUDBREAK_CLIENT = "CLOUDBREAK_CLIENT";

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudbreakClient.class);

    private static com.sequenceiq.cloudbreak.client.CloudbreakClient singletonCloudbreakClient;

    private com.sequenceiq.cloudbreak.client.CloudbreakClient cloudbreakClient;

    CloudbreakClient(String newId) {
        super(newId);
    }

    CloudbreakClient() {
        this(CLOUDBREAK_CLIENT);
    }

    public void setCloudbreakClient(com.sequenceiq.cloudbreak.client.CloudbreakClient cloudbreakClient) {
        this.cloudbreakClient = cloudbreakClient;
    }

    public com.sequenceiq.cloudbreak.client.CloudbreakClient getCloudbreakClient() {
        return cloudbreakClient;
    }

    public static Function<IntegrationTestContext, CloudbreakClient> getTestContextCloudbreakClient(String key) {
        return (testContext) -> testContext.getContextParam(key, CloudbreakClient.class);
    }

    public static Function<IntegrationTestContext, CloudbreakClient> getTestContextCloudbreakClient() {
        return getTestContextCloudbreakClient(CLOUDBREAK_CLIENT);
    }

    public static CloudbreakClient isCreated() {
        CloudbreakClient client = new CloudbreakClient();
        client.setCreationStrategy(CloudbreakClient::singletonCloudbreakClient);
        return client;
    }

    public static void newCloudbreakClientCreationStrategy(IntegrationTestContext integrationTestContext, Entity entity) {
        CloudbreakClient clientEntity = (CloudbreakClient) entity;
        com.sequenceiq.cloudbreak.client.CloudbreakClient client;
        client = new CloudbreakClientBuilder(
                integrationTestContext.getContextParam(CloudbreakTest.CLOUDBREAK_SERVER_ROOT),
                integrationTestContext.getContextParam(CloudbreakTest.IDENTITY_URL),
                "cloudbreak_shell")
                .withCertificateValidation(false)
                .withDebug(true)
                .withCredential(integrationTestContext.getContextParam(CloudbreakTest.USER),
                        integrationTestContext.getContextParam(CloudbreakTest.PASSWORD))
                .withIgnorePreValidation(true)
                .build();
        clientEntity.cloudbreakClient = client;
    }

    public static synchronized void singletonCloudbreakClient(IntegrationTestContext integrationTestContext, Entity entity) {
        CloudbreakClient clientEntity = (CloudbreakClient) entity;
        if (singletonCloudbreakClient == null) {
            singletonCloudbreakClient = new CloudbreakClientBuilder(
                    integrationTestContext.getContextParam(CloudbreakTest.CLOUDBREAK_SERVER_ROOT),
                    integrationTestContext.getContextParam(CloudbreakTest.IDENTITY_URL),
                    "cloudbreak_shell")
                    .withCertificateValidation(false)
                    .withDebug(true)
                    .withCredential(integrationTestContext.getContextParam(CloudbreakTest.USER),
                            integrationTestContext.getContextParam(CloudbreakTest.PASSWORD))
                    .withIgnorePreValidation(true)
                    .build();
        }
        clientEntity.cloudbreakClient = singletonCloudbreakClient;
    }
}

