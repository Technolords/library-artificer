package net.technolords.tools.test;

import org.testng.annotations.Test;

/**
 * Created by Technolords on 2016-Jun-17.
 */
public class SilentLoadGeneratorTest {

    @Test
    public void testSmallSpike() throws InterruptedException {
        SilentLoadGenerator silentLoadGenerator = new SilentLoadGenerator(500, 500, null, true);
        silentLoadGenerator.executeLoadGeneration();
    }
}