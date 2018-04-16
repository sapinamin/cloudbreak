package com.sequenceiq.cloudbreak.converter.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;

import com.google.common.collect.Lists;
import com.sequenceiq.cloudbreak.api.model.FileSystemConfiguration;
import com.sequenceiq.cloudbreak.api.model.FileSystemRequest;
import com.sequenceiq.cloudbreak.api.model.InstanceGroupType;
import com.sequenceiq.cloudbreak.api.model.KerberosRequest;
import com.sequenceiq.cloudbreak.api.model.SharedServiceRequest;
import com.sequenceiq.cloudbreak.api.model.v2.AmbariV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.ClusterV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.InstanceGroupV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.StackV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.TemplateV2Request;
import com.sequenceiq.cloudbreak.blueprint.BlueprintPreparationObject;
import com.sequenceiq.cloudbreak.blueprint.BlueprintProcessingException;
import com.sequenceiq.cloudbreak.blueprint.GeneralClusterConfigsProvider;
import com.sequenceiq.cloudbreak.blueprint.filesystem.FileSystemConfigurationProvider;
import com.sequenceiq.cloudbreak.blueprint.sharedservice.SharedServiceConfigsProvider;
import com.sequenceiq.cloudbreak.blueprint.template.views.HostgroupView;
import com.sequenceiq.cloudbreak.blueprint.templates.BlueprintStackInfo;
import com.sequenceiq.cloudbreak.blueprint.templates.GeneralClusterConfigs;
import com.sequenceiq.cloudbreak.blueprint.utils.StackInfoService;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.common.service.user.UserFilterField;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.domain.FlexSubscription;
import com.sequenceiq.cloudbreak.domain.KerberosConfig;
import com.sequenceiq.cloudbreak.domain.LdapConfig;
import com.sequenceiq.cloudbreak.domain.RDSConfig;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.service.CloudbreakServiceException;
import com.sequenceiq.cloudbreak.service.blueprint.BlueprintService;
import com.sequenceiq.cloudbreak.service.flex.FlexSubscriptionService;
import com.sequenceiq.cloudbreak.service.ldapconfig.LdapConfigService;
import com.sequenceiq.cloudbreak.service.rdsconfig.RdsConfigService;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.service.user.UserDetailsService;

public class StackRequestToBlueprintPreparationObjectConverterTest {

    private static final Long DUMMY_LONG = 1L;

    private static final String DUMMY_EXCEPTION_MESSAGE = "some exception message";

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private StackRequestToBlueprintPreparationObjectConverter underTest;

    @Mock
    private FlexSubscriptionService flexSubscriptionService;

    @Mock
    private LdapConfigService ldapConfigService;

    @Mock
    private StackService stackService;

    @Mock
    private RdsConfigService rdsConfigService;

    @Mock
    private GeneralClusterConfigsProvider generalClusterConfigsProvider;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private BlueprintService blueprintService;

    @Mock
    private FileSystemConfigurationProvider fileSystemConfigurationProvider;

    @Mock
    private StackInfoService stackInfoService;

    @Mock
    private SharedServiceConfigsProvider sharedServiceConfigsProvider;

    @Mock
    private ConversionService conversionService;

    @Mock
    private StackV2Request source;

    @Mock
    private IdentityUser user;

    @Mock
    private ClusterV2Request cluster;

    @Mock
    private AmbariV2Request ambari;

    @Mock
    private KerberosRequest kerberos;

    @Mock
    private Blueprint blueprint;

    @Mock
    private BlueprintStackInfo blueprintStackInfo;

