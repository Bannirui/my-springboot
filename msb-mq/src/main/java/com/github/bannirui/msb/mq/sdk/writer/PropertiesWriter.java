package com.github.bannirui.msb.mq.sdk.writer;

import com.google.common.collect.Maps;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class PropertiesWriter {
    private final Map<String, String> props = Maps.newTreeMap();

    public static PropertiesWriter newInstance() {
        PropertiesWriter fileUtil = new PropertiesWriter();
        return fileUtil;
    }

    public PropertiesWriter add(String key, String value) {
        this.props.put(key, value);
        return this;
    }

    public void store(OutputStream outputStream) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        this.props.forEach((key, value) -> {
        });
        for (Map.Entry<String, String> entry : this.props.entrySet()) {
            bufferedWriter.write(entry.getKey() + "=" + entry.getValue());
            bufferedWriter.newLine();
        }
        bufferedWriter.flush();
    }
}
