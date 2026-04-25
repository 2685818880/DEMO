package cn.zdjc.wms.project.service.material.impl;

import cn.zdjc.wms.project.domain.entity.material.StorageMaterialExtEntity;
import cn.zdjc.wms.project.mapper.material.StorageMaterialExtMapper;
import cn.zdjc.wms.project.repository.material.StorageMaterialExtRepository;
import cn.zdjc.wms.project.service.material.StorageMaterialExtService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class StorageMaterialExtServiceImpl
        extends ServiceImpl<StorageMaterialExtMapper, StorageMaterialExtEntity>
        implements StorageMaterialExtService {

    @Resource
    private StorageMaterialExtRepository repository;
}