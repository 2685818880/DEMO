//package cn.zdjc.wms.project.protocol;
//
//import cn.zdjc.wms.core.protocol.exceptions.ProtocolNotImplementException;
//import org.springframework.stereotype.Service;
//import com.foeris.y.common.result.MessageResult;
//import cn.zdjc.wms.core.protocol.wcs.AbstractWcsProtocol;
//
///**
// * 中鼎新WCS对接实现类，V3
// *
// * @author Leon Regulus at 2018/11/03
// * @version 1.0
// * @since 1.0
// */
//@Service
//public class ProjectWcsProtocolHttpImplV3 extends AbstractWcsProtocol {
//
//    @Override
//    public String getName() {
//        return "中鼎新WCS对接（HTTP）";
//    }
//
//    @Override
//    public String getType() {
//        return "WCS_HTTP_3";
//    }
//
//    @Override
//    public String getVersion() {
//        return "3";
//    }
//
//    @Override
//    public String getDescription() {
//        return "";
//    }
//
//    @Override
//    public MessageResult wmsFinishStack(String deviceCode, String containerCode, String houseNo) {
//        return null;
//    }
//
//    @Override
//    public MessageResult wmsRotate(String deviceCode, String containerCode, String houseNo) {
//        return null;
//    }
//}
