package cn.zdjc.wms.project.paas.excel.style;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * @author caoxianlei
 */
public class FreezeAndFilter implements SheetWriteHandler {

    public int colSplit = 0, rowSplit = 1, leftmostColumn = 0, topRow = 1;
//    public String autoFilterRange = "1:1";

    @Override
    public void beforeSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {

    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Sheet sheet = writeSheetHolder.getSheet();
        sheet.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow);
//        sheet.setAutoFilter(CellRangeAddress.valueOf(autoFilterRange));
    }
}
