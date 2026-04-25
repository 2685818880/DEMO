package cn.zdjc.wms.project.ai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class AiResponses {

    @Data
    public static class PredictionResult {
        private String warehouseId;
        private String skuCode;
        private List<DailyPrediction> predictions;
        private Double confidence;
        private List<String> suggestions;
    }

    @Data
    public static class DailyPrediction {
        private String date;
        private Double value;
        private Double lowerBound;
        private Double upperBound;
    }

    @Data
    public static class PathResult {
        private List<String> path;
        private Double totalDistance;
        private Integer totalTime;
        private List<String> improvements;
    }

    @Data
    public static class AnomalyResult {
        private List<AnomalyItem> anomalies;
        private Integer totalCount;
    }

    @Data
    public static class AnomalyItem {
        private String id;
        private String label;
        private String type;
        private String severity;
        private String description;
        private Double score;
        private String timestamp;
    }

    @Data
    public static class AgentStatus {
        private String status;
        private List<AgentInfo> agents;
    }

    @Data
    public static class AgentInfo {
        private String name;
        private String type;
        private boolean running;
    }

    @Data
    public static class HealthStatus {
        private String status;
        private String service;
    }

    // ==================== ChatBI DTOs ====================

    @Data
    public static class ChatBIResponse {
        private String query;
        private String summary;
        private List<SlowMovingItem> items;
        private List<InventorySummaryItem> summaryItems;
        private List<SkuItem> skuItems;
        private List<SkuCategoryItem> skuCategoryItems;
        private List<StorageLocationItem> locationItems;
        private List<ContainerItem> containerItems;
        private List<DispatchInfoItem> dispatchInfoItems;
        private List<DispatchJobItem> dispatchJobItems;
        private List<SchedulerManageItem> schedulerItems;
        private List<ActionButton> actionButtons;
        private List<String> suggestions;
        private ChatMeta meta;
    }

    @Data
    public static class SkuItem {
        private String skuCode;
        private String skuName;
        private String categoryCode;
        private String categoryName;
        private String barcode;
        private String packageType;
        private String durationOfValidity;
        private String validityDateUnit;
        private boolean skuBatchFlag;
        private boolean skuSerialFlag;
        private boolean blockState;
        private boolean isActive;
    }

    @Data
    public static class SkuCategoryItem {
        private String categoryCode;
        private String categoryName;
        private String categoryDesc;
        private String parentCategoryName;
        private int treeLevel;
        private int childCount;
        private boolean isActive;
    }

    @Data
    public static class StorageLocationItem {
        private String locNo;
        private String houseCode;
        private String xPos;
        private String yPos;
        private String zPos;
        private String locType;
        private String storageStatus;
        private String locUseStatus;
        private boolean forbidIn;
        private boolean forbidOut;
        private boolean locIsError;
        private String locErrorReason;
        private boolean isActive;
    }

    @Data
    public static class ContainerItem {
        private String containerCode;
        private String containerTypeCode;
        private String containerTypeName;
        private String containerLength;
        private String containerWidth;
        private String containerHeight;
        private String containerWeight;
        private int usageCount;
    }

    @Data
    public static class DispatchInfoItem {
        private String dispatchCode;
        private String dispatchName;
        private String dispatchType;
        private String dispatchStatus;
        private String houseCode;
        private String containerCode;
        private String businessFormNo;
        private String businessFormType;
        private String createDatetime;
    }

    @Data
    public static class DispatchJobItem {
        private String taskNo;
        private String taskType;
        private String taskStatus;
        private String houseCode;
        private String containerCode;
        private String fromPos;
        private String toPos;
        private String dispatchId;
        private String sendTime;
        private String inTime;
        private String errorCode;
        private String errorDesc;
        private String businessFormNo;
        private String businessFormType;
        private int taskLevel;
        private String groupCode;
    }

    @Data
    public static class SchedulerManageItem {
        private String jobCode;
        private String triggerCode;
        private String jobType;
        private String triggerCorn;
        private String nextTriggerDatetime;
        private boolean isPause;
        private boolean isFinished;
        private boolean isDisable;
        private String description;
        private int startTimeSeconds;
    }

    @Data
    public static class InventorySummaryItem {
        private String skuCode;
        private String skuName;
        private String primaryUnit;
        private BigDecimal totalAvailableQty;
        private BigDecimal totalPrimaryQty;
        private int batchCount;
        private int locationCount;
    }

    @Data
    public static class SlowMovingItem {
        private String skuCode;
        private String skuName;
        private String batchNo;
        private String locationCode;
        private String containerCode;
        private BigDecimal primaryQty;
        private BigDecimal availableQty;
        private String primaryUnit;
        private String zoneName;
        private int dwellDays;
        private String qualityStatus;
        private String suggestion;
    }

    @Data
    public static class ActionButton {
        private String label;
        private String action;
        private String icon;
        private String type;
    }

    @Data
    public static class ChatMeta {
        private String parsedZone;
        private String parsedType;
        private String parsedAction;
        private int totalItems;
        private long queryTimeMs;
    }
}
