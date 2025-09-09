package com.github.bannirui.msb.sample;

import com.github.bannirui.mms.client.consumer.MsgConsumedStatus;
import com.github.bannirui.msb.annotation.EnableMsbFramework;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import com.github.bannirui.msb.log.annotation.EnableMsbLog;
import com.github.bannirui.msb.mq.annotation.EnableMsbMQ;
import com.github.bannirui.msb.mq.annotation.MMSListener;
import com.github.bannirui.msb.mq.annotation.MMSListenerParameter;
import com.github.bannirui.msb.mq.configuration.MMSTemplate;
import com.github.bannirui.msb.mq.enums.MMSResult;
import com.github.bannirui.msb.mq.enums.MQMsgEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;

@EnableMsbFramework
@EnableMsbConfig
@EnableMsbLog
@EnableMsbMQ
public class App06 implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(App06.class);
    @Autowired
    MMSTemplate mmsTemplate;

    public static void main(String[] args) {
        SpringApplication.run(App06.class, args);
    }

    @MMSListener(consumerGroup = "consumer1")
    public MMSResult listen(
                                 @MMSListenerParameter(name = MQMsgEnum.TAG) String tag,
                                 @MMSListenerParameter(name = MQMsgEnum.BODY) String body,
                                 @MMSListenerParameter(name = MQMsgEnum.RECONSUME_TIMES) String reconsumeTimes
    ) {
        log.info("业务收到MQ消息 tag={} msg={} retry_times={}", tag, body, reconsumeTimes);
        return MMSResult.status(MsgConsumedStatus.SUCCEED);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        while (true) {
            Thread.sleep(5_000L);
            // mq发送消息
            log.info("开始发送消息");
            String mid = this.mmsTemplate.send("topic1", "1", "hello.");
            log.info("业务发送MQ消息成功 mid={}", mid);
        }
    }
}
