//package cn.zdjc.wms.project.common.utils;
//
//import cn.zdjc.wms.base.logs.elasticsearch.dto.ElasticsearchRollingSearchBaseDTO;
//import cn.zdjc.wms.base.logs.service.LogElasticsearchService;
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.common.xcontent.XContentBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//public class ElasticsearchAsyncUtil {
//    @Autowired
//    private LogElasticsearchService logElasticsearchService;
//
//    @Async("esLogExecutor")
//    public void esInsertTime(ElasticsearchRollingSearchBaseDTO dto, String indexName, XContentBuilder xContentBuilder) {
//        long esStartTime = System.currentTimeMillis();
//        logElasticsearchService.indexRolling(indexName, dto, xContentBuilder);
//        long esEndTime = System.currentTimeMillis();
//        long executionTime = esEndTime - esStartTime;
//        log.info("ES插入时间：" + (esEndTime - esStartTime) + "毫秒");
//        // 判断是否大于 3 秒
//        if (executionTime > 3000) {
//            log.info("日志记录程序执行时间大于 3 秒: " + executionTime + " 毫秒");
//        }
//    }
//}