    @Mock
    private FlexSubscription flexSubscription;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(userDetailsService.getDetails(any(), any(UserFilterField.class))).thenReturn(user);
        when(source.getCluster()).thenReturn(cluster);
        when(cluster.getAmbari()).thenReturn(ambari);
        when(ambari.getKerberos()).thenReturn(kerberos);
        when(blueprintService.get(any())).thenReturn(blueprint);
        when(flexSubscriptionService.findOneById(any())).thenReturn(flexSubscription);
        when(stackInfoService.blueprintStackInfo(any())).thenReturn(blueprintStackInfo);
    }

    @Test
    public void testConvertWhenThereIsNotFlexIdThenFlexSubscriptionShouldBeNull() {
        when(source.getFlexId()).thenReturn(null);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertFalse(result.getFlexSubscription().isPresent());
        verify(flexSubscriptionService, times(0)).findOneById(any());
    }

    @Test
    public void testConvertWhenTheFlexIdExistsThenFlexSubscriptionShouldBeNull() {
        when(source.getFlexId()).thenReturn(DUMMY_LONG);
        when(flexSubscriptionService.findOneById(DUMMY_LONG)).thenReturn(flexSubscription);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertTrue(result.getFlexSubscription().isPresent());
        verify(flexSubscriptionService, times(1)).findOneById(DUMMY_LONG);
    }

    @Test
    public void testConvertWhenThereIsNoFlexIdThenSmartsenseSubscriptionIdShouldBeNull() {
        when(source.getFlexId()).thenReturn(null);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertFalse(result.getSmartSenseSubscriptionId().isPresent());
        verify(flexSubscriptionService, times(0)).findOneById(any());
    }

    @Test
    public void testConvertWhenFlexIdExistsThenSmartsenseSubscriptionIdShouldBeNull() {
        String expected = "some value";
        when(source.getFlexId()).thenReturn(DUMMY_LONG);
        when(flexSubscriptionService.findOneById(DUMMY_LONG)).thenReturn(flexSubscription);
        when(flexSubscription.getSubscriptionId()).thenReturn(expected);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertTrue(result.getFlexSubscription().isPresent());
        Assert.assertEquals(expected, result.getFlexSubscription().get().getSubscriptionId());
        verify(flexSubscriptionService, times(1)).findOneById(DUMMY_LONG);
    }

    @Test
    public void testConvertWhenAmbariHasNoKerberosThenKerberosConfigShouldBeNull() {
        when(flexSubscriptionService.findOneById(any())).thenReturn(flexSubscription);
        when(ambari.getKerberos()).thenReturn(null);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertFalse(result.getKerberosConfig().isPresent());
        verify(conversionService, times(0)).convert(any(), any());
    }

    @Test
    public void testConvertWhenAmbariHasKerberosButEnabledSecurityIsFalseThenKerberosConfigShouldBeNull() {
        when(flexSubscriptionService.findOneById(any())).thenReturn(flexSubscription);
        when(ambari.getEnableSecurity()).thenReturn(false);
        when(ambari.getKerberos()).thenReturn(null);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertFalse(result.getKerberosConfig().isPresent());
        verify(conversionService, times(0)).convert(any(), any());
    }

    @Test
    public void testConvertWhenAmbariHasKerberosAndEnabledSecurityIsTrueThenKerberosConfigShouldBeSetProperly() {
        KerberosConfig expected = new KerberosConfig();
        when(flexSubscriptionService.findOneById(any())).thenReturn(flexSubscription);
        when(ambari.getEnableSecurity()).thenReturn(true);
        when(ambari.getKerberos()).thenReturn(kerberos);
        when(conversionService.convert(kerberos, KerberosConfig.class)).thenReturn(expected);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertTrue(result.getKerberosConfig().isPresent());
        Assert.assertEquals(expected, result.getKerberosConfig().get());
        verify(conversionService, times(1)).convert(kerberos, KerberosConfig.class);
    }

    @Test
    public void testConvertWhenLdapConfigNameIsNullThenResultLdapConfigShouldAlsoBeNull() {
        when(cluster.getLdapConfigName()).thenReturn(null);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertFalse(result.getLdapConfig().isPresent());
        verify(ldapConfigService, times(0)).getPublicConfig(any(), any());
    }

    @Test
    public void testConvertWhenLdapConfigNameIsNotNullThenResultLdapConfigShouldBeSetProperly() {
        String ldapConfigName = "some value";
        LdapConfig expected = new LdapConfig();
        when(cluster.getLdapConfigName()).thenReturn(ldapConfigName);
        when(ldapConfigService.getPublicConfig(ldapConfigName, user)).thenReturn(expected);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertTrue(result.getLdapConfig().isPresent());
        Assert.assertEquals(expected, result.getLdapConfig().get());
        verify(ldapConfigService, times(1)).getPublicConfig(ldapConfigName, user);
    }

    @Test
    public void testConvertWhenClusterFileSystemFieldHasNotSetThenFileSystemConfigurationViewShouldAlsoBeNull() throws IOException {
        when(cluster.getFileSystem()).thenReturn(null);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertFalse(result.getFileSystemConfigurationView().isPresent());
        verify(conversionService, times(0)).convert(any(FileSystemRequest.class), any());
        verify(fileSystemConfigurationProvider, times(0)).fileSystemConfiguration(any(), any());
    }

    @Test
    public void testConvertWhenClusterFileSystemFieldHasSetThenFileSystemConfigurationViewShouldAlsoBeSetProperly() throws IOException {
        boolean defaultFileSystem = true;
        FileSystem fileSystem = mock(FileSystem.class);
        FileSystemRequest fileSystemRequest = mock(FileSystemRequest.class);
        FileSystemConfiguration fileSystemConfiguration = mock(FileSystemConfiguration.class);
        when(cluster.getFileSystem()).thenReturn(fileSystemRequest);
        when(fileSystemRequest.isDefaultFs()).thenReturn(defaultFileSystem);
        when(conversionService.convert(fileSystemRequest, FileSystem.class)).thenReturn(fileSystem);
        when(fileSystemConfigurationProvider.fileSystemConfiguration(fileSystem, null)).thenReturn(fileSystemConfiguration);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertTrue(result.getFileSystemConfigurationView().isPresent());
        Assert.assertEquals(fileSystemConfiguration, result.getFileSystemConfigurationView().get().getFileSystemConfiguration());
        Assert.assertEquals(defaultFileSystem, result.getFileSystemConfigurationView().get().isDefaultFs());
        verify(conversionService, times(1)).convert(fileSystemRequest, FileSystem.class);
        verify(fileSystemConfigurationProvider, times(1)).fileSystemConfiguration(fileSystem, null);
    }

    @Test
    public void testConvertIfRdsConfigNamesAreEmptyThenEmptyRdsConfigsShouldBePlacedInResult() {
        when(cluster.getRdsConfigNames()).thenReturn(Collections.emptySet());

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertNotNull(result.getRdsConfigs());
        Assert.assertTrue(result.getRdsConfigs().isEmpty());
    }

    @Test
    public void testConvertIfRdsConfigNamesAreNotEmptyThenTheExpectedRdsConfigsShouldBePlacedInResult() {
        List<String> rdsConfigNamesList = Arrays.asList("first", "second");
        Set<String> rdsConfigNamesSet = new LinkedHashSet<>(rdsConfigNamesList);
        Map<String, RDSConfig> rdsConfigs = createRdsConfigKeyValuePairs(rdsConfigNamesList);
        when(cluster.getRdsConfigNames()).thenReturn(rdsConfigNamesSet);
        rdsConfigNamesList.forEach(key -> when(rdsConfigService.getPrivateRdsConfig(key, user)).thenReturn(rdsConfigs.get(key)));

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertNotNull(result.getRdsConfigs());
        Assert.assertEquals(rdsConfigNamesSet.size(), result.getRdsConfigs().size());
        rdsConfigs.forEach((key, rdsConfig) -> Assert.assertTrue(result.getRdsConfigs().contains(rdsConfig)));
    }

    @Test
    public void testConvertWhenBluprintNameIsNullThenStackInfoServiceShouldBeCalledWithExpectedValue() {
        String blueprintText = "some blueprint text value";
        Blueprint blueprint = mock(Blueprint.class);
        when(blueprint.getBlueprintText()).thenReturn(blueprintText);
        when(ambari.getBlueprintName()).thenReturn(null);
        when(ambari.getBlueprintId()).thenReturn(DUMMY_LONG);
        when(blueprintService.get(DUMMY_LONG)).thenReturn(blueprint);

        underTest.convert(source);

        verify(stackInfoService, times(1)).blueprintStackInfo(blueprintText);
        verify(blueprintService, times(1)).get(DUMMY_LONG);
        verify(blueprintService, times(0)).get(any(), any());
    }

    @Test
    public void testConvertWhenBluprintNameIsEmptyThenStackInfoServiceShouldBeCalledWithExpectedValue() {
        String blueprintText = "some blueprint text value";
        Blueprint blueprint = mock(Blueprint.class);
        when(blueprint.getBlueprintText()).thenReturn(blueprintText);
        when(ambari.getBlueprintName()).thenReturn("");
        when(ambari.getBlueprintId()).thenReturn(DUMMY_LONG);
        when(blueprintService.get(DUMMY_LONG)).thenReturn(blueprint);

        underTest.convert(source);

        verify(stackInfoService, times(1)).blueprintStackInfo(blueprintText);
        verify(blueprintService, times(1)).get(DUMMY_LONG);
        verify(blueprintService, times(0)).get(any(), any());
    }

    @Test
    public void testConvertWhenBluprintNameIsNotEmptyThenStackInfoServiceShouldBeCalledWithExpectedValue() {
        String blueprintText = "some blueprint text value";
        String identityAccount = "account value";
        String blueprintName = "name of the blueprint";
        Blueprint blueprint = mock(Blueprint.class);
        when(blueprint.getBlueprintText()).thenReturn(blueprintText);
        when(ambari.getBlueprintName()).thenReturn(blueprintName);
        when(ambari.getBlueprintId()).thenReturn(DUMMY_LONG);
        when(user.getAccount()).thenReturn(identityAccount);
        when(blueprintService.get(blueprintName, identityAccount)).thenReturn(blueprint);

        underTest.convert(source);

        verify(stackInfoService, times(1)).blueprintStackInfo(blueprintText);
        verify(blueprintService, times(0)).get(anyLong());
        verify(blueprintService, times(1)).get(blueprintName, identityAccount);
    }

    @Test
    public void testConvertWhenStackInfoProvidesBpStackInfoThenItsDataShouldBePlacedInResultBlueprintView() {
        String version = "version data";
        String type = "type data";
        BlueprintStackInfo blueprintStackInfo = mock(BlueprintStackInfo.class);
        when(blueprintStackInfo.getVersion()).thenReturn(version);
        when(blueprintStackInfo.getType()).thenReturn(type);
        when(stackInfoService.blueprintStackInfo(any())).thenReturn(blueprintStackInfo);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertNotNull(result.getBlueprintView());
        Assert.assertEquals(version, result.getBlueprintView().getVersion());
        Assert.assertEquals(type, result.getBlueprintView().getType());
        Assert.assertTrue(result.getStackRepoDetailsHdpVersion().isPresent());
        Assert.assertEquals(version, result.getStackRepoDetailsHdpVersion().get());
    }

    @Test
    public void testConvertWhenInstanceGroupsAreNotProvidedThenInResultThereShouldBeAnEmptyHostGroupView() {
        when(source.getInstanceGroups()).thenReturn(Collections.emptyList());
        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertNotNull(result.getHostgroupViews());
        Assert.assertTrue(result.getHostgroupViews().isEmpty());
    }

    @Test
    public void testConvertWhenInstanceGroupsAreProvidedWithOneValueToCheckInnerValuesInResultThenTheExpectedValuesShouldBeInTheHostGroupView() {
        List<InstanceGroupV2Request> instanceGroupV2Requests = createInstanceGroupV2Requests(1);
        HostgroupView hostgroupView = createHostGroupViewFromInstanceRequest(instanceGroupV2Requests.get(0));
        when(source.getInstanceGroups()).thenReturn(instanceGroupV2Requests);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertNotNull(result.getHostgroupViews());
        Assert.assertEquals(instanceGroupV2Requests.size(), result.getHostgroupViews().size());
        Lists.newArrayList(result.getHostgroupViews()).forEach(view -> {
            Assert.assertEquals(hostgroupView.getName(), view.getName());
            Assert.assertEquals(hostgroupView.getVolumeCount(), view.getVolumeCount());
            Assert.assertEquals(hostgroupView.getInstanceGroupType(), view.getInstanceGroupType());
            Assert.assertEquals(hostgroupView.getNodeCount(), view.getNodeCount());
        });
    }

    @Test
    public void testConvertWhenGeneralClusterConfigsProviderGivesConfigsInstanceThenThisShouldBeStored() {
        GeneralClusterConfigs generalClusterConfigs = new GeneralClusterConfigs();
        when(generalClusterConfigsProvider.generalClusterConfigs(source, user)).thenReturn(generalClusterConfigs);

        BlueprintPreparationObject result = underTest.convert(source);

        Assert.assertEquals(generalClusterConfigs, result.getGeneralClusterConfigs());
        verify(generalClusterConfigsProvider, times(1)).generalClusterConfigs(source, user);
    }

    @Test
    public void testConvertWhenWhenClusterHasNoSharedServiceThenSharedServiceConfigsProviderShouldBeCalledWithNullDataLakeStack() {
        String ambariPw = "some password value";
        when(ambari.getPassword()).thenReturn(ambariPw);
        when(blueprintService.get(anyLong())).thenReturn(blueprint);
        when(cluster.getSharedService()).thenReturn(null);

        underTest.convert(source);

        verify(sharedServiceConfigsProvider, times(1)).createSharedServiceConfigs(blueprint, ambariPw, null);
    }

    @Test
    public void testConvertWhenWhenClusterHasSharedServiceButItsSharedClusterIsNUllThenSharedServiceConfigsProviderShouldBeCalledWithValidDataLakeStack() {
        String ambariPw = "some password value";
        SharedServiceRequest sharedServiceRequest = mock(SharedServiceRequest.class);
        Stack dataLakeStack = new Stack();
        when(ambari.getPassword()).thenReturn(ambariPw);
        when(blueprintService.get(anyLong())).thenReturn(blueprint);
        when(cluster.getSharedService()).thenReturn(sharedServiceRequest);
        when(sharedServiceRequest.getSharedCluster()).thenReturn(null);
        when(stackService.getPublicStack(null, user)).thenReturn(dataLakeStack);

        underTest.convert(source);

        verify(sharedServiceRequest, times(2)).getSharedCluster();
        verify(sharedServiceConfigsProvider, times(1)).createSharedServiceConfigs(blueprint, ambariPw, dataLakeStack);
    }

    @Test
    public void testConvertWhenWhenClusterHasSharedServiceButItsSharedClusterIsEmptyThenSharedServiceConfigsProviderShouldBeCalledWithValidDataLakeStack() {
        String ambariPw = "some password value";
        SharedServiceRequest sharedServiceRequest = mock(SharedServiceRequest.class);
        Stack dataLakeStack = new Stack();
        when(ambari.getPassword()).thenReturn(ambariPw);
        when(blueprintService.get(anyLong())).thenReturn(blueprint);
        when(cluster.getSharedService()).thenReturn(sharedServiceRequest);
        when(sharedServiceRequest.getSharedCluster()).thenReturn("");
        when(stackService.getPublicStack("", user)).thenReturn(dataLakeStack);

        underTest.convert(source);

        verify(sharedServiceRequest, times(2)).getSharedCluster();
        verify(sharedServiceConfigsProvider, times(1)).createSharedServiceConfigs(blueprint, ambariPw, dataLakeStack);
    }

    @Test
    public void testConvertWhenWhenClusterHasSharedServiceButItsSharedClusterIsEmptyThenSharedServiceConfigsProviderShouldBeCalledWithNullDataLakeStack() {
        String ambariPw = "some password value";
        String sharedClusterValue = "some not empty value";
        SharedServiceRequest sharedServiceRequest = mock(SharedServiceRequest.class);
        Stack dataLakeStack = new Stack();
        when(ambari.getPassword()).thenReturn(ambariPw);
        when(blueprintService.get(anyLong())).thenReturn(blueprint);
        when(cluster.getSharedService()).thenReturn(sharedServiceRequest);
        when(sharedServiceRequest.getSharedCluster()).thenReturn(sharedClusterValue);
        when(stackService.getPublicStack(sharedClusterValue, user)).thenReturn(dataLakeStack);

        underTest.convert(source);

        verify(sharedServiceRequest, times(1)).getSharedCluster();
        verify(sharedServiceConfigsProvider, times(1)).createSharedServiceConfigs(blueprint, ambariPw, null);
    }

    @Test
    public void testConvertWhenBlueprintProcessingExceptionComesFromSomewhereThenCloudbreakServiceExceptionShouldComeOutside() {
        when(userDetailsService.getDetails(any(), any())).thenThrow(new BlueprintProcessingException(DUMMY_EXCEPTION_MESSAGE));

        thrown.expect(CloudbreakServiceException.class);
        thrown.expectMessage(DUMMY_EXCEPTION_MESSAGE);

        underTest.convert(source);
    }

    @Test
    public void testConvertWhenIOExceptionComesFromSomewhereThenCloudbreakServiceExceptionShouldComeOutside() throws IOException {
        when(cluster.getFileSystem()).thenReturn(new FileSystemRequest());
        when(fileSystemConfigurationProvider.fileSystemConfiguration(any(), any())).thenThrow(new IOException(DUMMY_EXCEPTION_MESSAGE));

        thrown.expect(CloudbreakServiceException.class);
        thrown.expectMessage(DUMMY_EXCEPTION_MESSAGE);

        underTest.convert(source);
    }

    private List<InstanceGroupV2Request> createInstanceGroupV2Requests(int quantity) {
        List<InstanceGroupV2Request> requests = new ArrayList<>(quantity);
        for (int i = 0; i < quantity; i++) {
            InstanceGroupV2Request request = new InstanceGroupV2Request();
            request.setGroup(String.format("group_%d", i));
            TemplateV2Request template = new TemplateV2Request();
            template.setVolumeCount(i);
            request.setTemplate(template);
            request.setType(InstanceGroupType.CORE);
            request.setNodeCount(i);
            requests.add(request);
        }
        return requests;
    }

    private HostgroupView createHostGroupViewFromInstanceRequest(InstanceGroupV2Request request) {
        return new HostgroupView(
                request.getGroup(),
                request.getTemplate().getVolumeCount(),
                request.getType(),
                request.getNodeCount());
    }

    private Map<String, RDSConfig> createRdsConfigKeyValuePairs(List<String> keys) {
        Map<String, RDSConfig> rdsConfigs = new LinkedHashMap<>(keys.size());
        keys.forEach(key -> rdsConfigs.put(key, new RDSConfig()));
        return rdsConfigs;
    }

}