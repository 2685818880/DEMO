package cn.zdjc.wms.project.erp.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.Collection;

/**
 * 统一API返回结果封装类
 */
@Data
public class ResultWrapper<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 操作是否成功 */
    private boolean success;

    /** 状态码 */
    private String code;

    /** 消息 */
    private String message;

    /** 数据 */
    private T data;


    public ResultWrapper() {}

    protected ResultWrapper(T data) {
        this.data = data;
    }

    public static <T> ResultWrapper<T> buildFailure( String errMessage) {
        ResultWrapper<T> response = new ResultWrapper<>();
        response.setSuccess(false);
        response.setCode("500");
        response.setMessage(errMessage);
        return response;
    }

    public static <T> ResultWrapper<T> buildFailure(String errCode, String errMessage) {
        ResultWrapper<T> response = new ResultWrapper<>();
        response.setSuccess(false);
        response.setCode(errCode);
        response.setMessage(errMessage);
        return response;
    }
    public static <T> ResultWrapper<T> buildSuccess() {
        ResultWrapper<T> response = new ResultWrapper<>(null);
        response.setSuccess(true);
        response.setCode("200");
        response.setMessage("操作成功");
        return response;
    }

    public static <T> ResultWrapper<T> buildSuccess(T data) {
        ResultWrapper<T> response = new ResultWrapper<>(data);
        response.setSuccess(true);
        response.setCode("200");
        response.setMessage("操作成功");
        return response;
    }
    public static <T> ResultWrapper<T> buildSuccess(T data, String msg) {
        ResultWrapper<T> response = new ResultWrapper<>(data);
        response.setSuccess(true);
        response.setCode("200");
        response.setMessage("操作成功");
        return response;
    }
    public boolean isCollection() {
        return this.data instanceof Collection;
    }
}