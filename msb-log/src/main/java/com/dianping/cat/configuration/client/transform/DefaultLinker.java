package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.entity.Bind;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.entity.Server;
import java.util.ArrayList;
import java.util.List;

public class DefaultLinker implements ILinker {

    private boolean m_deferrable;
    private List<Runnable> m_deferedJobs = new ArrayList<>();

    public DefaultLinker(boolean deferrable) {
        this.m_deferrable = deferrable;
    }

    public void finish() {
        this.m_deferedJobs.forEach(Runnable::run);
    }

    @Override
    public boolean onBind(ClientConfig parent, Bind bind) {
        parent.setBind(bind);
        return true;
    }

    @Override
    public boolean onDomain(final ClientConfig parent, final Domain domain) {
        if (this.m_deferrable) {
            this.m_deferedJobs.add(() -> parent.addDomain(domain));
        } else {
            parent.addDomain(domain);
        }
        return true;
    }

    @Override
    public boolean onProperty(final ClientConfig parent, final Property property) {
        if (this.m_deferrable) {
            this.m_deferedJobs.add(() -> parent.addProperty(property));
        } else {
            parent.addProperty(property);
        }
        return true;
    }

    @Override
    public boolean onServer(ClientConfig parent, Server server) {
        parent.addServer(server);
        return true;
    }
}
