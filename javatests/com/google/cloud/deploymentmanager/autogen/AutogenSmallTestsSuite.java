package com.google.cloud.deploymentmanager.autogen;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    DisplayDescriptionGeneratorTest.class,
    SoyDirectivesTest.class,
    SoyFunctionsTest.class,
    SpecDefaultsTest.class,
    SpecValidationsTest.class
})
public class AutogenSmallTestsSuite {
}
