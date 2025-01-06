package com.dianping.cat.configuration;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;
import com.github.bannirui.msb.log.cat.enhance.ClientConfigValidator;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
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
            domain = domains.isEmpty() ? null : (Domain) domains.values().iterator().next();
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

    private ClientConfig loadConfigFromEnviroment() {
        String appName = this.loadProjectName();
        if (appName != null) {
            ClientConfig config = new ClientConfig();
            config.addDomain(new Domain(appName));
            return config;
        } else {
            return null;
        }
    }

    private ClientConfig loadConfigFromXml() {
        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream("/META-INF/cat/client.xml");
            if (in == null) {
                in = Cat.class.getResourceAsStream("/META-INF/cat/client.xml");
            }
            if (in == null) {
                return null;
            }
            String xml = Files.forIO().readFrom(in, "utf-8");
            this.m_logger.info(String.format("Resource file(%s) found.", Cat.class.getResource("/META-INF/cat/client.xml")));
            return DefaultSaxParser.parse(xml);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception var13) {
                }
            }
        }
        return null;
    }

    private String loadProjectName() {
        String appName = null;
        InputStream in = null;
        try {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(APP_CONFIG_FILE);
            if (in == null) {
                in = Cat.class.getResourceAsStream(APP_CONFIG_FILE);
            }
            if (in != null) {
                Properties prop = new Properties();
                prop.load(in);
                appName = prop.getProperty("app.name");
                if (appName == null) {
                    this.m_logger.info("Can't find app.name from app.properties.");
                    return null;
                }
                this.m_logger.info(String.format("Find domain name %s from app.properties.", appName));
            } else {
                this.m_logger.info(String.format("Can't find app.properties in %s", APP_CONFIG_FILE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception var14) {
                }
            }

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
            clientConfig = this.loadConfigFromEnviroment();
            if (clientConfig == null) {
                clientConfig = this.loadConfigFromXml();
            }
            if (globalConfig != null && clientConfig != null) {
                globalConfig.accept(new ClientConfigMerger(clientConfig));
            }
            if (clientConfig != null) {
                clientConfig.accept(new ClientConfigValidator());
            }
            this.m_config = clientConfig;
        } catch (Exception var5) {
            throw new InitializationException(var5.getMessage(), var5);
        }
    }
}
