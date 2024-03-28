package com.github.bannirui.msb.sample.all.starter;

import com.github.bannirui.msb.http.annotation.EnableMyHttp;
import com.github.bannirui.msb.web.annotation.EnableMyWeb;
import com.github.bannirui.msg.remotecfg.annotation.EnableMyRemoteCfg;
import org.springframework.context.annotation.Configuration;

/**
 * 启动场景.
 */
@Configuration
@EnableMyWeb
@EnableMyHttp
@EnableMyRemoteCfg
public class Starter {
}
