package com.github.bannirui.msb.dubbo.config;

public class MultipleProperties {
    private String registryAddress;
    private boolean use;

    public String getRegistryAddress() {
        return this.registryAddress;
    }

    public boolean isUse() {
        return this.use;
    }

    public void setRegistryAddress(final String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setUse(final boolean use) {
        this.use = use;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof MultipleProperties)) {
            return false;
        } else {
            MultipleProperties other = (MultipleProperties)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$registryAddress = this.getRegistryAddress();
                Object other$registryAddress = other.getRegistryAddress();
                if (this$registryAddress == null) {
                    if (other$registryAddress == null) {
                        return this.isUse() == other.isUse();
                    }
                } else if (this$registryAddress.equals(other$registryAddress)) {
                    return this.isUse() == other.isUse();
                }

                return false;
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof MultipleProperties;
    }

    @Override
    public int hashCode() {
        int result = 1;
        Object $registryAddress = this.getRegistryAddress();
        result = result * 59 + ($registryAddress == null ? 43 : $registryAddress.hashCode());
        result = result * 59 + (this.isUse() ? 79 : 97);
        return result;
    }

    @Override
    public String toString() {
        return "MultipleProperties(registryAddress=" + this.getRegistryAddress() + ", use=" + this.isUse() + ")";
    }
}
