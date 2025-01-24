package com.github.bannirui.msb.sample;

import com.github.bannirui.msb.common.annotation.EnableMsbFramework;
import com.github.bannirui.msb.config.annotation.EnableMsbConfig;
import com.github.bannirui.msb.log.annotation.EnableMsbLog;
import com.github.bannirui.msb.mq.annotation.EnableMsbMQ;
import com.github.bannirui.msb.mq.annotation.MMSListener;
import com.github.bannirui.msb.mq.annotation.MMSListenerParameter;
import com.github.bannirui.msb.mq.configuration.MMSTemplate;
import com.github.bannirui.msb.mq.enums.MQMsgEnum;
import com.github.bannirui.msb.mq.enums.MMSResult;
import com.github.bannirui.msb.mq.sdk.consumer.MsgConsumedStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;

@EnableMsbFramework
@EnableMsbConfig
@EnableMsbLog
@EnableMsbMQ
public class App06 implements ApplicationRunner {

    @Autowired
    MMSTemplate mmsTemplate;

    public static void main(String[] args) {
        SpringApplication.run(App06.class, args);
    }

    @MMSListener(consumerGroup = "a")
    public MMSResult listen(
                                 @MMSListenerParameter(name = MQMsgEnum.TAG) String tag,
                                 @MMSListenerParameter(name = MQMsgEnum.BODY) String body,
                                 @MMSListenerParameter(name = MQMsgEnum.RECONSUME_TIMES) String reconsumeTimes
    ) {
        System.out.println(body);
        return MMSResult.status(MsgConsumedStatus.SUCCEED);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // this.mmsTemplate.send("a", "1", 1);
    }
}
