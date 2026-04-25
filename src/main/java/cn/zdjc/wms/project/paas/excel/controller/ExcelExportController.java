package cn.zdjc.wms.project.paas.excel.controller;

import cn.zdjc.wms.project.paas.excel.data.DemoData;
import cn.zdjc.wms.project.paas.excel.util.ExportExcelUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author caoxianlei
 */
@Controller
@RequestMapping("wms/paas/excel/export")
public class ExcelExportController {

    @PostMapping("/takeStockItem/download")
    @CrossOrigin
    public void takeStockItem(HttpServletResponse response) throws IOException {
        List<DemoData> dataList = new LinkedList<>();
        //处理数据塞入DemoData
        ExportExcelUtil.exportExcel(response, dataList, "演示DEMO" + System.currentTimeMillis(), DemoData.class);
    }
}
