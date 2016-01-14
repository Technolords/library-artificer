package net.technolords.tools.artificer.analyser.dotclass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Created by Technolords on 2016-Jan-06.
 */
public class DemoTestNGAnnotationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoTestNGAnnotationTest.class);
    private Sample sample;

    // BeforeSuite
    // AfterSuite

    // BeforeTest
    // AfterTest
    @BeforeTest
    public void beforeTest() {
        LOGGER.info("beforeTest called...");
    }

    // BeforeGroups
    // AfterGroups
    @BeforeGroups(groups = "groupA")
    public void beforeGroupsA() {
        LOGGER.info("beforeGroups A called...");
        sample = new Sample();
    }

    @AfterGroups(groups = "groupA")
    public void afterGroupsA() {
        LOGGER.info("afterGroups A called...");
        sample = null;
    }

    @BeforeGroups(groups = "groupB")
    public void beforeGroupsB() {
        LOGGER.info("beforeGroups B called...");
//        sample = new Sample();
    }

    // BeforeClass
    // AfterClass
    @BeforeClass
    public void beforeClass() {
        LOGGER.info("beforeClass called...");
    }

    // BeforeMethod
    // AfterMethod
    @BeforeMethod
    public void beforeMethod() {
        LOGGER.info("beforeMethod called...");
    }

    @DataProvider(name = "dataSet")
    public Object[][] dataSet() {
        return new Object[][] {
            { "first" },
            { "second" },
            { "third" },
        };
    }

    @Test(groups = "groupA", dataProvider = "dataSet")
    public void testA(String cookie) {
        LOGGER.info("testA called, with Sample: " + sample);
    }

    @Test(groups = "groupA")
    public void testB() {
        LOGGER.info("testB called, with Sample: " + sample);
    }

    @Test(groups = "groupB")
    public void testC() {
        LOGGER.info("testC called, with Sample: " + sample);
    }

    public class Sample {

    }
}
