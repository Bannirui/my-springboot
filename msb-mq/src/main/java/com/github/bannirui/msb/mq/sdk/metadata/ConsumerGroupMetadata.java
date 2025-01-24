package com.github.bannirui.msb.mq.sdk.metadata;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class ConsumerGroupMetadata extends MmsMetadata {
    private String bindingTopic;
    private String consumeFrom;
    private String broadcast;
    private String suspend = "false";
    private String releaseStatus;

    public String getConsumeFrom() {
        return this.consumeFrom;
    }

    public void setConsumeFrom(String consumeFrom) {
        this.consumeFrom = consumeFrom;
    }

    public String getBroadcast() {
        return this.broadcast;
    }

    public void setBroadcast(String broadcast) {
        this.broadcast = broadcast;
    }

    public String getBindingTopic() {
        return this.bindingTopic;
    }

    public void setBindingTopic(String bindingTopic) {
        this.bindingTopic = bindingTopic;
    }

    public String getSuspend() {
        return this.suspend;
    }

    public void setSuspend(String suspend) {
        this.suspend = suspend;
    }

    public boolean needSuspend() {
        return StringUtils.isNotBlank(this.suspend) && "true".equalsIgnoreCase(this.suspend);
    }

    public String getReleaseStatus() {
        return this.releaseStatus;
    }

    public void setReleaseStatus(String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    public boolean suspendChange(ConsumerGroupMetadata metadata) {
        return this.bindingTopic.equals(metadata.getBindingTopic()) && this.consumeFrom.equals(metadata.getConsumeFrom()) && this.broadcast.equals(metadata.getBroadcast()) && !this.suspend.equals(metadata.getSuspend()) && super.equals(metadata);
    }

    public String toString() {
        return "ConsumerGroupMetadata{bindingTopic='" + this.bindingTopic + '\'' + ", consumeFrom='" + this.consumeFrom + '\'' + ", broadcast='" + this.broadcast + '\'' + ", suspend='" + this.suspend + '\'' + ", releaseStatus=" + this.releaseStatus + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ConsumerGroupMetadata that = (ConsumerGroupMetadata)o;
            return Objects.equals(this.bindingTopic, that.bindingTopic) && Objects.equals(this.consumeFrom, that.consumeFrom) && Objects.equals(this.broadcast, that.broadcast) && Objects.equals(this.suspend, that.suspend) && Objects.equals(this.releaseStatus, that.releaseStatus) && super.equals(that);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hashCode(new Object[]{this.bindingTopic, this.consumeFrom, this.broadcast, this.suspend, this.getType(), this.getName(), this.getClusterMetadata(), this.getDomain()});
    }
}
