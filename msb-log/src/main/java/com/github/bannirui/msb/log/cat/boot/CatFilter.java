package com.github.bannirui.msb.log.cat.boot;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.AbstractMessage;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.spi.MessageTree;
import com.github.bannirui.msb.common.env.MsbEnvironmentMgr;
import com.github.bannirui.msb.common.ex.BusinessException;
import com.github.bannirui.msb.log.cat.MsbCat;
import com.github.bannirui.msb.log.cat.http.HttpCatContext;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.unidal.helper.Joiners;

public class CatFilter implements Filter {

    private List<Handler> handlers = new ArrayList<>();
    private static final MsbCat catInstance = MsbCat.getInstance();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.handlers.add(CatFilter.CatHandler.ENVIRONMENT);
        this.handlers.add(CatFilter.CatHandler.LOG_SPAN);
        this.handlers.add(CatFilter.CatHandler.LOG_CLIENT_PAYLOAD);
        this.handlers.add(CatFilter.CatHandler.ID_SETUP);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)servletRequest;
        CatFilter.Context ctx = new CatFilter.Context(req, (HttpServletResponse) servletResponse, filterChain, this.handlers);
        ctx.handle();
    }

    @Override
    public void destroy() {
    }

    protected String getOriginalUrl(ServletRequest request) {
        return ((HttpServletRequest)request).getRequestURI();
    }

    private static void createProviderCross(URL url, Transaction transaction) {
        String consumerAppName = MsbEnvironmentMgr.getAppName();
        Event crossAppEvent = catInstance.newEvent("PigeonService.app", consumerAppName);
        Event crossServerEvent = catInstance.newEvent("PigeonService.client", url.getHost());
        crossAppEvent.setStatus("0");
        crossServerEvent.setStatus("0");
        completeEvent(crossAppEvent);
        completeEvent(crossServerEvent);
        transaction.addChild(crossAppEvent);
        transaction.addChild(crossServerEvent);
    }

    private static void completeEvent(Event event) {
        AbstractMessage message = (AbstractMessage)event;
        message.setCompleted(true);
    }

    protected interface Handler {
        void handle(CatFilter.Context ctx) throws IOException, ServletException;
    }

    protected static class Context {
        private FilterChain chain;
        private List<CatFilter.Handler> handlers;
        private int index;
        private HttpServletRequest request;
        private HttpServletResponse response;
        private boolean top;
        private String type;

        public Context(HttpServletRequest request, HttpServletResponse response, FilterChain chain, List<CatFilter.Handler> handlers) {
            this.request = request;
            this.response = response;
            this.chain = chain;
            this.handlers = handlers;
        }

        public HttpServletRequest getRequest() {
            return this.request;
        }

        public HttpServletResponse getResponse() {
            return this.response;
        }

        public String getType() {
            return this.type;
        }

        public void handle() throws IOException, ServletException {
            if (this.index < this.handlers.size()) {
                CatFilter.Handler handler = (CatFilter.Handler)this.handlers.get(this.index++);
                handler.handle(this);
            } else {
                this.chain.doFilter(this.request, this.response);
            }
        }

        public boolean isTop() {
            return this.top;
        }

        public void setTop(boolean top) {
            this.top = top;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    private enum CatHandler implements CatFilter.Handler {
        ENVIRONMENT {
            public void handle(CatFilter.Context ctx) throws IOException, ServletException {
                HttpServletRequest req = ctx.getRequest();
                boolean top = !Cat.getManager().hasContext();
                ctx.setTop(top);
                if (top) {
                    ctx.setType("URL");
                    this.setTraceMode(req);
                } else {
                    ctx.setType("URL.Forward");
                }
                ctx.handle();
            }

            protected void setTraceMode(HttpServletRequest req) {
                String traceMode = "X-CAT-TRACE-MODE";
                String headMode = req.getHeader(traceMode);
                if ("true".equals(headMode)) {
                    Cat.getManager().setTraceMode(true);
                }
            }
        },
        ID_SETUP {
            private String m_servers;

            private String getCatServer() {
                if (this.m_servers == null) {
                    DefaultMessageManager manager = (DefaultMessageManager)Cat.getManager();
                    List<Server> servers = manager.getConfigManager().getServers();
                    this.m_servers = Joiners.by(',').join(servers, (server) -> {
                        String ip = server.getIp();
                        Integer httpPort = server.getHttpPort();
                        return ip + ":" + httpPort;
                    });
                }
                return this.m_servers;
            }

            public void handle(CatFilter.Context ctx) throws IOException, ServletException {
                boolean isTraceMode = Cat.getManager().isTraceMode();
                HttpServletResponse res = ctx.getResponse();
                if (isTraceMode) {
                    String id = Cat.getCurrentMessageId();
                    res.setHeader("X-CAT-ROOT-ID", id);
                    res.setHeader("X-CAT-SERVER", this.getCatServer());
                }
                ctx.handle();
            }
        },
        LOG_CLIENT_PAYLOAD {
            public void handle(CatFilter.Context ctx) throws IOException, ServletException {
                HttpServletRequest req = ctx.getRequest();
                String type = ctx.getType();
                if (ctx.isTop()) {
                    this.logRequestClientInfo(req, type);
                    this.logRequestPayload(req, type);
                } else {
                    this.logRequestPayload(req, type);
                }
                ctx.handle();
            }

            protected void logRequestClientInfo(HttpServletRequest req, String type) {
                StringBuilder sb = new StringBuilder(1024);
                String ip = "";
                String ipForwarded = req.getHeader("x-forwarded-for");
                if (ipForwarded == null) {
                    ip = req.getRemoteAddr();
                } else {
                    ip = ipForwarded;
                }
                sb.append("IPS=").append(ip);
                sb.append("&VirtualIP=").append(req.getRemoteAddr());
                sb.append("&Server=").append(req.getServerName());
                sb.append("&Referer=").append(req.getHeader("referer"));
                sb.append("&Agent=").append(req.getHeader("user-agent"));
                CatFilter.catInstance.logEvent(type, type + ".Server", "0", sb.toString());
            }

            protected void logRequestPayload(HttpServletRequest req, String type) {
                StringBuilder sb = new StringBuilder(256);
                sb.append(req.getScheme().toUpperCase()).append('/');
                sb.append(req.getMethod()).append(' ').append(req.getRequestURI());
                String qs = req.getQueryString();
                if (qs != null) {
                    sb.append('?').append(qs);
                }
                CatFilter.catInstance.logEvent(type, type + ".Method", "0", sb.toString());
            }
        },
        LOG_SPAN {
            public static final char SPLIT = '/';

            private void customizeStatus(Transaction t, HttpServletRequest req) {
                Object catStatus = req.getAttribute("cat-state");
                if (catStatus != null) {
                    t.setStatus(catStatus.toString());
                } else {
                    t.setStatus("0");
                }
            }

            private void customizeUri(Transaction t, HttpServletRequest req) {
                if (t instanceof DefaultTransaction) {
                    Object catPageType = req.getAttribute("cat-page-type");
                    if (catPageType instanceof String) {
                        ((DefaultTransaction)t).setType(catPageType.toString());
                    }
                    Object catPageUri = req.getAttribute("cat-page-uri");
                    if (catPageUri instanceof String) {
                        ((DefaultTransaction)t).setName(catPageUri.toString());
                    }
                }

            }

            private String getRequestURI(HttpServletRequest req) {
                String url = req.getRequestURI();
                int length = url.length();
                StringBuilder sb = new StringBuilder(length);
                int index = 0;

                while(true) {
                    while(true) {
                        while(index < length) {
                            char c = url.charAt(index);
                            if (c == '/' && index < length - 1) {
                                sb.append(c);
                                StringBuilder nextSection = new StringBuilder();
                                boolean isNumber = false;
                                boolean first = true;

                                for(int j = index + 1; j < length; ++j) {
                                    char next = url.charAt(j);
                                    if ((first || isNumber) && next != '/') {
                                        isNumber = this.isNumber(next);
                                        first = false;
                                    }

                                    if (next == '/') {
                                        if (isNumber) {
                                            sb.append("{num}");
                                        } else {
                                            sb.append(nextSection.toString());
                                        }

                                        index = j;
                                        break;
                                    }

                                    if (j == length - 1) {
                                        if (isNumber) {
                                            sb.append("{num}");
                                        } else {
                                            nextSection.append(next);
                                            sb.append(nextSection.toString());
                                        }

                                        index = j + 1;
                                        break;
                                    }

                                    nextSection.append(next);
                                }
                            } else {
                                sb.append(c);
                                ++index;
                            }
                        }

                        return sb.toString();
                    }
                }
            }

            public void handle(CatFilter.Context ctx) throws IOException, ServletException {
                HttpServletRequest req = ctx.getRequest();
                Transaction t = CatFilter.catInstance.newTransaction(ctx.getType(), this.getRequestURI(req));
                String rootMessageId = req.getHeader("_catRootMessageId");
                String parentMessageId = req.getHeader("_catParentMessageId");
                String childMessageId = req.getHeader("_catChildMessageId");
                com.dianping.cat.Cat.Context context = new HttpCatContext();
                if (rootMessageId != null && !rootMessageId.isEmpty() && parentMessageId != null && !parentMessageId.isEmpty() && childMessageId != null && !childMessageId.isEmpty()) {
                    context.addProperty("_catRootMessageId", rootMessageId);
                    context.addProperty("_catParentMessageId", parentMessageId);
                    context.addProperty("_catChildMessageId", childMessageId);
                    Cat.logRemoteCallServer(context);
                    CatFilter.createProviderCross(new URL(req.getRequestURL().toString()), t);
                    CatFilter.catInstance.logEvent("PigeonCall.app", req.getHeader("_catClientDomainName"));
                }

                MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
                if (tree != null) {
                    String messageId = tree.getMessageId();
                    if (messageId == null) {
                        messageId = Cat.createMessageId();
                        tree.setMessageId(messageId);
                    }
                    ctx.getResponse().setHeader("X-Trace-Id", messageId);
                }

                try {
                    ctx.handle();
                    this.customizeStatus(t, req);
                } catch (ServletException var15) {
                    t.setStatus(var15);
                    Cat.logError(var15);
                    throw var15;
                } catch (IOException var16) {
                    t.setStatus(var16);
                    Cat.logError(var16);
                    throw var16;
                } catch (Throwable var17) {
                    if (BusinessException.isBusinessException(var17)) {
                        t.setStatus("0");
                    } else {
                        t.setStatus(var17);
                        Cat.logError(var17);
                    }

                    throw var17;
                } finally {
                    this.customizeUri(t, req);
                    t.complete();
                }

            }

            private boolean isNumber(char c) {
                return c >= '0' && c <= '9' || c == '.' || c == '-' || c == ',';
            }
        },
        ;
    }
}
