package com.sequenceiq.cloudbreak.converter.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sequenceiq.cloudbreak.api.model.GatewayType;
import com.sequenceiq.cloudbreak.api.model.SSOType;
import com.sequenceiq.cloudbreak.blueprint.BlueprintPreparationObject;
import com.sequenceiq.cloudbreak.blueprint.GeneralClusterConfigsProvider;
import com.sequenceiq.cloudbreak.blueprint.filesystem.FileSystemConfigurationProvider;
import com.sequenceiq.cloudbreak.blueprint.nifi.HdfConfigProvider;
import com.sequenceiq.cloudbreak.blueprint.nifi.HdfConfigs;
import com.sequenceiq.cloudbreak.blueprint.sharedservice.SharedServiceConfigsProvider;
import com.sequenceiq.cloudbreak.blueprint.templates.BlueprintStackInfo;
import com.sequenceiq.cloudbreak.blueprint.utils.StackInfoService;
import com.sequenceiq.cloudbreak.cloud.model.component.StackRepoDetails;
import com.sequenceiq.cloudbreak.core.bootstrap.service.container.postgres.PostgresConfigService;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.Cluster;
import com.sequenceiq.cloudbreak.domain.Gateway;
import com.sequenceiq.cloudbreak.domain.HostGroup;
import com.sequenceiq.cloudbreak.domain.InstanceMetaData;
import com.sequenceiq.cloudbreak.domain.LdapConfig;
import com.sequenceiq.cloudbreak.domain.SmartSenseSubscription;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.json.Json;
import com.sequenceiq.cloudbreak.service.ClusterComponentConfigProvider;
import com.sequenceiq.cloudbreak.service.cluster.ClusterService;
import com.sequenceiq.cloudbreak.service.cluster.ambari.InstanceGroupMetadataCollector;
import com.sequenceiq.cloudbreak.service.hostgroup.HostGroupService;
import com.sequenceiq.cloudbreak.service.smartsense.SmartSenseSubscriptionService;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.service.user.UserDetailsService;

public class StackToBlueprintPreparationObjectConverterTest {

    private static final String BLUEPRINT_TEXT = "";

    @InjectMocks
    private StackToBlueprintPreparationObjectConverter underTest;

    @Mock
    private HostGroupService hostGroupService;

    @Mock
    private ClusterComponentConfigProvider clusterComponentConfigProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private InstanceGroupMetadataCollector instanceGroupMetadataCollector;

    @Mock
    private SmartSenseSubscriptionService smartSenseSubscriptionService;

    @Mock
    private StackInfoService stackInfoService;

    @Mock
    private HdfConfigProvider hdfConfigProvider;

    @Mock
    private PostgresConfigService postgresConfigService;

    @Mock
    private FileSystemConfigurationProvider fileSystemConfigurationProvider;

    @Mock
    private StackService stackService;

    @Mock
    private ClusterService clusterService;

    @Mock
    private GeneralClusterConfigsProvider generalClusterConfigsProvider;

    @Mock
    private SharedServiceConfigsProvider sharedServiceConfigProvider;

    @Mock
    private Stack source;

    @Mock
    private Cluster cluster;

    @Mock
    private Blueprint blueprint;

    @Mock
    private Gateway gateway;

    @Mock
    private Json blueprintInputs;

