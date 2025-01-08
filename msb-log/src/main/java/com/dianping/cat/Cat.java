package com.dianping.cat;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.ForkedTransaction;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.MessageProducer;
import com.dianping.cat.message.TaggedTransaction;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageTree;
import com.github.bannirui.msb.common.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.springframework.core.io.ClassPathResource;
import org.unidal.helper.Files;
import org.unidal.helper.Properties;
import org.unidal.initialization.DefaultModuleContext;
import org.unidal.initialization.Module;
import org.unidal.initialization.ModuleContext;
import org.unidal.initialization.ModuleInitializer;
import org.unidal.lookup.ContainerLoader;

public class Cat {

    private static Cat s_instance = new Cat();
    private static volatile boolean s_init = false;
    private MessageProducer m_producer;
    private MessageManager m_manager;
    private PlexusContainer m_container;

    /**
     * 接入cat-client要指定client.xml配置文件
     * 优先级为
     * <ul>
     *     <li>启动jar包时通过VM参数-DCAT_HOME 以${CAT_HOME}/client.xml</li>
     *     <li>msb项目的classpath:/META-INF/cat/client.xml</li>
     * </ul>
     */
    private static void checkAndInitialize() {
        if (!s_init) {
            synchronized (s_instance) {
                if (!s_init) {
                    // cat client config
                    String parentPath = getCatHome();
                    File f = null;
                    if(Objects.nonNull(parentPath)) {
                        f=new File(parentPath, "client.xml");
                    }
                    if (Objects.isNull(f) || !f.exists()) {
                        try {
                            f = File.createTempFile("tmp", "txt");
                            FileUtils.copyInputStreamToFile(new ClassPathResource("/META-INF/cat/client.xml").getInputStream(), f);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (!f.exists()) {
                            log("WARN", "Cat client config file not exists");
                        }
                    }
                    initialize(f);
                    log("WARN", "Cat is lazy initialized!");
                    s_init = true;
                }
            }
        }
    }

    public static String createMessageId() {
        return getProducer().createMessageId();
    }

    public static void destroy() {
        s_instance.m_container.dispose();
        s_instance = new Cat();
    }

    /**
     * cat客户端全局配置文件.
     */
    public static String getCatHome() {
        return Properties.forString().fromEnv().fromSystem().getProperty("CAT_HOME", null);
    }

    public static String getCurrentMessageId() {
        MessageTree tree = getManager().getThreadLocalMessageTree();
        if (tree != null) {
            String messageId = tree.getMessageId();
            if (messageId == null) {
                messageId = createMessageId();
                tree.setMessageId(messageId);
            }
            return messageId;
        } else {
            return null;
        }
    }

    public static Cat getInstance() {
        return s_instance;
    }

    public static MessageManager getManager() {
        checkAndInitialize();
        return s_instance.m_manager;
    }

    public static MessageProducer getProducer() {
        checkAndInitialize();
        return s_instance.m_producer;
    }

    /**
     * @param configFile cat client的配置文件
     */
    public static void initialize(File configFile) {
        PlexusContainer container = ContainerLoader.getDefaultContainer();
        initialize(container, configFile);
    }

    public static void initialize(PlexusContainer container, File configFile) {
        ModuleContext ctx = new DefaultModuleContext(container);
        Module module = ctx.lookup(Module.class, "cat-client");
        if (!module.isInitialized()) {
            ModuleInitializer initializer = ctx.lookup(ModuleInitializer.class);
            ctx.setAttribute("cat-client-config-file", configFile);
            initializer.execute(ctx, module);
        }
    }

    public static void initialize(String... servers) {
        File configFile = null;
        try {
            configFile = File.createTempFile("cat-client", ".xml");
            ClientConfig config = (new ClientConfig()).setMode("client");
            for (String server : servers) {
                config.addServer(new Server(server));
            }
            Files.forIO().writeTo(configFile, config.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        initialize(configFile);
    }

    public static boolean isInitialized() {
        synchronized (s_instance) {
            return s_instance.m_container != null;
        }
    }

    static void log(String severity, String message) {
        MessageFormat format = new MessageFormat("[{0,date,MM-dd HH:mm:ss.sss}] [{1}] [{2}] {3}");
        System.out.println(format.format(new Object[] {new Date(), severity, "cat", message}));
    }

    public static void logError(String message, Throwable cause) {
        getProducer().logError(message, cause);
    }

    public static void logError(Throwable cause) {
        getProducer().logError(cause);
    }

    public static void logEvent(String type, String name) {
        getProducer().logEvent(type, name);
    }

    public static void logEvent(String type, String name, String status, String nameValuePairs) {
        getProducer().logEvent(type, name, status, nameValuePairs);
    }

    public static void logHeartbeat(String type, String name, String status, String nameValuePairs) {
        getProducer().logHeartbeat(type, name, status, nameValuePairs);
    }

    public static void logMetric(String name, Object... keyValues) {
    }

    public static void logMetricForCount(String name) {
        logMetricInternal(name, "C", "1");
    }

    public static void logMetricForCount(String name, int quantity) {
        logMetricInternal(name, "C", String.valueOf(quantity));
    }

    public static void logMetricForDuration(String name, long durationInMillis) {
        logMetricInternal(name, "T", String.valueOf(durationInMillis));
    }

    public static void logMetricForSum(String name, double value) {
        logMetricInternal(name, "S", String.format("%.2f", value));
    }

    public static void logMetricForSum(String name, double sum, int quantity) {
        logMetricInternal(name, "S,C", String.format("%s,%.2f", quantity, sum));
    }

    private static void logMetricInternal(String name, String status, String keyValuePairs) {
        getProducer().logMetric(name, status, keyValuePairs);
    }

    public static void logRemoteCallClient(Cat.Context ctx) {
        MessageTree tree = getManager().getThreadLocalMessageTree();
        String messageId = tree.getMessageId();
        if (messageId == null) {
            messageId = createMessageId();
            tree.setMessageId(messageId);
        }
        String childId = createMessageId();
        logEvent("RemoteCall", "", "0", childId);
        String root = tree.getRootMessageId();
        if (root == null) {
            root = messageId;
        }
        ctx.addProperty("_catRootMessageId", root);
        ctx.addProperty("_catParentMessageId", messageId);
        ctx.addProperty("_catChildMessageId", childId);
    }

    public static void logRemoteCallServer(Cat.Context ctx) {
        MessageTree tree = getManager().getThreadLocalMessageTree();
        String messageId = ctx.getProperty("_catChildMessageId");
        String rootId = ctx.getProperty("_catRootMessageId");
        String parentId = ctx.getProperty("_catParentMessageId");
        if (messageId != null) {
            tree.setMessageId(messageId);
        }
        if (parentId != null) {
            tree.setParentMessageId(parentId);
        }
        if (rootId != null) {
            tree.setRootMessageId(rootId);
        }
    }

    public static void logTrace(String type, String name) {
        getProducer().logTrace(type, name);
    }

    public static void logTrace(String type, String name, String status, String nameValuePairs) {
        getProducer().logTrace(type, name, status, nameValuePairs);
    }

    public static <T> T lookup(Class<T> role) throws ComponentLookupException {
        return lookup(role, (String) null);
    }

    public static <T> T lookup(Class<T> role, String hint) throws ComponentLookupException {
        return s_instance.m_container.lookup(role, hint);
    }

    public static Event newEvent(String type, String name) {
        return getProducer().newEvent(type, name);
    }

    public static ForkedTransaction newForkedTransaction(String type, String name) {
        return getProducer().newForkedTransaction(type, name);
    }

    public static Heartbeat newHeartbeat(String type, String name) {
        return getProducer().newHeartbeat(type, name);
    }

    public static TaggedTransaction newTaggedTransaction(String type, String name, String tag) {
        return getProducer().newTaggedTransaction(type, name, tag);
    }

    public static Trace newTrace(String type, String name) {
        return getProducer().newTrace(type, name);
    }

    public static Transaction newTransaction(String type, String name) {
        return getProducer().newTransaction(type, name);
    }

    public static void reset() {
    }

    public static void setup(String sessionToken) {
        getManager().setup();
    }

    private Cat() {
    }

    void setContainer(PlexusContainer container) {
        try {
            this.m_container = container;
            this.m_manager = container.lookup(MessageManager.class);
            this.m_producer = container.lookup(MessageProducer.class);
        } catch (ComponentLookupException var3) {
            throw new RuntimeException("Unable to get instance of MessageManager, please make sure the environment was setup correctly!", var3);
        }
    }

    public interface Context {
        String ROOT = "_catRootMessageId";
        String PARENT = "_catParentMessageId";
        String CHILD = "_catChildMessageId";

        void addProperty(String k, String v);

        String getProperty(String k);
    }
}
