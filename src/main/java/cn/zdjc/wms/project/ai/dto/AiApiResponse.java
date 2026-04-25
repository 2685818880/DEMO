package cn.zdjc.wms.project.ai.dto;

import lombok.Data;

@Data
public class AiApiResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;
}