    @Mock
    private BlueprintStackInfo blueprintStackInfo;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(source.getCluster()).thenReturn(cluster);
        when(clusterService.getById(any())).thenReturn(cluster);
        when(cluster.getBlueprint()).thenReturn(blueprint);
        when(cluster.getGateway()).thenReturn(gateway);
        when(cluster.getBlueprintInputs()).thenReturn(blueprintInputs);
        when(blueprintInputs.get(Map.class)).thenReturn(Collections.emptyMap());
        when(stackInfoService.blueprintStackInfo(any())).thenReturn(blueprintStackInfo);
    }

    @Test
    public void testConvertWhenDefaultSmartSenseSubscriptionCouldNotGetThenNullShouldBeStored() {
        when(smartSenseSubscriptionService.getDefault()).thenReturn(Optional.empty());

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertFalse(result.getSmartSenseSubscriptionId().isPresent());
        verify(smartSenseSubscriptionService, times(1)).getDefault();
    }

    @Test
    public void testConvertWhenDefaultSmartSenseSubscriptionExistsThroughServiceThenNullShouldBeStored() {
        SmartSenseSubscription dummySmartSenseSubscription = new SmartSenseSubscription();
        String expectedId = "1234";
        dummySmartSenseSubscription.setSubscriptionId(expectedId);
        when(smartSenseSubscriptionService.getDefault()).thenReturn(Optional.of(dummySmartSenseSubscription));

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertTrue(result.getSmartSenseSubscriptionId().isPresent());
        Assert.assertEquals(expectedId, result.getSmartSenseSubscriptionId().get());
        verify(smartSenseSubscriptionService, times(1)).getDefault();
    }

    @Test
    public void testConvertWhenClusterComesFromServiceByIdThenItsGatewayShouldBeSet() throws JsonProcessingException {
        Long clusterId = 1L;
        Gateway gateway = new Gateway();
        gateway.setEnableGateway(true);
        gateway.setGatewayType(GatewayType.CENTRAL);
        gateway.setPath("some path value");
        gateway.setTopologyName("some topology name value");
        gateway.setExposedServices(new Json("{}"));
        gateway.setSsoType(SSOType.SSO_CONSUMER);
        gateway.setSsoProvider("some sso provider value");
        gateway.setSignKey("some sign key value");
        gateway.setSignCert("some sign cert value");
        gateway.setTokenCert("some sign token cert value");
        when(cluster.getId()).thenReturn(clusterId);
        when(clusterService.getById(clusterId)).thenReturn(cluster);
        when(cluster.getGateway()).thenReturn(gateway);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertEquals(gateway.getEnableGateway(), result.getGatewayView().getEnableGateway());
        Assert.assertEquals(gateway.getGatewayType(), result.getGatewayView().getGatewayType());
        Assert.assertEquals(gateway.getPath(), result.getGatewayView().getPath());
        Assert.assertEquals(gateway.getTopologyName(), result.getGatewayView().getTopologyName());
        Assert.assertEquals(gateway.getExposedServices(), result.getGatewayView().getExposedServices());
        Assert.assertEquals(gateway.getSsoType(), result.getGatewayView().getSsoType());
        Assert.assertEquals(gateway.getSsoProvider(), result.getGatewayView().getSsoProvider());
        Assert.assertEquals(gateway.getSignKey(), result.getGatewayView().getSignKey());
        Assert.assertEquals(gateway.getSignCert(), result.getGatewayView().getSignCert());
        Assert.assertEquals(gateway.getTokenCert(), result.getGatewayView().getTokenCert());
        verify(clusterService, times(1)).getById(clusterId);
    }

    @Test
    public void testConvertWhenClusterHaveLdapConfigThenThisShouldBeSetInResult() {
        LdapConfig ldapConfig = new LdapConfig();
        when(cluster.getLdapConfig()).thenReturn(ldapConfig);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertTrue(result.getLdapConfig().isPresent());
        Assert.assertEquals(ldapConfig, result.getLdapConfig().get());
    }

    @Test
    public void testConvertWhenClusterHasNoLdapConfigThenThisFieldShouldBeEmpty() {
        when(cluster.getLdapConfig()).thenReturn(null);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertFalse(result.getLdapConfig().isPresent());
    }

    @Test
    public void testConvertWhenHdpRepoCouldComeFromServiceThenItsHdpVersionShouldBeStored() {
        Long clusterId = 1L;
        StackRepoDetails hdpRepo = new StackRepoDetails();
        hdpRepo.setHdpVersion("some version data");
        when(cluster.getId()).thenReturn(clusterId);
        when(clusterComponentConfigProvider.getHDPRepo(clusterId)).thenReturn(hdpRepo);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertTrue(result.getStackRepoDetailsHdpVersion().isPresent());
        Assert.assertEquals(hdpRepo.getHdpVersion(), result.getStackRepoDetailsHdpVersion().get());
        verify(clusterComponentConfigProvider, times(1)).getHDPRepo(clusterId);
    }

    @Test
    public void testConvertWhenHdpRepoCouldNotComeFromServiceThenItsHdpVersionShouldBeStored() {
        Long clusterId = 1L;
        when(cluster.getId()).thenReturn(clusterId);
        when(clusterComponentConfigProvider.getHDPRepo(clusterId)).thenReturn(null);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertFalse(result.getStackRepoDetailsHdpVersion().isPresent());
        verify(clusterComponentConfigProvider, times(1)).getHDPRepo(clusterId);
    }

    @Test
    public void testConvertWhenHdfConfigsCouldComeFromConfigProviderThenThisShouldBeSetInResult() {
        HdfConfigs expected = mock(HdfConfigs.class);
        Map<String, List<InstanceMetaData>> groupInstances = Collections.emptyMap();
        Set<HostGroup> hostGroups = Collections.emptySet();
        when(instanceGroupMetadataCollector.collectMetadata(source)).thenReturn(groupInstances);
        when(cluster.getHostGroups()).thenReturn(hostGroups);
        when(blueprint.getBlueprintText()).thenReturn(BLUEPRINT_TEXT);
        when(hdfConfigProvider.createHdfConfig(hostGroups, groupInstances, BLUEPRINT_TEXT)).thenReturn(expected);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertTrue(result.getHdfConfigs().isPresent());
        Assert.assertEquals(expected, result.getHdfConfigs().get());
        verify(instanceGroupMetadataCollector, times(1)).collectMetadata(source);
        verify(hdfConfigProvider, times(1)).createHdfConfig(hostGroups, groupInstances, BLUEPRINT_TEXT);
    }

    @Test
    public void testConvertWhenHdfConfigsCouldNotComeFromConfigProviderThenThisShouldBeSetInResult() {
        Map<String, List<InstanceMetaData>> groupInstances = Collections.emptyMap();
        Set<HostGroup> hostGroups = Collections.emptySet();
        when(instanceGroupMetadataCollector.collectMetadata(source)).thenReturn(groupInstances);
        when(cluster.getHostGroups()).thenReturn(hostGroups);
        when(blueprint.getBlueprintText()).thenReturn(BLUEPRINT_TEXT);
        when(hdfConfigProvider.createHdfConfig(hostGroups, groupInstances, BLUEPRINT_TEXT)).thenReturn(null);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertFalse(result.getHdfConfigs().isPresent());
        verify(instanceGroupMetadataCollector, times(1)).collectMetadata(source);
        verify(hdfConfigProvider, times(1)).createHdfConfig(hostGroups, groupInstances, BLUEPRINT_TEXT);
    }

    @Test
    public void testConvertWhenBlueprintStackInfoCouldComeFromServiceThenItsDataShouldBeStored() {
        String stackInfoType = "some stack info type value";
        String stackInfoVersion = "some stack info version value";
        BlueprintStackInfo blueprintStackInfo = mock(BlueprintStackInfo.class);
        when(blueprintStackInfo.getVersion()).thenReturn(stackInfoVersion);
        when(blueprintStackInfo.getType()).thenReturn(stackInfoType);
        when(blueprint.getBlueprintText()).thenReturn(BLUEPRINT_TEXT);
        when(stackInfoService.blueprintStackInfo(BLUEPRINT_TEXT)).thenReturn(blueprintStackInfo);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertEquals(stackInfoType, result.getBlueprintView().getType());
        Assert.assertEquals(stackInfoVersion, result.getBlueprintView().getVersion());
        verify(stackInfoService, times(1)).blueprintStackInfo(BLUEPRINT_TEXT);
    }


}