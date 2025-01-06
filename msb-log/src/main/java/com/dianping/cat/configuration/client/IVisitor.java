package com.dianping.cat.configuration.client;

import com.dianping.cat.configuration.client.entity.Bind;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.entity.Server;

public interface IVisitor {

    void visitBind(Bind bind);

    void visitConfig(ClientConfig config);

    void visitDomain(Domain domain);

    void visitProperty(Property property);

    void visitServer(Server server);
}
