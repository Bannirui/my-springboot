package com.github.bannirui.msb.mq.sdk.writer;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import org.ini4j.Ini;
import org.ini4j.Profile;

public class IniFileWriter {
    private Ini ini;

    public static IniFileWriter newInstance() {
        IniFileWriter fileUtil = new IniFileWriter();
        fileUtil.init();
        return fileUtil;
    }

    private void init() {
        this.ini = new Ini();
        this.ini.getConfig().setEscape(false);
    }

    public IniFileWriter add(String sectionName, List<IniFileEntity> options) {
        Profile.Section section = this.ini.add(sectionName);
        options.forEach((item) -> {
            section.put(item.getKey(), item.getValue());
        });
        return this;
    }

    public void store(OutputStream outputStream) throws IOException {
        this.ini.store(outputStream);
    }

    public static void main(String[] args) throws IOException {
        IniFileWriter iniFileWriter = newInstance();
        List<IniFileWriter.IniFileEntity> list = Arrays.asList(new IniFileWriter.IniFileEntity("ip", "1.1.1.1"), new IniFileWriter.IniFileEntity("ipPort", "8567"), new IniFileWriter.IniFileEntity("isUsed", Lists.newArrayList(new String[]{"1", "2"})));
        iniFileWriter.add("section:11", list);
        iniFileWriter.store(System.out);
    }

    public static class IniFileEntity {
        private String key;
        private Object value;

        public IniFileEntity(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return this.key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
