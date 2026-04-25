package cn.zdjc.wms.project.control.asn;

import cn.zdjc.wms.definition.infrastructure.enums.AsnStatus;
import cn.zdjc.wms.project.domain.dto.asn.AsnExtDto;
import cn.zdjc.wms.project.domain.dto.asn.AsnItemExtDto;
import cn.zdjc.wms.project.domain.query.asn.AsnExtExQuery;
import cn.zdjc.wms.project.domain.query.asn.AsnItemExtExQuery;
import cn.zdjc.wms.project.service.asn.AsnExtService;
import cn.zdjc.wms.project.service.asn.AsnItemExtService;
import com.foeris.y.common.result.PageResult;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/restful/api/asn")
@RequiredArgsConstructor
public class AsnExtController {

    private final AsnItemExtService asnItemExtService;

    private final AsnExtService asnExtService;

    @GetMapping("/page")
    public PageResult<AsnExtDto> findOrdersPage(
            @ApiParam("查询条件")
            @Validated AsnExtExQuery query) {

        return asnExtService.queryAsnPage(query);
    }


    @GetMapping("/item/page")
    public PageResult<AsnItemExtDto> findOrdersPage(
            @ApiParam("查询条件")
            @Validated AsnItemExtExQuery query) {
        List<String> statusList = Arrays.asList(String.valueOf(AsnStatus.Created),String.valueOf(AsnStatus.Executing));
        query.setAsnStatusList(statusList);
        return asnItemExtService.queryAsnItemAndAsnPagePageList(query);
    }

}