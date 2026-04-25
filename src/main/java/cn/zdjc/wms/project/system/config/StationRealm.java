//package cn.zdjc.wms.project.system.config;
//
//import com.foeris.y.common.reflect.ReflectUtl;
//import com.foeris.y.common.string.Md5Utl;
//import com.foeris.y.frame.user.User;
//import com.foeris.y.frame.user.UserContext;
//import com.foeris.y.frame.user.UserService;
//import org.apache.shiro.SecurityUtils;
//import org.apache.shiro.authc.*;
//import org.apache.shiro.authz.AuthorizationInfo;
//import org.apache.shiro.authz.SimpleAuthorizationInfo;
//import org.apache.shiro.realm.AuthorizingRealm;
//import org.apache.shiro.session.Session;
//import org.apache.shiro.subject.PrincipalCollection;
//import org.apache.shiro.subject.Subject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//
//public class StationRealm extends AuthorizingRealm {
//    private static final Logger log = LoggerFactory.getLogger(StationRealm.class);
//    public static final String authorizationKeyInSession = StationRealm.class.getName() + ".authorization";
//    @Autowired(
//            required = false
//    )
//    private UserService userService;
//
//    public StationRealm() {
//    }
//
//    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
//        log.trace("shiro 身份验证");
//        String account = (String) token.getPrincipal();
//        String password = new String((char[]) token.getCredentials());
//        if (account != null && password != null) {
//            User user = (User) ReflectUtl.invoke(UserContext.class, "getUser", new Class[]{String.class}, new Object[]{account});
//            if (user == null) {
//                user = this.userService.queryByAccount(account);
//            }
//
//            if (user != null && user.getAccount() != null) {
//                if (user.getIs_locked()) {
//                    UserContext.cleanCache();
//                    throw new AccountException("账号被锁");
//                } else {
//                    Subject subject = SecurityUtils.getSubject();
//                    Session session = subject.getSession();
//                    if (session.getAttribute("oauth-open-pwd") != null) {
//                        session.removeAttribute("oauth-open-pwd");
//                    } else if (!Md5Utl.digestHexStr(password).equals(user.getPassword()) && !password.equals(user.getPassword())) {
//                        UserContext.cleanCache();
//                        throw new AccountException("用户名或密码错误");
//                    }
//
//                    if (user != null && user.getAccount() != null && (user.getAccount().equals("admin")) || user.getRolesId() != null && user.getRolesId().size() != 0) {
//                        return new SimpleAuthenticationInfo(account, password, this.getName());
//                    } else {
//                        UserContext.cleanCache();
//                        throw new AccountException("账号无任何权限");
//                    }
//                }
//            } else {
//                UserContext.cleanCache();
//                throw new AccountException("用户名或密码错误");
//            }
//        } else {
//            throw new AccountException("请输入用户名或密码");
//        }
//    }
//
//    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
//        log.trace("授权操作");
//        Subject subject = SecurityUtils.getSubject();
//        Session session = subject.getSession(false);
//        SimpleAuthorizationInfo authorizationInfo = (SimpleAuthorizationInfo) session.getAttribute(authorizationKeyInSession);
//        if (authorizationInfo == null) {
//            log.trace("shiro 授权查询");
//            authorizationInfo = new SimpleAuthorizationInfo();
//            authorizationInfo.addRoles(UserContext.getUser().getRolesId());
//            authorizationInfo.addStringPermissions(UserContext.getUser().getPermissions());
//            session.setAttribute(authorizationKeyInSession, authorizationInfo);
//        }
//
//        return authorizationInfo;
//    }
//}
