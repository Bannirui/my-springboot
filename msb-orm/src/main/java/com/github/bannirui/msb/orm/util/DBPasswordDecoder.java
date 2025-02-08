package com.github.bannirui.msb.orm.util;

import com.github.bannirui.msb.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.util.RSAUtils;
import org.apache.commons.lang3.StringUtils;

public class DBPasswordDecoder {
    private DBPasswordDecoder() {
        throw new IllegalStateException("Utility class");
    }

    public static String decode(String password) {
        String privateKey = MsbEnvironmentMgr.getProperty("db.password.privateKey");
        if(StringUtils.isNotBlank(privateKey)) {
            String temp = RSAUtils.RSADecode(password, privateKey);
            if (temp != null) {
                return temp;
            }
        }
        return password;
    }
}
