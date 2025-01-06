package com.dianping.cat.servlet;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageManager;
import com.dianping.cat.message.internal.DefaultTransaction;
import java.io.IOException;
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
    private List<Handler> m_handlers = new ArrayList<>();

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        CatFilter.Context ctx = new CatFilter.Context((HttpServletRequest) request, (HttpServletResponse) response, chain, this.m_handlers);
        ctx.handle();
    }

    protected String getOriginalUrl(ServletRequest request) {
        return ((HttpServletRequest) request).getRequestURI();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.m_handlers.add(CatFilter.CatHandler.ENVIRONMENT);
        this.m_handlers.add(CatFilter.CatHandler.LOG_SPAN);
        this.m_handlers.add(CatFilter.CatHandler.LOG_CLIENT_PAYLOAD);
        this.m_handlers.add(CatFilter.CatHandler.ID_SETUP);
    }

    protected interface Handler {
        void handle(CatFilter.Context var1) throws IOException, ServletException;
    }

    protected static class Context {
        private FilterChain m_chain;
        private List<CatFilter.Handler> m_handlers;
        private int m_index;
        private HttpServletRequest m_request;
        private HttpServletResponse m_response;
        private boolean m_top;
        private String m_type;

        public Context(HttpServletRequest request, HttpServletResponse response, FilterChain chain, List<CatFilter.Handler> handlers) {
            this.m_request = request;
            this.m_response = response;
            this.m_chain = chain;
            this.m_handlers = handlers;
        }

        public HttpServletRequest getRequest() {
            return this.m_request;
        }

        public HttpServletResponse getResponse() {
            return this.m_response;
        }

        public String getType() {
            return this.m_type;
        }

        public void handle() throws IOException, ServletException {
            if (this.m_index < this.m_handlers.size()) {
                CatFilter.Handler handler = (CatFilter.Handler) this.m_handlers.get(this.m_index++);
                handler.handle(this);
            } else {
                this.m_chain.doFilter(this.m_request, this.m_response);
            }
        }

        public boolean isTop() {
            return this.m_top;
        }

        public void setTop(boolean top) {
            this.m_top = top;
        }

        public void setType(String type) {
            this.m_type = type;
        }
    }

    private enum CatHandler implements CatFilter.Handler {
        ENVIRONMENT {
            @Override
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
                    DefaultMessageManager manager = (DefaultMessageManager) Cat.getManager();
                    List<Server> servers = manager.getConfigManager().getServers();
                    this.m_servers = Joiners.by(',').join(servers, server -> {
                        String ip = server.getIp();
                        Integer httpPort = server.getHttpPort();
                        return ip + ":" + httpPort;
                    });
                }
                return this.m_servers;
            }

            @Override
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
            @Override
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
                Cat.logEvent(type, type + ".Server", "0", sb.toString());
            }

            protected void logRequestPayload(HttpServletRequest req, String type) {
                StringBuilder sb = new StringBuilder(256);
                sb.append(req.getScheme().toUpperCase()).append('/');
                sb.append(req.getMethod()).append(' ').append(req.getRequestURI());
                String qs = req.getQueryString();
                if (qs != null) {
                    sb.append('?').append(qs);
                }
                Cat.logEvent(type, type + ".Method", "0", sb.toString());
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
                if (t instanceof DefaultTransaction tt) {
                    Object catPageType = req.getAttribute("cat-page-type");
                    if (catPageType instanceof String) {
                        tt.setType(catPageType.toString());
                    }
                    Object catPageUri = req.getAttribute("cat-page-uri");
                    if (catPageUri instanceof String) {
                        tt.setName(catPageUri.toString());
                    }
                }
            }

            private String getRequestURI(HttpServletRequest req) {
                String url = req.getRequestURI();
                int length = url.length();
                StringBuilder sb = new StringBuilder(length);
                int index = 0;
                while (true) {
                    while (true) {
                        while (index < length) {
                            char c = url.charAt(index);
                            if (c == '/' && index < length - 1) {
                                sb.append(c);
                                StringBuilder nextSection = new StringBuilder();
                                boolean isNumber = false;
                                boolean first = true;
                                for (int j = index + 1; j < length; ++j) {
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

            @Override
            public void handle(CatFilter.Context ctx) throws IOException, ServletException {
                HttpServletRequest req = ctx.getRequest();
                Transaction t = Cat.newTransaction(ctx.getType(), this.getRequestURI(req));
                try {
                    ctx.handle();
                    this.customizeStatus(t, req);
                } catch (ServletException var10) {
                    t.setStatus(var10);
                    Cat.logError(var10);
                    throw var10;
                } catch (IOException var11) {
                    t.setStatus(var11);
                    Cat.logError(var11);
                    throw var11;
                } catch (Throwable var12) {
                    t.setStatus(var12);
                    Cat.logError(var12);
                    throw new RuntimeException(var12);
                } finally {
                    this.customizeUri(t, req);
                    t.complete();
                }
            }

            private boolean isNumber(char c) {
                return c >= '0' && c <= '9' || c == '.' || c == '-' || c == ',';
            }
        };

        CatHandler() {
        }
    }
}
