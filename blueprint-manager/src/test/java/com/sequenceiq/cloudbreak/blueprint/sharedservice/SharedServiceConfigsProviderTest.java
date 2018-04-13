package com.sequenceiq.cloudbreak.blueprint.sharedservice;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.Cluster;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.json.Json;

public class SharedServiceConfigsProviderTest {

    private static final String AMBARI_PASSWORD = "Passw0rd";

    private SharedServiceConfigsProvider underTest;

    @Mock
    private Blueprint blueprint;

    @Mock
    private Stack source;

    @Mock
    private Stack dataLakeStack;

    @Mock
    private Cluster cluster;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new SharedServiceConfigsProvider();
        when(source.getCluster()).thenReturn(cluster);
        when(cluster.getBlueprint()).thenReturn(blueprint);
        when(cluster.getPassword()).thenReturn(AMBARI_PASSWORD);
    }

    @Test
    public void testCreateConfigsTwoParamMethodWhenDatalakeStackIsNullAndClusterHasNoBlueprintTagsThenAttachedAndDatalakeClustersFalse() {
        when(blueprint.getTags()).thenReturn(null);

        SharedServiceConfigs result = underTest.createSharedServiceConfigs(source, null);

        Assert.assertEquals(AMBARI_PASSWORD, result.getRangerAdminPassword());
        Assert.assertFalse(result.isAttachedCluster());
        Assert.assertFalse(result.isDatalakeCluster());
    }

    @Test
    public void testCreateConfigsTwoParamMethodWhenDatalakeStackIsNullAndClusterHasBlueprintButNotContainsKeyThenAttachedAndDatalakeClustersFalse() {
        Json json = mock(Json.class);
        when(blueprint.getTags()).thenReturn(json);
        when(json.getMap()).thenReturn(Collections.emptyMap());

        SharedServiceConfigs result = underTest.createSharedServiceConfigs(source, null);

        Assert.assertEquals(AMBARI_PASSWORD, result.getRangerAdminPassword());
        Assert.assertFalse(result.isAttachedCluster());
        Assert.assertFalse(result.isDatalakeCluster());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateConfigsTwoParamMethodWhenDatalakeStackIsNullAndClusterHasBlueprintAndContainsKeyThenAttachedClusterFalseDatalakeClustersTrue() {
        Json json = mock(Json.class);
        Map<String, Object> tagMap = mock(HashMap.class);
        when(blueprint.getTags()).thenReturn(json);
        when(json.getMap()).thenReturn(tagMap);
        when(tagMap.containsKey("shared_services_ready")).thenReturn(true);

        SharedServiceConfigs result = underTest.createSharedServiceConfigs(source, null);

        Assert.assertEquals(AMBARI_PASSWORD, result.getRangerAdminPassword());
        Assert.assertFalse(result.isAttachedCluster());
        Assert.assertTrue(result.isDatalakeCluster());
    }

    @Test
    public void testCreateConfigsTwoParamMethodWhenDatalakeStackIsNotNullThenAttachedClusterTrueDatalakeClusterFalseAndDatalakePasswordWillBeSet() {
        String clusterPassword = "Some0therPassw0rd";
        when(dataLakeStack.getCluster()).thenReturn(cluster);
        when(cluster.getPassword()).thenReturn(clusterPassword);

        SharedServiceConfigs result = underTest.createSharedServiceConfigs(source, dataLakeStack);

        Assert.assertEquals(clusterPassword, result.getRangerAdminPassword());
        Assert.assertTrue(result.isAttachedCluster());
        Assert.assertFalse(result.isDatalakeCluster());
    }

    @Test
    public void testCreateConfigsThreeParamMethodWhenDatalakeStackIsNullAndClusterHasNoBlueprintTagsThenAttachedAndDatalakeClustersFalse() {
        when(blueprint.getTags()).thenReturn(null);

        SharedServiceConfigs result = underTest.createSharedServiceConfigs(blueprint, AMBARI_PASSWORD, null);

        Assert.assertEquals(AMBARI_PASSWORD, result.getRangerAdminPassword());
        Assert.assertFalse(result.isAttachedCluster());
        Assert.assertFalse(result.isDatalakeCluster());
    }

    @Test
    public void testCreateConfigsThreeParamMethodWhenDatalakeStackIsNullAndClusterHasBlueprintButNotContainsKeyThenAttachedAndDatalakeClustersFalse() {
        Json json = mock(Json.class);
        when(blueprint.getTags()).thenReturn(json);
        when(json.getMap()).thenReturn(Collections.emptyMap());

        SharedServiceConfigs result = underTest.createSharedServiceConfigs(blueprint, AMBARI_PASSWORD, null);

        Assert.assertEquals(AMBARI_PASSWORD, result.getRangerAdminPassword());
        Assert.assertFalse(result.isAttachedCluster());
        Assert.assertFalse(result.isDatalakeCluster());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateConfigsThreeParamMethodWhenDatalakeStackIsNullAndClusterHasBlueprintAndContainsKeyThenAttachedClusterFalseDatalakeClustersTrue() {
        Json json = mock(Json.class);
        Map<String, Object> tagMap = mock(HashMap.class);
        when(blueprint.getTags()).thenReturn(json);
        when(json.getMap()).thenReturn(tagMap);
        when(tagMap.containsKey("shared_services_ready")).thenReturn(true);

        SharedServiceConfigs result = underTest.createSharedServiceConfigs(blueprint, AMBARI_PASSWORD, null);

        Assert.assertEquals(AMBARI_PASSWORD, result.getRangerAdminPassword());
        Assert.assertFalse(result.isAttachedCluster());
        Assert.assertTrue(result.isDatalakeCluster());
    }

    @Test
    public void testCreateConfigsThreeParamMethodWhenDatalakeStackIsNotNullThenAttachedClusterTrueDatalakeClusterFalseAndDatalakePasswordWillBeSet() {
        String clusterPassword = "Some0therPassw0rd";
        when(dataLakeStack.getCluster()).thenReturn(cluster);
        when(cluster.getPassword()).thenReturn(clusterPassword);

        SharedServiceConfigs result = underTest.createSharedServiceConfigs(blueprint, AMBARI_PASSWORD, dataLakeStack);

        Assert.assertEquals(clusterPassword, result.getRangerAdminPassword());
        Assert.assertTrue(result.isAttachedCluster());
        Assert.assertFalse(result.isDatalakeCluster());
    }

}