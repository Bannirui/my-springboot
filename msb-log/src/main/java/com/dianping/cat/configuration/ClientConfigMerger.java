package com.dianping.cat.configuration;

import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.transform.DefaultMerger;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class ClientConfigMerger extends DefaultMerger {

    public ClientConfigMerger(ClientConfig config) {
        super(config);
    }

    @Override
    protected void mergeDomain(Domain old, Domain domain) {
        if (domain.getIp() != null) {
            old.setIp(domain.getIp());
        }
        if (domain.getEnabled() != null) {
            old.setEnabled(domain.getEnabled());
        }
        if (domain.getMaxMessageSize() > 0) {
            old.setMaxMessageSize(domain.getMaxMessageSize());
        }
    }

    @Override
    protected void visitConfigChildren(ClientConfig to, ClientConfig from) {
        if (to != null) {
            Stack<Object> objs = this.getObjects();
            if (!from.getServers().isEmpty()) {
                to.getServers().clear();
                to.getServers().addAll(from.getServers());
            }
            Map<String, Domain> domainMap = from.getDomains();
            if(Objects.nonNull(domainMap)) {
                domainMap.forEach((k,v)->{
                    Domain target = to.findDomain(v.getId());
                    if (target == null) {
                        target = new Domain(v.getId());
                        to.addDomain(target);
                    }
                    if (to.getDomains().containsKey(v.getId())) {
                        objs.push(target);
                        v.accept(this);
                        objs.pop();
                    }
                });
            }

            Map<String, Property> properties = from.getProperties();
            if(Objects.nonNull(properties)) {
                properties.forEach((k,v)->{
                    Property target = to.findProperty(v.getName());
                    if (target == null) {
                        target = new Property(v.getName());
                        to.addProperty(target);
                    }
                    objs.push(target);
                    v.accept(this);
                    objs.pop();
                });
            }
        }
    }
}
