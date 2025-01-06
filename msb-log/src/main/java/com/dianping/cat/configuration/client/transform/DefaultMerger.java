package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.IEntity;
import com.dianping.cat.configuration.client.IVisitor;
import com.dianping.cat.configuration.client.entity.Bind;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.entity.Server;
import java.util.Stack;

public class DefaultMerger implements IVisitor {
    private Stack<Object> m_objs = new Stack<>();
    private ClientConfig m_config;

    public DefaultMerger(ClientConfig config) {
        this.m_config = config;
        this.m_objs.push(config);
    }

    public ClientConfig getConfig() {
        return this.m_config;
    }

    protected Stack<Object> getObjects() {
        return this.m_objs;
    }

    public <T> void merge(IEntity<T> to, IEntity<T> from) {
        this.m_objs.push(to);
        from.accept(this);
        this.m_objs.pop();
    }

    protected void mergeBind(Bind to, Bind from) {
        to.mergeAttributes(from);
    }

    protected void mergeConfig(ClientConfig to, ClientConfig from) {
        to.mergeAttributes(from);
        to.setBaseLogDir(from.getBaseLogDir());
    }

    protected void mergeDomain(Domain to, Domain from) {
        to.mergeAttributes(from);
    }

    protected void mergeProperty(Property to, Property from) {
        to.mergeAttributes(from);
        to.setText(from.getText());
    }

    protected void mergeServer(Server to, Server from) {
        to.mergeAttributes(from);
    }

    protected void visitBindChildren(Bind to, Bind from) {
    }

    protected void visitConfigChildren(ClientConfig to, ClientConfig from) {
        from.getServers().forEach(x -> {
            Server target = to.findServer(x.getIp());
            if (target == null) {
                target = new Server(x.getIp());
                to.addServer(target);
            }
            this.m_objs.push(target);
            x.accept(this);
            this.m_objs.pop();
        });
        from.getDomains().values().forEach(x -> {
            Domain target = to.findDomain(x.getId());
            if (target == null) {
                target = new Domain(x.getId());
                to.addDomain(target);
            }
            this.m_objs.push(target);
            x.accept(this);
            this.m_objs.pop();
        });
        if (from.getBind() != null) {
            Bind target = to.getBind();
            if (target == null) {
                target = new Bind();
                to.setBind(target);
            }
            this.m_objs.push(target);
            from.getBind().accept(this);
            this.m_objs.pop();
        }
        from.getProperties().forEach((s, property) -> {
            Property target = to.findProperty(property.getName());
            if (target == null) {
                target = new Property(property.getName());
                to.addProperty(target);
            }
            this.m_objs.push(target);
            property.accept(this);
            this.m_objs.pop();
        });
    }

    protected void visitDomainChildren(Domain to, Domain from) {
    }

    protected void visitPropertyChildren(Property to, Property from) {
    }

    protected void visitServerChildren(Server to, Server from) {
    }

    @Override
    public void visitBind(Bind from) {
        Bind to = (Bind) this.m_objs.peek();
        this.mergeBind(to, from);
        this.visitBindChildren(to, from);
    }

    @Override
    public void visitConfig(ClientConfig from) {
        ClientConfig to = (ClientConfig) this.m_objs.peek();
        this.mergeConfig(to, from);
        this.visitConfigChildren(to, from);
    }

    @Override
    public void visitDomain(Domain from) {
        Domain to = (Domain) this.m_objs.peek();
        this.mergeDomain(to, from);
        this.visitDomainChildren(to, from);
    }

    @Override
    public void visitProperty(Property from) {
        Property to = (Property) this.m_objs.peek();
        this.mergeProperty(to, from);
        this.visitPropertyChildren(to, from);
    }

    @Override
    public void visitServer(Server from) {
        Server to = (Server) this.m_objs.peek();
        this.mergeServer(to, from);
        this.visitServerChildren(to, from);
    }
}
