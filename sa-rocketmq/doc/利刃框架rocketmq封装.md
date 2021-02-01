# 1 概述
RocketMQ是alibaba公司开源的一个纯java的开源消息中间件。

# 2 开发环境搭建
## 2.1 maven依赖
```
<!-- https://mvnrepository.com/artifact/com.alibaba.rocketmq/rocketmq-client -->
<dependency>
    <groupId>com.alibaba.rocketmq</groupId>
    <artifactId>rocketmq-client</artifactId>
    <version>3.6.2.Final</version>
</dependency>
```

## 2.2 服务端代码示例
```
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.remoting.common.RemotingHelper;
import RocketMQProducer;
import RocketMqException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * Created by asiamaster on 2017/11/7 0007.
 */
@Component
public class TopicProducer {

    @Autowired
    private RocketMQProducer rocketMQProducer;

    public void send(final String destination, final String tag, final String message){
        try {
            Message mesg = new Message(destination, tag, message.getBytes(RemotingHelper.DEFAULT_CHARSET));
            rocketMQProducer.sendMsg(mesg);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }catch (RocketMqException e) {
            System.out.println(String.format("发送MQ异常！TOPIC=%s,内容=%s", destination, message));
        }
    }
}
```

## 2.3 客户端代码示例
```
import com.alibaba.rocketmq.common.message.MessageExt;
import RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by asiamaster on 2017/11/7 0007.
 */
@Component
public class MsgListener implements RocketMQListener {

    protected static final Logger log = LoggerFactory.getLogger(MsgListener.class);
    @Override
    public String getTopic() {
        return "testTopic";
    }

    @Override
    public String getTags() {
        return "tag";
    }

    @Override
    public void operate(MessageExt messageExt) {
        if (messageExt == null) {
            log.warn("listener AttributeMessageHandler content is null");
            return;
        }
        try {
            byte[] bytesMessage = messageExt.getBody();
            String json = new String(bytesMessage,"UTF-8");
            log.info("==============================================");
            log.info("监听收到数据:"+json);
            log.info("==============================================");
        } catch (Exception e) {
            log.error("listener AttributeMessageHandler Exception:%s",
                    e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
```

## 2.4 客户端配置示例
客户端需要配置nameserver地址和消费分组
框架消费者采用集群消费和推送形式，所以在同一个分组(group)内，消息会按队列分别消费，
而广播模式的消息则会分发给所有消费者，后期会通过配置文件进行扩展
```
mq.namesrvAddr=127.0.0.1:9876
mq.producerGroup=group
```

**小科谱一下:**
Producer/Consumer Group的作用

Producer Group：

(1)、可以通过运维工具查询这个组下有多少Producer实例，命令：sh mqadmin producerConnection；

(2)、事务消息，如果Producer意外宕机，Broker会主动回调Producer Group中的任意一台机器确认事务状态。
Consumer Group:

(3)、可通过运维工具查询这个组下的消费进度,多少个Consumer实例，命令：sh mqadmin consumerProgress/consumerConnection -g xxx

(4)、集群模式，一个Consumer Group下的多个Consumer均摊消费消息；广播模式，group无意义。