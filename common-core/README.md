### 关键类

`getter`, `setter`等由 **lombok** 自动生成

#### 数据模型
`com.bit.examsystem.common.model`
- Question: 
  - String id, String title, QuestionType type, Integer score
  - 选择题：List<String> options
  - 敏感字段：String correctAnswer
- QuestionType: SINGLE_CHOICE, MULTI_CHOICE, JUDGE, FILL_IN
- ExamPaper: 
  - String examId, String title, Integer durationMinutes, Long startTime, List<Question> questions
- Student:
  - String id, String name, String ip, boolean isOnline
- StudentAnswer:
  - String questionId, String answer

#### DTO (Data Transfer Object)
`com.bit.examsystem.common.dto`
- QuestionDTO: 与 Question 相比无 String correctAnswer
- ExamPaperDTO: 题目列表使用 DTO 类 (List<QuestionDTO> questions)

#### 通信协议
`com.bit.examsystem.common.message`
- Message<T>: MessageType type, Long timestamp, T body
  - 构造方法：快速构造`public Message() {}`和完整构造
```java
    public Message(MessageType type, T body) {
        this.type = type;
        this.body = body;
        this.timestamp = System.currentTimeMillis();
    }
```
- MessageType: 
  - LOGIN_REQ, LOGIN_RESP
  - EXAM_WAITING, EXAM_START, EXAM_END
  - ANSWER_SUBMIT, RESULT_PUB
  - HEARTBEAT

#### JSON序列化/反序列化工具
- public static String toJson(Object object): 将对象转换为 JSON 字符串
- public static <T> T fromJson(String json, Class<T> clazz): 将 JSON 字符串转换为 Java 对象
- public static <T> T fromJson(String json, TypeReference<T> typeReference)
  - 将 JSON 字符串转换为复杂类型对象 (如 List<Question>, Message<LoginReq> 等泛型)
  - 使用示例: List<Question> list = JsonUtil.fromJson(json, new TypeReference<List<Question>>(){});
- A public static <T> T convert(Object fromValue, Class<T> toValueType)
  - 类型转换：将 Map/LinkedHashMap 转换为具体的 Java Bean
  - 场景：处理 `Message<Object>` 时，body 是 Map，需要转为具体对象
- B public static <T> T convert(Object fromValue, TypeReference<T> toValueTypeRef)
  - 类型转换：将 Map/LinkedHashMap 转换为复杂的泛型对象 (如 List<Question>)

#### 解编码器
- MessageEncoder: 将 Message<?> 对象编码为：[4字节长度] + [JSON字节数组]
  - void encode(ChannelHandlerContext ctx, Message<?> msg, ByteBuf out)
- MessageDecoder: 将 JSON 字符串的 ByteBuf 解码为 `Message<Object>` 对象
  - `void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)`
- `ProtocolInitializer`: 用于统一 Client 和 Server 的 Pipeline 结构
- `void initChannel(SocketChannel ch)` 

### 最佳实践
Handler 接受解码器传来的`Message<Object>`, 获取消息类型，在每个case分支中明确已知body类型，使用JsonUtil.convert()转换为具体对象。

```java
// package com.bit.examsystem.student.network; // 假设在学生端

import com.bit.examsystem.common.dto.ExamPaperDTO;
import com.bit.examsystem.common.model.Message;
import com.bit.examsystem.common.model.MessageType;
import com.bit.examsystem.common.util.JsonUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

// 关键：Handler 的泛型参数是 Message<Object>，与解码器输出一致
public class StudentBusinessHandler extends SimpleChannelInboundHandler<Message<Object>> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message<Object> msg) throws Exception {
        MessageType type = msg.getType();

        // 使用 switch 来分发消息
        switch (type) {
            case LOGIN_RESP:
                // msg.getBody() 此刻是一个 LinkedHashMap，或者简单类型(Boolean/String)
                Boolean isSuccess = JsonUtil.convert(msg.getBody(), Boolean.class);
                if (isSuccess) {
                    System.out.println("登录成功！");
                } else {
                    // 如果失败，body 可能是 String
                    String errorMsg = JsonUtil.convert(msg.getBody(), String.class);
                    System.out.println("登录失败: " + errorMsg);
                }
                break;

            case EXAM_START:
                // 这是处理复杂泛型的核心！
                // 将 msg.getBody() (一个 LinkedHashMap) 转换为我们期望的 ExamPaperDTO 对象
                ExamPaperDTO examPaper = JsonUtil.convert(msg.getBody(), ExamPaperDTO.class);
                System.out.println("考试开始！试卷标题：" + examPaper.getTitle());
                System.out.println("第一题：" + examPaper.getQuestions().get(0).getTitle());
                // ... 接下来调用 Controller 更新 UI ...
                break;
            
            case RESULT_PUB:
                Integer score = JsonUtil.convert(msg.getBody(), Integer.class);
                System.out.println("您的成绩是：" + score);
                break;
            
            case HEARTBEAT:
                // 收到心跳，可以忽略或回复
                System.out.println("Received heartbeat from server.");
                break;

            // ... 处理其他消息类型
            default:
                System.out.println("收到未知类型的消息: " + type);
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```