package cn.zdjc.wms.project.ai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class AiRequests {

    @Data
    public static class InventoryPrediction {
        private String warehouseId;
        private String skuCode;
        private String startDate;
        private String endDate;
        private String granularity;
    }

    @Data
    public static class PathOptimization {
        private String warehouseId;
        private String goal;
        private List<PickingTask> tasks;
        private WarehouseLayout layout;
    }

    @Data
    public static class PickingTask {
        private String fromLocation;
        private String toLocation;
        private String skuCode;
        private Integer quantity;
    }

    @Data
    public static class WarehouseLayout {
        private String name;
        private Integer width;
        private Integer height;
        private List<StorageLocation> locations;
    }

    @Data
    public static class StorageLocation {
        private String id;
        private String name;
        private Integer x;
        private Integer y;
        private String type;
    }

    @Data
    public static class AnomalyDetection {
        private String warehouseId;
        private String detectionType;
        private String startTime;
        private String endTime;
    }

    @Data
    public static class ChatBIQuery {
        private String query;
    }

    @Data
    public static class ChatBIItem {
        private String skuCode;
        private String skuName;
        private String batchNo;
        private String locationCode;
        private String containerCode;
        private BigDecimal availableQty;
        private String primaryUnit;
    }

    @Data
    public static class ChatBIActionRequest {
        private String houseCode;
        private String zoneName;
        private List<ChatBIItem> items;
    }
}
