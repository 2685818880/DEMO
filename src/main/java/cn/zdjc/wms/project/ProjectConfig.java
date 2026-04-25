package cn.zdjc.wms.project;

import cn.zdjc.platform.system.manage.dictionary.DictionaryItem;

import java.util.HashMap;
import java.util.Map;


@DictionaryItem(description = "项目配置")
public class ProjectConfig {

    @DictionaryItem(description = "服务多活模式")
    public static Boolean MULTI_INSTANCE_MODE  = Boolean.FALSE;

    @DictionaryItem(description = "接口并发请求等待超时时间(s)")
    public static Integer DISTRIBUTE_LOCK_WAIT_TIME = 30;

    @DictionaryItem(description = "模拟ERP")
    public static Boolean Fake_ERP = Boolean.FALSE;

    @DictionaryItem(description = "ERP_URL地址")
    public static String ERP_Url = "http://218.92.77.58:8800/k3cloud/";

    /**
     * 工作站和工作站拥有模式映射关系
     */
    @DictionaryItem(description = "工作站和工作站拥有模式映射关系<key:工作站编号,value:拥有的模式(模式1,模式2...)>")
    public static Map<String, String> WorkstationAndModeListMapping = new HashMap<>();


    /**
     * 工作站设备位置和工作站具体点位映射关系
     */
    @DictionaryItem(description = "工作站设备位置和工作站具体点位映射关系<key:工作站编号,value:工作站具体点位(pickLeftDeviceCode,pickRightDeviceCode,tallyLeftDeviceCode,tallyRightDeviceCode,takeStockLeftDeviceCode)>")
    public static Map<String, String> WorkstationCodeAndCodeMapping = new HashMap<>();

    static {
        // 添加工作站设备映射
        WorkstationCodeAndCodeMapping.put("P01", "3081_1,4057,3081,3081_1,3081_1");
        WorkstationCodeAndCodeMapping.put("P02", "3054_1,4033,3054,3054_1,3054_1");
    }

    @DictionaryItem(description = "申请点托盘,自定义移动(key:起始地点 val:目标地点)")
    public static Map<String, String> MoveDeviceTargetPos = new HashMap<>();

    @DictionaryItem(description = "申请点托盘,自定义区域-设备(key:区域 val:设备)")
    public static Map<String, String> SectionDeviceTargetPos = new HashMap<>();


}
