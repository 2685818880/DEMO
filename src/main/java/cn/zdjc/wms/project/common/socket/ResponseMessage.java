package cn.zdjc.wms.project.common.socket;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * WebSocket响应消息
 */
@Data
@NoArgsConstructor
public class ResponseMessage implements Serializable {

    private String type;        // 消息类型
    private Integer code;       // 状态码
    private String message;     // 消息内容
    private Object data;        // 数据
    private Long timestamp;     // 时间戳

    public ResponseMessage(String type, Integer code, String message, Object data) {
        this.type = type;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应
     */
    public static ResponseMessage success(String message, Object data) {
        return new ResponseMessage("success", 200, message, data);
    }

    /**
     * 错误响应
     */
    public static ResponseMessage error(String message) {
        return new ResponseMessage("error", 500, message, null);
    }

    /**
     * 心跳响应
     */
    public static ResponseMessage pong() {
        return new ResponseMessage("pong", 200, "pong", null);
    }

    /**
     * 通知消息
     */
    public static ResponseMessage notify(String notifyType, Object data) {
        ResponseMessage msg = new ResponseMessage("notify", 200, notifyType, data);
        msg.setType(notifyType);
        return msg;
    }
}