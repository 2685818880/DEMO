//package cn.zdjc.wms.project.service.ws.impl;
//
//import cn.hutool.core.collection.CollUtil;
//import cn.hutool.core.exceptions.ExceptionUtil;
//
//import cn.zdjc.wms.base.logs.elasticsearch.dto.ElasticsearchResult;
//import cn.zdjc.wms.base.logs.service.LogElasticsearchService;
//import cn.zdjc.wms.base.logs.utils.LogElasticsearchUtils;
//import cn.zdjc.wms.project.common.constant.ElasticsearchConstants;
//import cn.zdjc.wms.project.common.param.ListDataParam;
//import cn.zdjc.wms.project.common.utils.ElasticsearchAsyncUtil;
//import cn.zdjc.wms.project.domain.dto.ws.WorkstationHistoryDTO;
//import cn.zdjc.wms.project.domain.query.ws.WorkstationHistoryQuery;
//import cn.zdjc.wms.project.domain.query.ws.WorkstationHistoryResp;
//import cn.zdjc.wms.project.service.ws.WorkstationHistoryService;
//import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.List;
//
//
///**
// * @author
// * @date 6/14/22 10:23 AM
// */
//@Service
//@Slf4j
//public class WorkstationHistoryServiceImpl implements WorkstationHistoryService {
//
//
//    @Autowired
//    private LogElasticsearchService logElasticsearchService;
//    @Resource
//    private ElasticsearchAsyncUtil elasticsearchAsyncUtil;
//
//    @Override
//    public ElasticsearchResult<WorkstationHistoryResp> searchRollingPage(WorkstationHistoryQuery query) {
//
//        List<String> index = LogElasticsearchUtils.getYearIndexListByTimeRange(ElasticsearchConstants.WORKSTATION_HISTORY_INDEX_NAME_PREFIX, query.getStartTime(), query.getEndTime());
//        ElasticsearchResult<WorkstationHistoryDTO> result = logElasticsearchService.searchRolling(index, query, WorkstationHistoryDTO.class);
//        if (CollUtil.isNotEmpty(result.getData())) {
//            List<WorkstationHistoryResp> workstationHistoryResps = new ArrayList<>();
//            for (WorkstationHistoryDTO workstationHistoryDTO : result.getData()) {
//                workstationHistoryResps.add(workstationHistoryDTO);
//            }
//            ElasticsearchResult<WorkstationHistoryResp> respElasticsearchResult = new ElasticsearchResult<>(result.getTotalRecords(), workstationHistoryResps, result.getPageUid());
//            return respElasticsearchResult;
//        }
//
//        return new ElasticsearchResult<>();
//    }
//
//    @Override
//    public void createWorkstationHistory(ListDataParam<WorkstationHistoryDTO> param) {
//        try {
//            if (CollectionUtils.isEmpty(param.getData())) {
//                return;
//            }
//            for (WorkstationHistoryDTO d : param.getData()) {
//                d.fieldCheck();
//            }
//            for (WorkstationHistoryDTO d : param.getData()) {
//                String index = LogElasticsearchUtils.getYearIndex(ElasticsearchConstants.WORKSTATION_HISTORY_INDEX_NAME_PREFIX, d.getStartTime());
//                elasticsearchAsyncUtil.esInsertTime(d, index, d.getMapping());
//            }
//        } catch (Exception e) {
//            log.info(ExceptionUtil.stacktraceToString(e));
//        }
//    }
//
//}
