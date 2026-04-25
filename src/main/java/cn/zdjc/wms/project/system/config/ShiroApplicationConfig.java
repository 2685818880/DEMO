//package cn.zdjc.wms.project.system.config;
//
//import com.foeris.y.deprecate.shiro.YsEnterpriseCacheSessionDAO;
//import com.foeris.y.frame.filter.shiro.UserShiroFilter;
//import com.foreris.eris.frame.web.WebResourceConfig;
//import org.apache.shiro.codec.Base64;
//import org.apache.shiro.mgt.SecurityManager;
//import org.apache.shiro.session.mgt.SessionManager;
//import org.apache.shiro.session.mgt.eis.SessionDAO;
//import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
//import org.apache.shiro.util.ThreadContext;
//import org.apache.shiro.web.mgt.CookieRememberMeManager;
//import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
//import org.apache.shiro.web.servlet.SimpleCookie;
//import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Lazy;
//
//import javax.servlet.Filter;
//import java.util.*;
//
///**
// * @author caoxianlei
// */
//@Configuration
//public class ShiroApplicationConfig {
//    public ShiroApplicationConfig() {
//    }
//
//    public UserShiroFilter userShiroFilter() {
//        return new UserShiroFilter();
//    }
//
//
//    @Bean
//    public ShiroFilterFactoryBean shiroFilterFactoryBean(@Qualifier("securityManager") SecurityManager securityManager) {
//        ShiroFilterFactoryBean shiroBean = new ShiroFilterFactoryBean();
//        shiroBean.setLoginUrl("/login");
//        shiroBean.setSecurityManager(securityManager);
//        Map<String, Filter> filters = shiroBean.getFilters();
//        filters.put("user", this.userShiroFilter());
//        Map<String, String> filterMap = new LinkedHashMap<>();
//        shiroBean.setFilters(filters);
//
//        List<String> excludeResource = WebResourceConfig.staticResource;
//        Iterator var6 = excludeResource.iterator();
//
//        String exclude;
//        while (var6.hasNext()) {
//            exclude = (String) var6.next();
//            filterMap.put(exclude, "anon");
//        }
//
//        excludeResource = WebResourceConfig.noneUser;
//        var6 = excludeResource.iterator();
//
//        while (var6.hasNext()) {
//            exclude = (String) var6.next();
//            filterMap.put(exclude, "anon");
//        }
//        filterMap.put("/doc.html/**", "anon");
//        filterMap.put("/thymeleaf/station-view/**", "anon");
//        filterMap.put("/Swagger/**", "anon");
//        filterMap.put("/swagger-resources/**", "anon");
//        filterMap.put("/**", "user");
//        shiroBean.setFilterChainDefinitionMap(filterMap);
//        return shiroBean;
//    }
//
//    @Bean(
//            name = {"securityManager"}
//    )
//    public DefaultWebSecurityManager securityManager(@Lazy @Qualifier("StationRealm") StationRealm applicationRealm, @Lazy @Qualifier("rememberMeManager") CookieRememberMeManager rememberMeManager, @Lazy @Qualifier("sessionManager") SessionManager sessionManager) {
//        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
//        securityManager.setRealm(applicationRealm);
//        securityManager.setRememberMeManager(rememberMeManager);
//        securityManager.setSessionManager(sessionManager);
//        ThreadContext.bind(securityManager);
//        return securityManager;
//    }
//
//    @Bean(
//            name = {"StationRealm"}
//    )
//    public StationRealm applicationRealm() {
//        return new StationRealm();
//    }
//
//    @Bean({"rememberMeManager"})
//    public CookieRememberMeManager rememberMeManager() {
//        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
//        cookieRememberMeManager.setCookie(this.rememberMeCookie());
//        cookieRememberMeManager.setCipherKey(Base64.decode("4AvVhmFLUs0KTA3Kprsdag=="));
//        return cookieRememberMeManager;
//    }
//
//    @Bean
//    public SimpleCookie rememberMeCookie() {
//        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
//        simpleCookie.setHttpOnly(true);
//        simpleCookie.setMaxAge(-1);
//        simpleCookie.setPath("/");
//        return simpleCookie;
//    }
//
//    @Bean({"sessionManager"})
//    public DefaultWebSessionManager sessionManager() {
//        DefaultWebSessionManager manager = new DefaultWebSessionManager();
//        manager.setSessionIdUrlRewritingEnabled(false);
//        manager.setGlobalSessionTimeout(-1);
//        manager.setDeleteInvalidSessions(true);
//        manager.setSessionValidationSchedulerEnabled(true);
//        manager.setSessionDAO(this.sessionDAO());
//        manager.setSessionIdCookieEnabled(true);
//        manager.setSessionIdCookie(this.sessionIdCookie());
//        return manager;
//    }
//
//    @Bean
//    public SessionDAO sessionDAO() {
//        return new YsEnterpriseCacheSessionDAO();
//    }
//
//    @Bean
//    public SimpleCookie sessionIdCookie() {
//        SimpleCookie simpleCookie = new SimpleCookie("sid-wms");
//        simpleCookie.setHttpOnly(true);
//        simpleCookie.setPath("/");
//        simpleCookie.setMaxAge(-1);
//        return simpleCookie;
//    }
//}
