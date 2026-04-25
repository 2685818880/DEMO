//package cn.zdjc.wms.project.paas.controller;
//
//import cn.zdjc.platform.system.manage.frame.LoginController;
//import cn.zdjc.platform.system.repository.user.menu.MenuDto;
//import cn.zdjc.platform.system.repository.user.menu.MenuService;
//import cn.zdjc.wms.pda.application.entity.Parameter;
//import com.baomidou.mybatisplus.core.toolkit.StringUtils;
//import com.foeris.y.common.result.MessageResult;
//import com.foeris.y.common.result.ObjectResult;
//import com.foeris.y.common.result.ResultFactory;
//import com.foeris.y.frame.user.User;
//import com.foeris.y.frame.user.UserContext;
//import com.foeris.y.frame.user.UserUtl;
//import org.apache.commons.collections.CollectionUtils;
//import org.apache.shiro.SecurityUtils;
//import org.apache.shiro.subject.Subject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.*;
//import java.util.stream.Collectors;
//
//
///**
// * @author caoxianlei
// */
//@Controller
//@RequestMapping("restful/wms/paas/user/")
//public class UserController {
//
//    @Resource
//    private LoginController loginController;
//    @Autowired
//    private MenuService menuService;
//
//    /**
//     * 当前用户校验是否登录接口
//     *
//     * @return
//     */
//    @PostMapping("check")
//    @ResponseBody
//    public ObjectResult<Map<String,Object>> check() {
//        if (UserUtl.isLogin()) {
//            Map<String, Object> map = new LinkedHashMap<>(16);
//            User user = UserContext.getUser();
//            map.put("id",user.getId());
//            map.put("account", user.getAccount());
//            map.put("name", user.getName());
//            map.put("is_locked", user.getIs_locked());
//            map.put("roles", user.getRoles());
//            return ResultFactory.getObject(map);
//        } else {
//            return ResultFactory.getErrorObject("用户登陆已失效,请重新登陆！");
//        }
//    }
//
//    /**
//     * 用户登出
//     * @param request
//     * @return
//     */
//    @PostMapping("logout")
//    @ResponseBody
//    public MessageResult logout(HttpServletRequest request) {
//        if (UserUtl.isLogin()) {
//            Subject subject = SecurityUtils.getSubject();
//            System.out.println(subject.getPrincipal());
//            request.setAttribute("userName", subject.getPrincipal());
//            request.setAttribute("toUrl", request.getContextPath() + "/");
//            subject.logout();
//        }
//        return ResultFactory.getSuccess();
//    }
//
//    /**
//     * 用户登录
//     * @param parameter
//     * @param request
//     * @param response
//     * @param model
//     * @return 返回转换后的权限（需在description字段维护映射）
//     */
//    @RequestMapping("login")
//    @ResponseBody
//    public ObjectResult<Map<String,Object>> login(@RequestBody Parameter parameter, HttpServletRequest request, HttpServletResponse response, Model model) {
//        try {
//            loginController.login(request, response, model, "", parameter.getUser(), parameter.getPwd(), "");
//            Object error = model.getAttribute("error");
//            if (!Objects.isNull(error)) {
//                return ResultFactory.getErrorObject(error.toString());
//            }
//        } catch (Exception e) {
//            return ResultFactory.getErrorObject(e.getMessage());
//        }
//        Map<String, Object> map = new LinkedHashMap<>(16);
//        User user = UserContext.getUser();
//        map.put("id",user.getId());
//        map.put("account", user.getAccount());
//        map.put("name", user.getName());
//        map.put("is_locked", user.getIs_locked());
//        List<String> rolesId = user.getRolesId();
//        map.put("rolesId", rolesId);
//        //处理权限数据
//        Set<String> perSet = new HashSet<>();
//        if (CollectionUtils.isNotEmpty(rolesId)) {
//            for (String roleId : rolesId) {
//                List<MenuDto> menuDtoList = menuService.queryAllListTreeCheckedByRoleId(UUID.fromString(roleId));
//                if (CollectionUtils.isNotEmpty(menuDtoList)) {
//                    for (MenuDto dto : menuDtoList) {
//                        if(StringUtils.isNotBlank(dto.getDescription())){
//                            perSet.add(dto.getDescription());
//                        }
//                    }
//                }
//            }
//            map.put("permissions", String.join(",", perSet));
//        }
//        return ResultFactory.getObject(map);
//    }
//}
