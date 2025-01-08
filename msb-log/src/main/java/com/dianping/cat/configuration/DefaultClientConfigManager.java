package com.dianping.cat.configuration;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;
import com.github.bannirui.msb.log.cat.enhance.ClientConfigValidator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.springframework.core.io.ClassPathResource;
import org.unidal.helper.Files;

public class DefaultClientConfigManager implements LogEnabled, ClientConfigManager, Initializable {

    // cat客户端配置文件
    public static final String CAT_CLIENT_XML = "/META-INF/cat/client.xml";
    // 应用配置文件 应用名
    public static final String APP_CONFIG_FILE = "/META-INF/app.properties";
    public static final String XML = "/data/appdatas/cat/client.xml";
    private Logger m_logger;
    private ClientConfig m_config;

    @Override
    public void enableLogging(Logger logger) {
        this.m_logger = logger;
    }

    @Override
    public Domain getDomain() {
        Domain domain = null;
        if (this.m_config != null) {
            Map<String, Domain> domains = this.m_config.getDomains();
            domain = domains.isEmpty() ? null : domains.values().iterator().next();
        }

        return domain != null ? domain : (new Domain("UNKNOWN")).setEnabled(false);
    }

    @Override
    public int getMaxMessageLength() {
        return this.m_config == null ? 5000 : this.getDomain().getMaxMessageSize();
    }

    @Override
    public String getServerConfigUrl() {
        if (this.m_config == null) {
            return null;
        }
        List<Server> servers = this.m_config.getServers();
        for (Server server : servers) {
            Integer httpPort = server.getHttpPort();
            if (httpPort == null || httpPort == 0) {
                httpPort = 8080;
            }
            return String.format("http://%s:%d/cat/s/router?domain=%s&ip=%s&op=json", server.getIp().trim(), httpPort, this.getDomain().getId(),
                NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
        }
        return null;
    }

    @Override
    public List<Server> getServers() {
        return this.m_config == null ? Collections.emptyList() : this.m_config.getServers();
    }

    @Override
    public int getTaggedTransactionCacheSize() {
        return 1024;
    }

    @Override
    public boolean isCatEnabled() {
        return this.m_config == null ? false : this.m_config.isEnabled();
    }

    @Override
    public boolean isDumpLocked() {
        return this.m_config == null ? false : this.m_config.isDumpLocked();
    }

    private ClientConfig loadConfigFromEnvironment() {
        String appName = this.loadProjectName();
        if (appName != null) {
            ClientConfig config = new ClientConfig();
            config.addDomain(new Domain(appName));
            return config;
        } else {
            return null;
        }
    }

    /**
     * CAT_HOME配置了cat-client的接入配置
     * /data/appdatas/cat
     */
    private ClientConfig loadConfigFromCatHome() {
        File f = new File("/data/appdatas/cat/client.xml");
        if(f.exists()) {
            try (FileInputStream is = new FileInputStream(f)) {
                String xml = Files.forIO().readFrom(is, "utf-8");
                return DefaultSaxParser.parse(xml);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * classpath:/META-INF/cat/client.xml配置了cat-client的接入配置
     */
    private ClientConfig loadConfigFromResources() {
        try(InputStream is = new ClassPathResource("/META-INF/cat/client.xml").getInputStream()) {
            String xml = Files.forIO().readFrom(is, "utf-8");
            return DefaultSaxParser.parse(xml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 应用的项目名 配置在classpath:/META-INF/app.properties
     * key=app.id
     */
    private String loadProjectName() {
        String appName = null;
        try (InputStream is = new ClassPathResource(APP_CONFIG_FILE).getInputStream()) {
            Properties prop = new Properties();
            prop.load(is);
            appName = prop.getProperty("app.id");
            this.m_logger.info(String.format("Find domain name %s from app.properties.", appName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appName;
    }

    @Override
    public void initialize() throws InitializationException {
        // cat client config
        File configFile = new File(CAT_CLIENT_XML);
        this.initialize(configFile);
    }

    @Override
    public void initialize(File configFile) throws InitializationException {
        try {
            ClientConfig globalConfig = null;
            ClientConfig clientConfig = null;
            if (configFile != null) {
                if (configFile.exists()) {
                    String xml = Files.forIO().readFrom(configFile.getCanonicalFile(), "utf-8");
                    globalConfig = DefaultSaxParser.parse(xml);
                    this.m_logger.info(String.format("Global config file(%s) found.", configFile));
                } else {
                    this.m_logger.warn(String.format("Global config file(%s) not found, IGNORED.", configFile));
                }
            }
            /**
             * 优先级
             * <ul>
             *     <li>CAT_HOME</li>
             *     <li>项目资源文件</li>
             * </ul>
             */
            clientConfig = this.loadConfigFromCatHome();
            if (clientConfig == null) {
                clientConfig = this.loadConfigFromResources();
            }
            if (globalConfig != null && clientConfig != null) {
                globalConfig.accept(new ClientConfigMerger(clientConfig));
            }
            if (clientConfig != null) {
                clientConfig.accept(new ClientConfigValidator());
            }
            this.m_config = clientConfig;
        } catch (Exception e) {
            throw new InitializationException(e.getMessage(), e);
        }
    }
}
