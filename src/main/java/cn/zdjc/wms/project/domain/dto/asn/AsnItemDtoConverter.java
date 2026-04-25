package cn.zdjc.wms.project.domain.dto.asn;

import cn.zdjc.wms.definition.domain.dto.AsnItemDto;
import cn.zdjc.wms.project.domain.dto.asn.AsnItemExtDto;
import com.foeris.y.common.bean.BeanUtl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AsnItemDto 与 AsnItemExtDto 转换器
 * 
 * @author your-name
 * @date 2026-02-03
 */
@Slf4j
public class AsnItemDtoConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 批量转换：AsnItemDto 列表 → AsnItemExtDto 列表
     */
    public static List<AsnItemExtDto> toExtDtoList(List<AsnItemDto> dtoList) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return new ArrayList<>();
        }
        
        return dtoList.stream()
                .map(AsnItemDtoConverter::toExtDto)
                .collect(Collectors.toList());
    }

    /**
     * 单个对象转换：AsnItemDto → AsnItemExtDto
     */
    public static AsnItemExtDto toExtDto(AsnItemDto dto) {
        if (dto == null) {
            return null;
        }
        
        // 1. 使用 BeanUtl 智能转换（自动处理 @BeanAlias 字段映射）
        AsnItemExtDto extDto = BeanUtl.copyProperties(dto, AsnItemExtDto.class);
        
        // 2. 处理额外字段（extra）：合并原始 extraMap + @ExtraAnnotation 字段
        Map<String, Object> extraMap = dto.getExtraMap() != null 
                ? new java.util.HashMap<>(dto.getExtraMap()) 
                : new java.util.HashMap<>();
        
        // 添加带有 @ExtraAnnotation 的字段
        extraMap.putAll(BeanUtl.getPropertiesWithAnnotation(dto, cn.zdjc.warehouse.ExtraAnnotation.class));
        
        // 转换为 JSON 字符串
        try {
            if (!extraMap.isEmpty()) {
                extDto.setExtra(OBJECT_MAPPER.writeValueAsString(extraMap));
            }
        } catch (JsonProcessingException e) {
            log.warn("转换 extra 字段失败，明细行号: {}", dto.getItem_no(), e);
            extDto.setExtra("{}");
        }
        
        // 3. 设置 modified 标志（默认 false）
//        extDto.setModified(false);
        
        return extDto;
    }

    /**
     * 反向转换：AsnItemExtDto → AsnItemDto（如需要）
     */
    public static AsnItemDto fromExtDto(AsnItemExtDto extDto) {
        if (extDto == null) {
            return null;
        }
        
        AsnItemDto dto = BeanUtl.copyProperties(extDto, AsnItemDto.class);
        
        // 处理 extra 字段反向转换
        if (extDto.getExtra() != null && !extDto.getExtra().trim().isEmpty()) {
            try {
                Map<String, Object> extraMap = OBJECT_MAPPER.readValue(
                    extDto.getExtra(), 
                    OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                );
                dto.setExtra(extraMap);
            } catch (JsonProcessingException e) {
                log.warn("解析 extra 字段失败，明细行号: {}", extDto.getItemNo(), e);
            }
        }
        
        return dto;
    }
}