package cn.zdjc.wms.project.service.outbound.impl;

import cn.zdjc.wms.project.domain.entity.outbound.OutboundExtEntity;
import cn.zdjc.wms.project.mapper.outbound.OutboundExtMapper;
import cn.zdjc.wms.project.repository.outbound.OutboundExtRepository;
import cn.zdjc.wms.project.service.outbound.OutboundExtService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class OutboundExtServiceImpl
        extends ServiceImpl<OutboundExtMapper, OutboundExtEntity>
        implements OutboundExtService {
    @Resource
    private OutboundExtRepository repository;

}