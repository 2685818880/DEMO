package cn.zdjc.wms.project.erp.dto.asn;

import cn.zdjc.wms.customize.domain.dto.ExtraDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * ERP 收货通知单 DTO（ASN）
 *
 * @author lele
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErpAsnDto extends ExtraDto {

    private UUID id;

    @JsonProperty("HOUSE_CODE")
    private String houseCode;

    @JsonProperty("FORM_ID")
    private String formId;

    @JsonProperty("BUS_NO")
    private String busNo;

    @JsonProperty("BUS_TYPE")
    private String busType;

    @JsonProperty("PRODUCER_CODE")
    private String producerCode;

    @JsonProperty("PRODUCER_NAME")
    private String producerName;

    @JsonProperty("SUPPLIER_CODE")
    private String supplierCode;

    @JsonProperty("SUPPLIER_NAME")
    private String supplierName;

    @JsonProperty("USER_DEPATMENT")
    private String userDepatment;

    @JsonProperty("CREATE_TIME")
    private String createTime;

    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("MAP")
    private Map<String, String> map;

    @JsonProperty("ITEMS")
    private List<ErpAsnItemDto> items;

    /**
     * 字段校验
     *
     * @throws IllegalStateException 当必填字段缺失或数据不合法时
     */
    public void fieldCheck() {
        // 校验顶层字段
        validateNotBlank(busNo, "BUS_NO 不能为空");
        validateNotBlank(busType, "BUS_TYPE 不能为空");

        // 校验 items
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("ITEMS 列表不能为空");
        }

        Set<String> uniqueItemNos = new HashSet<>();
        for (int i = 0; i < items.size(); i++) {
            ErpAsnItemDto item = items.get(i);
            if (item == null) {
                throw new IllegalStateException("ITEMS[" + i + "] 不能为 null");
            }

            String itemNo = item.getItemNo();
            validateNotBlank(itemNo, "ITEMS[" + i + "].ITEM_NO 不能为空");

            if (!uniqueItemNos.add(itemNo)) {
                throw new IllegalStateException("ITEMS 中 ITEM_NO 存在重复项: " + itemNo);
            }

            validateNotBlank(item.getSkuCode(), "ITEMS[" + i + "].SKU_CODE 不能为空");
        }
    }

    // ---------------- 工具方法 ----------------

    private void validateNotBlank(String value, String fieldName) {
        if (isBlank(value)) {
            throw new IllegalStateException(fieldName + " 不能为空");
        }
    }

    // ---------------- 可选：提供空安全的 items 访问 ----------------

    public List<ErpAsnItemDto> getItemsSafe() {
        return items != null ? items : Collections.emptyList();
    }

    public boolean hasItems() {
        return !getItemsSafe().isEmpty();
    }
}