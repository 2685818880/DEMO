package cn.zdjc.wms.project.erp.dto.requisition;

import cn.zdjc.wms.customize.domain.dto.ExtraDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * ERP 要货单 DTO
 *
 * @author lele
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErpRequisitionDto extends ExtraDto {

    @JsonProperty("FORM_ID")
    private String formId;

    @JsonProperty("HOUSE_CODE")
    private String houseCode;


    @JsonProperty("BUSINESS_FORM_NO")
    private String businessFormNo;

    @JsonProperty("BUSINESS_FORM_TYPE")
    private String businessFormType;

    @JsonProperty("CREATOR")
    private String creator;

    @JsonProperty("CREATE_DATE")
    private String createDate;

    @JsonProperty("CUSTOM_CODE")
    private String customCode;

    @JsonProperty("CUSTOM_NAME")
    private String customName;

    @JsonProperty("ITEMS")
    private List<ErpRequisitionItemDto> items;

    /**
     * 字段校验
     *
     * @throws IllegalStateException 当必填字段缺失或数据不合法时
     */
    public void fieldCheck() {
        // 校验顶层必填字段
        validateNotBlank(businessFormNo, "BUSINESS_FORM_NO");
        validateNotBlank(businessFormType, "BUSINESS_FORM_TYPE");

        // 校验明细列表
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("ITEMS 列表不能为空");
        }

        Set<String> uniqueItemNos = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            ErpRequisitionItemDto item = items.get(i);
            if (item == null) {
                throw new IllegalStateException("ITEMS[" + i + "] 不能为 null");
            }

            // 建议：复用 item 自身的校验方法（需确保 ErpRequisitionItemDto 有 isValid 或 validate）
            if (!item.isValid()) {
                throw new IllegalStateException("ITEMS[" + i + "] 字段校验失败，请检查必填项");
            }

            String itemNo = item.getItemNo();
            if (isBlank(itemNo)) {
                throw new IllegalStateException("ITEMS[" + i + "].ITEM_NO 不能为空");
            }

            if (!uniqueItemNos.add(itemNo)) {
                throw new IllegalStateException("ITEMS 中 ITEM_NO 存在重复项: " + itemNo);
            }
        }
    }

    // ---------------- 工具方法 ----------------

    private void validateNotBlank(String value, String fieldName) {
        if (isBlank(value)) {
            throw new IllegalStateException(fieldName + " 不能为空");
        }
    }
}