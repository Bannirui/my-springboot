package com.github.bannirui.msb.common.util;

import com.github.bannirui.msb.util.VersionUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = {VersionUtilTest.class})
@RunWith(SpringRunner.class)
public class VersionUtilTest {

    @Test
    public void test() {
        String version = VersionUtil.getVersion();
        Assert.assertEquals("3.2.4", version);
    }
}
