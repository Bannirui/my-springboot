package com.github.bannirui.msb.mq.sdk.common;

import com.github.bannirui.msb.mq.sdk.utils.Utils;
import org.springside.modules.utils.net.NetUtil;

public class MmsEnv {
    public static final String MMS_VERSION = Utils.getMmsVersion();
    /**
     * 本机ip.
     */
    public static final String MMS_IP = NetUtil.getLocalHost();
}
