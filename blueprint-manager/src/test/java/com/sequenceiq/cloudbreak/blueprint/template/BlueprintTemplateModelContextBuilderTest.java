package com.sequenceiq.cloudbreak.blueprint.template;

import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sequenceiq.cloudbreak.blueprint.sharedservice.SharedServiceConfigs;

public class BlueprintTemplateModelContextBuilderTest {

    private BlueprintTemplateModelContextBuilder underTest;

    @Before
    public void setUp() {
        underTest = new BlueprintTemplateModelContextBuilder();
    }

    @Test
    public void testWhetherSharedServiceAddedOrNotIfSomeValuePassed() {
        SharedServiceConfigs sharedServiceConfigs = new SharedServiceConfigs();
        underTest.withSharedServiceConfigs(Optional.of(sharedServiceConfigs));

        Map<String, Object> result = underTest.build();

        Assert.assertTrue(result.containsKey("sharedService"));
        Assert.assertNotNull(result.get("sharedService"));
        Assert.assertEquals(sharedServiceConfigs, result.get("sharedService"));
    }

    @Test
    public void testWhetherSharedServiceAddedOrNotIfNothingPassed() {
        Map<String, Object> result = underTest.build();

        Assert.assertTrue(result.containsKey("sharedService"));
        Assert.assertNull(result.get("sharedService"));
    }

}