package cn.zdjc.wms.project.service.material.impl;

import cn.zdjc.wms.project.domain.entity.material.ContainerMaterialExtEntity;
import cn.zdjc.wms.project.mapper.material.ContainerMaterialExtMapper;
import cn.zdjc.wms.project.repository.material.ContainerMaterialExtRepository;
import cn.zdjc.wms.project.service.material.ContainerMaterialExtService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class ContainerMaterialExtServiceImpl
        extends ServiceImpl<ContainerMaterialExtMapper, ContainerMaterialExtEntity>
        implements ContainerMaterialExtService {

    @Resource
    private ContainerMaterialExtRepository repository;


}