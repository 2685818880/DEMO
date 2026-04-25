package cn.zdjc.wms.project.ai.handler;

import cn.zdjc.wms.project.ai.client.AiApiClient;
import cn.zdjc.wms.project.ai.dto.AiApiResponse;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.function.Supplier;

/**
 * AI 服务调用处理器 - 统一的降级和错误处理
 */
@Component
@Slf4j
public class AiServiceHandler {

    @Resource
    private AiApiClient aiApiClient;

    /**
     * 执行 AI 服务调用，失败时自动降级到备用方案
     * 
     * @param logPrefix 日志前缀
     * @param apiCall API 调用函数
     * @param fallback 降级备用方案
     * @param <T> 返回类型
     * @return 统一包装结果
     */
    public <T> ResultWrapper<T> executeWithFallback(
            String logPrefix,
            Supplier<ResultWrapper<T>> apiCall,
            Supplier<ResultWrapper<T>> fallback) {
        
        try {
            ResultWrapper<T> result = apiCall.get();
            if (result != null && result.isSuccess()) {
                return result;
            }
            log.warn("{}API 调用未成功，启用降级方案", logPrefix);
            return fallback.get();
        } catch (Exception e) {
            log.error("{}AI 服务调用失败，使用降级方案", logPrefix, e);
            return fallback.get();
        }
    }

    /**
     * 执行带 API Gateway 降级的 AI 服务调用
     */
    public <T> ResultWrapper<T> executeWithGatewayFallback(
            String logPrefix,
            Supplier<T> localLogic,
            Class<T> responseType) {
        
        try {
            T result = localLogic.get();
            if (result != null) {
                return ResultWrapper.buildSuccess(result);
            }
            return ResultWrapper.buildFailure("本地处理结果为空");
        } catch (Exception e) {
            log.error("{}本地处理异常，尝试调用 API Gateway", logPrefix, e);
            try {
                AiApiResponse<T> response = aiApiClient.callGeneric(responseType);
                if (response != null && response.isSuccess() && response.getData() != null) {
                    return ResultWrapper.buildSuccess(response.getData());
                }
                return ResultWrapper.buildFailure(response != null ? response.getMessage() : "API Gateway 调用失败");
            } catch (Exception ex) {
                log.error("{}API Gateway 调用也失败，返回错误信息", logPrefix, ex);
                return ResultWrapper.buildFailure("AI 服务不可用：" + e.getMessage());
            }
        }
    }
}
