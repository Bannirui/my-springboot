package com.github.bannirui.msb.endpoint.web.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultThreadFactory;

public class NettyHttpService {
    private int prot = 8166;
    private EventLoopGroup accept = new NioEventLoopGroup(1, new DefaultThreadFactory("endpoint-accept"));
    private EventLoopGroup worker =
        new NioEventLoopGroup(Math.max(1, NettyRuntime.availableProcessors() / 2), new DefaultThreadFactory("endpoint-worker"));

    public NettyHttpService(int prot) {
        this.prot = prot;
    }

    public void start() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
            .group(this.accept, this.worker)
            .channel(NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 1024)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) {
                    channel.pipeline().addLast(new HttpResponseEncoder());
                    channel.pipeline().addLast(new HttpRequestDecoder());
                    channel.pipeline().addLast("httpAggregator", new HttpObjectAggregator(65535));
                    channel.pipeline().addLast(new HttpServerHandler());
                }
            });
        serverBootstrap.bind(this.prot).sync();
    }

    public void close() {
        this.accept.shutdownGracefully();
        this.worker.shutdownGracefully();
    }
}
