package cn.zdjc.wms.project.control.ai;

import cn.zdjc.wms.project.ai.dto.AiProviderDTO;
import cn.zdjc.wms.project.ai.dto.AiProviderVO;
import cn.zdjc.wms.project.ai.service.AiModelService;
import cn.zdjc.wms.project.ai.service.AiProviderService;
import cn.zdjc.wms.project.common.constant.ProjectConstant;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ProjectConstant.PREFIX_CONTEXT_PATH + "/ai/provider")
@Slf4j
public class AiProviderController {

    private static final String LOG_PREFIX = "[AiProviderController] ";

    private final AiProviderService aiProviderService;
    private final AiModelService aiModelService;

    public AiProviderController(AiProviderService aiProviderService, AiModelService aiModelService) {
        this.aiProviderService = aiProviderService;
        this.aiModelService = aiModelService;
    }

    @GetMapping("/list")
    public ResultWrapper<List<AiProviderVO>> list(@RequestParam(required = false) String keyword) {
        log.info("{}查询提供商列表, keyword={}", LOG_PREFIX, keyword);
        return ResultWrapper.buildSuccess(aiProviderService.listProviders(keyword));
    }

    @GetMapping("/{id}")
    public ResultWrapper<AiProviderVO> detail(@PathVariable Long id) {
        return ResultWrapper.buildSuccess(aiProviderService.getProviderDetail(id));
    }

    @PostMapping("/create")
    public ResultWrapper<Void> create(@RequestBody AiProviderDTO dto) {
        log.info("{}新增提供商: {}", LOG_PREFIX, dto.getName());
        aiProviderService.createProvider(dto);
        return ResultWrapper.buildSuccess();
    }

    @PutMapping("/{id}")
    public ResultWrapper<Void> update(@PathVariable Long id, @RequestBody AiProviderDTO dto) {
        log.info("{}更新提供商: id={}", LOG_PREFIX, id);
        aiProviderService.updateProvider(id, dto);
        return ResultWrapper.buildSuccess();
    }

    @DeleteMapping("/{id}")
    public ResultWrapper<Void> delete(@PathVariable Long id) {
        log.info("{}删除提供商: id={}", LOG_PREFIX, id);
        aiProviderService.deleteProvider(id);
        return ResultWrapper.buildSuccess();
    }

    @PostMapping("/{id}/api-key")
    public ResultWrapper<Void> hostApiKey(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String apiKey = body.get("apiKey");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return ResultWrapper.buildFailure("400", "API密钥不能为空");
        }
        log.info("{}托管密钥: providerId={}", LOG_PREFIX, id);
        aiProviderService.hostApiKey(id, apiKey.trim());
        return ResultWrapper.buildSuccess();
    }

    @PutMapping("/{id}/api-key")
    public ResultWrapper<Void> updateApiKey(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String apiKey = body.get("apiKey");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return ResultWrapper.buildFailure("400", "API密钥不能为空");
        }
        log.info("{}更新密钥: providerId={}", LOG_PREFIX, id);
        aiProviderService.updateApiKey(id, apiKey.trim());
        return ResultWrapper.buildSuccess();
    }

    @DeleteMapping("/{id}/api-key")
    public ResultWrapper<Void> deleteApiKey(@PathVariable Long id) {
        log.info("{}删除密钥: providerId={}", LOG_PREFIX, id);
        aiProviderService.deleteApiKey(id);
        return ResultWrapper.buildSuccess();
    }

    @PostMapping("/batch/delete")
    public ResultWrapper<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResultWrapper.buildFailure("400", "请选择要删除的提供商");
        }
        log.info("{}批量删除: ids={}", LOG_PREFIX, ids);
        aiProviderService.batchDelete(ids);
        return ResultWrapper.buildSuccess();
    }

    @PostMapping("/batch/update-key")
    public ResultWrapper<Void> batchUpdateKey(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.get("ids");
        String apiKey = (String) body.get("apiKey");
        if (ids == null || ids.isEmpty()) {
            return ResultWrapper.buildFailure("400", "请选择提供商");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return ResultWrapper.buildFailure("400", "API密钥不能为空");
        }
        log.info("{}批量更新密钥: ids={}", LOG_PREFIX, ids);
        aiProviderService.batchUpdateApiKey(ids, apiKey.trim());
        return ResultWrapper.buildSuccess();
    }

    @GetMapping("/check-key-status")
    public ResultWrapper<Void> checkAllStatus() {
        log.info("{}检查所有密钥状态", LOG_PREFIX);
        List<AiProviderVO> list = aiProviderService.listProviders(null);
        list.forEach(p -> aiProviderService.checkKeyStatus(p.getId()));
        return ResultWrapper.buildSuccess();
    }

    @GetMapping("/{id}/models")
    public ResultWrapper<List<AiProviderVO.AiModelVO>> listModels(@PathVariable Long id) {
        return ResultWrapper.buildSuccess(aiModelService.listByProvider(id));
    }

    @PutMapping("/{providerId}/models/{modelId}/toggle")
    public ResultWrapper<Void> toggleModel(@PathVariable Long modelId, @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) enabled = true;
        aiModelService.toggleModel(modelId, enabled);
        return ResultWrapper.buildSuccess();
    }

    @GetMapping("/models/{modelId}/test")
    public ResultWrapper<Map<String, Object>> testModel(@PathVariable Long modelId) {
        Map<String, Object> result = aiModelService.testModel(modelId);
        return ResultWrapper.buildSuccess(result);
    }
}
