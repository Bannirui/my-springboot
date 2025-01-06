package com.dianping.cat.configuration.client.transform;

import com.dianping.cat.configuration.client.entity.Bind;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Property;
import com.dianping.cat.configuration.client.entity.Server;

public interface IMaker<T> {

    Bind buildBind(T t);

    ClientConfig buildConfig(T t);

    Domain buildDomain(T t);

    Property buildProperty(T t);

    Server buildServer(T t);
}
