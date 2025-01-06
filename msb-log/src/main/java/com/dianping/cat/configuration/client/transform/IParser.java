package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.entity.Bind;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.entity.Server;

public interface IParser<T> {

    ClientConfig parse(IMaker<T> maker, ILinker linker, T t);

    void parseForBind(IMaker<T> maker, ILinker linker, Bind bind, T t);

    void parseForDomain(IMaker<T> maker, ILinker linker, Domain domain, T t);

    void parseForProperty(IMaker<T> maker, ILinker linker, Property property, T t);

    void parseForServer(IMaker<T> maker, ILinker linker, Server server, T t);
}
