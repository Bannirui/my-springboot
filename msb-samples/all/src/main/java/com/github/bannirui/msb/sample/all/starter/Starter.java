package com.github.bannirui.msb.sample.all.starter;

import com.github.bannirui.msb.http.annotation.EnableMyHttp;
import com.github.bannirui.msb.web.annotation.EnableMyWeb;
import com.github.com.bannirui.msb.sso.annotation.EnableMySso;
import org.springframework.context.annotation.Configuration;

/**
 * 启动场景.
 */
@Configuration
@EnableMyWeb
@EnableMySso
@EnableMyHttp
public class Starter {
}
