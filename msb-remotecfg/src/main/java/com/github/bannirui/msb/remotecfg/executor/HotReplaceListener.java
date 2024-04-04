package com.github.bannirui.msb.remotecfg.executor;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.github.bannirui.msb.remotecfg.MySpringApplicationEventListener;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 监听器.
 * 监听远程配置中心的回调.
 * 启动时机放在Spring应用已经准备好.
 * 因为要读到远程配置中心的信息 所以放到{@link MySpringApplicationEventListener}中去.
 */
public class HotReplaceListener extends Thread {

    private MySpringApplicationEventListener listener;

    /**
     * 跟{@link MySpringApplicationEventListener}耦合的原因是为了读取它的成员(nacos的配置信息).
     */
    public HotReplaceListener(MySpringApplicationEventListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        String nacosUrl = this.listener.getNacosMeta().getServer();
        List<String> dataIds = this.listener.getNacosMeta().getDataIds();
        String group = "DEFAULT_GROUP";
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, nacosUrl);
        ConfigService configService = null;
        try {
            configService = NacosFactory.createConfigService(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (configService == null) {
            return;
        }
        for (String dataId : dataIds) {
            // 轮询注册监听器
            try {
                configService.addListener(dataId, group, new Listener() {

                    /**
                     * nacos配置中心发布更新 回调给监听器的竟然是整个data id的所有配置内容
                     * @param content 配置内容
                     */
                    @Override
                    public void receiveConfigInfo(String content) {
                        // TODO: 2024/4/5 收到了nacos的回调 解析出每个配置项
                        /**
                         * 这个时候又遇到问题了 收到的回到是所有配置内容
                         * <ul>
                         *     <li>在内容中记下每个key的最新的value 然后收到nacos的回调后跟value比较 有更新才执行反射</li>
                         *     <li>每次收到所有配置的时候全量执行反射所有Bean实例 这样肯定不行的</li>
                         * </ul>
                         */
                    }

                    @Override
                    public Executor getExecutor() {
                        return null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
