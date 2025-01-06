package com.dianping.cat.status;

import java.util.ArrayList;
import java.util.List;

public class StatusExtensionRegister {
    private List<StatusExtension> m_extensions = new ArrayList<>();
    public static StatusExtensionRegister s_register = new StatusExtensionRegister();

    public static StatusExtensionRegister getInstance() {
        return s_register;
    }

    public List<StatusExtension> getStatusExtension() {
        synchronized (this) {
            return this.m_extensions;
        }
    }

    public void register(StatusExtension monitor) {
        synchronized (this) {
            this.m_extensions.add(monitor);
        }
    }

    public void unregister(StatusExtension monitor) {
        synchronized (this) {
            this.m_extensions.remove(monitor);
        }
    }
}
