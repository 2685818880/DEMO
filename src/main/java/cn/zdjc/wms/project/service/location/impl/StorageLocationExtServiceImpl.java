package cn.zdjc.wms.project.service.location.impl;

import cn.zdjc.wms.project.domain.entity.location.StorageLocationExtEntity;
import cn.zdjc.wms.project.mapper.location.StorageLocationExtMapper;
import cn.zdjc.wms.project.repository.location.StorageLocationExtRepository;
import cn.zdjc.wms.project.service.location.StorageLocationExtService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class StorageLocationExtServiceImpl
        extends ServiceImpl<StorageLocationExtMapper, StorageLocationExtEntity>
        implements StorageLocationExtService {

    @Resource
    private StorageLocationExtRepository repository;
}