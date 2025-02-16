package com.github.bannirui.msb.impl;

import com.github.bannirui.Echo;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
public class EchoServiceImpl implements Echo {

    @Override
    public String echo(String msg) {
        return "msg is: " + msg;
    }
}
