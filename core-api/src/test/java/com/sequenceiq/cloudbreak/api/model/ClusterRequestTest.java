package com.sequenceiq.cloudbreak.api.model;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.HibernateValidator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

public class ClusterRequestTest {

    private static final String NOT_NULL_VIOLATION_MESSAGE = "may not be null";

    private LocalValidatorFactoryBean localValidatorFactory;

    private ClusterRequest underTest;

    @Before
    public void setUp() {
        underTest = new ClusterRequest();
        localValidatorFactory = new LocalValidatorFactoryBean();
        localValidatorFactory.setProviderClass(HibernateValidator.class);
        localValidatorFactory.afterPropertiesSet();
    }

    @Test
    public void testClusterRequestCreationWhenNameHasMeetsTheRequirementsThenEverythingGoesFine() {
        underTest.setName("some-name");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertFalse(constraintViolations.stream().anyMatch(violation -> !NOT_NULL_VIOLATION_MESSAGE.equalsIgnoreCase(violation.getMessage())));
    }

    @Test
    public void testClusterRequestCreationWhenNameDoesNotContainsHyphenThenEverythingGoesFine() {
        underTest.setName("somename");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertFalse(constraintViolations.stream().anyMatch(violation -> !NOT_NULL_VIOLATION_MESSAGE.equalsIgnoreCase(violation.getMessage())));
    }

    @Test
    public void testClusterRequestCreationWhenNameStartsWithHyphenThenViolationHappens() {
        underTest.setName("-somename");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenNameEndsWithHyphenThenViolationHappens() {
        underTest.setName("somename-");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenNameContainsOnlyHyphenThenViolationHappens() {
        underTest.setName("------");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenNameContainsAnUpperCaseLetterThenViolationHappens() {
        underTest.setName("somEname");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenUsernameHasMeetsTheRequirementsThenEverythingGoesFine() {
        underTest.setUserName("some-name");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertFalse(constraintViolations.stream().anyMatch(violation -> !NOT_NULL_VIOLATION_MESSAGE.equalsIgnoreCase(violation.getMessage())));
    }

    @Test
    public void testClusterRequestCreationWhenUsernameDoesNotContainsHyphenThenEverythingGoesFine() {
        underTest.setUserName("somename");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertFalse(constraintViolations.stream().anyMatch(violation -> !NOT_NULL_VIOLATION_MESSAGE.equalsIgnoreCase(violation.getMessage())));
    }

    @Test
    public void testClusterRequestCreationWhenUsernameStartsWithHyphenThenViolationHappens() {
        underTest.setUserName("-somename");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenUsernameEndsWithHyphenThenViolationHappens() {
        underTest.setUserName("somename-");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenUsernameContainsOnlyHyphenThenViolationHappens() {
        underTest.setUserName("------");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenUsernameContainsAnUpperCaseLetterThenViolationHappens() {
        underTest.setUserName("somEname");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenPasswordHasEnoughLengthAndMeetsWithEveryRequirementThenEverythingGoesFine() {
        underTest.setPassword("Passw0rd");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertFalse(constraintViolations.stream().anyMatch(violation -> !NOT_NULL_VIOLATION_MESSAGE.equalsIgnoreCase(violation.getMessage())));
    }

    @Test
    public void testClusterRequestCreationWhenPasswordHasNotEnoughLengthThenViolationHappens() {
        underTest.setPassword("Adm1n");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenPasswordHasNotEnoughDigitThenViolationHappens() {
        underTest.setPassword("Password");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenPasswordHasNotEnoughUpperCaseCharacterThenViolationHappens() {
        underTest.setPassword("passw0rd");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenPasswordHasNotEnoughLowerCaseCharacterThenViolationHappens() {
        underTest.setPassword("PASSW0RD");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenPasswordHasOnlyUpperCaseCharactersThenViolationHappens() {
        underTest.setPassword("PASSWORD");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenPasswordHasOnlyLowerCaseCharactersThenViolationHappens() {
        underTest.setPassword("password");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenPasswordHasOnlydDigitsThenViolationHappens() {
        underTest.setPassword("123456789");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    @Test
    public void testClusterRequestCreationWhenPasswordHasEveryRequiredElementButHasUndesirableSpecialCharacterThenViolationHappens() {
        underTest.setPassword("Passw0rd$");
        Set<ConstraintViolation<ClusterRequest>> constraintViolations = localValidatorFactory.validate(underTest);
        Assert.assertEquals(1L, countViolationsExceptSpecificOne(constraintViolations, NOT_NULL_VIOLATION_MESSAGE));
    }

    private long countViolationsExceptSpecificOne(Set<ConstraintViolation<ClusterRequest>> constraintViolations, String excludedViolationMessage) {
        return constraintViolations.stream().filter(violation -> !excludedViolationMessage.equalsIgnoreCase(violation.getMessage())).count();
    }

}