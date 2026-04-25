package cn.zdjc.wms.project.paas.excel.util;

import cn.zdjc.wms.project.paas.excel.style.ExcelCellWidthStyleStrategy;
import cn.zdjc.wms.project.paas.excel.style.FreezeAndFilter;
import cn.zdjc.wms.project.paas.excel.style.StyleUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.excel.write.style.row.SimpleRowHeightStyleStrategy;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author caoxianlei
 */
public class ExportExcelUtil {
    public static void exportExcel(HttpServletResponse response, List<?> list,
                                   String fileName, Class baseEntity) throws IOException {
        //格式excel
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        //这里URLEncoder.encode可以防止中午乱码
        String file_name = URLEncoder.encode(fileName, "UTF-8");
        //Content-disposition:以下载方式执行此操作
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment;filename=" + file_name + ".xlsx");

        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), baseEntity)
                //设置输出excel，不设置默认为xlsx
                .excelType(ExcelTypeEnum.XLSX)
                //设置拦截器自定义样式
                //宽度自适应 自定义handler
                .registerWriteHandler(new ExcelCellWidthStyleStrategy())
                //设置标题行高和内容行高
                .registerWriteHandler(new SimpleRowHeightStyleStrategy((short) 40, (short) 30))
                //筛选和固定表头
                .registerWriteHandler(new FreezeAndFilter())
                .registerWriteHandler(new HorizontalCellStyleStrategy(StyleUtils.getHeadStyle(), StyleUtils.getContentStyle()))
                //设置默认样式及写入头信息开始的行数 这里0代表从第一行开始
                .useDefaultStyle(true).relativeHeadRowIndex(0).build();
        excelWriter.write(list, EasyExcel.writerSheet(fileName).build());
        excelWriter.finish();
    }
}
