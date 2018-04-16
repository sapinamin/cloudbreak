package com.sequenceiq.cloudbreak.converter.v2;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;

import com.sequenceiq.cloudbreak.api.model.FileSystemConfiguration;
import com.sequenceiq.cloudbreak.api.model.FileSystemRequest;
import com.sequenceiq.cloudbreak.api.model.KerberosRequest;
import com.sequenceiq.cloudbreak.api.model.v2.AmbariV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.ClusterV2Request;
import com.sequenceiq.cloudbreak.api.model.v2.StackV2Request;
import com.sequenceiq.cloudbreak.blueprint.BlueprintPreparationObject;
import com.sequenceiq.cloudbreak.blueprint.GeneralClusterConfigsProvider;
import com.sequenceiq.cloudbreak.blueprint.filesystem.FileSystemConfigurationProvider;
import com.sequenceiq.cloudbreak.blueprint.sharedservice.SharedServiceConfigsProvider;
import com.sequenceiq.cloudbreak.blueprint.templates.BlueprintStackInfo;
import com.sequenceiq.cloudbreak.blueprint.utils.StackInfoService;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.common.service.user.UserFilterField;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.FileSystem;
import com.sequenceiq.cloudbreak.domain.FlexSubscription;
import com.sequenceiq.cloudbreak.domain.KerberosConfig;
import com.sequenceiq.cloudbreak.domain.LdapConfig;
import com.sequenceiq.cloudbreak.service.blueprint.BlueprintService;
import com.sequenceiq.cloudbreak.service.flex.FlexSubscriptionService;
import com.sequenceiq.cloudbreak.service.ldapconfig.LdapConfigService;
import com.sequenceiq.cloudbreak.service.rdsconfig.RdsConfigService;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.service.user.UserDetailsService;

public class StackRequestToBlueprintPreparationObjectConverterTest {

    private static final Long DUMMY_LONG = 1L;

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

}