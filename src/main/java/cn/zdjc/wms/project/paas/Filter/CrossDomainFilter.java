//package cn.zdjc.wms.project.paas.Filter;
//
//import cn.hutool.json.JSONArray;
//import cn.hutool.json.JSONObject;
//import cn.hutool.json.JSONUtil;
//import com.baomidou.mybatisplus.core.toolkit.StringUtils;
//import org.apache.commons.text.StringEscapeUtils;
//import org.springframework.core.annotation.Order;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.*;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletRequestWrapper;
//import java.io.IOException;
//import java.util.*;
//
//
///**
// * @author caoxianlei
// * @Order里边的数字越小代表越先被该Filter过滤
// * @WebFilter代表这是个Filter类并把这个类注入到容器中
// * 处理低代码iframe嵌套页面跨域登录校验问题
// */
//@Order(1)
//@WebFilter(filterName = "crossDomainFilter", urlPatterns = "/*")
//@Component
//public class CrossDomainFilter implements Filter {
//
//
//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
////        Filter.super.init(filterConfig);
//    }
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
//        // 修改cookie
//        ModifyHttpServletRequestWrapper mParametersWrapper = new ModifyHttpServletRequestWrapper((HttpServletRequest) request);
//        String authorization = mParametersWrapper.getHeader("Authorization");
//        if (StringUtils.isNotBlank(authorization)) {
//            JSONArray array = JSONUtil.parseArray(StringEscapeUtils.unescapeHtml3(authorization));
//            array.stream().filter(Objects::nonNull).forEach(json->{
//                mParametersWrapper.putCookie(((JSONObject) json).getStr("name"),((JSONObject) json).getStr("value"));
//            });
//            chain.doFilter(mParametersWrapper, response);
//        } else {
//            chain.doFilter(request, response);
//        }
//    }
//
//    @Override
//    public void destroy() {
////        Filter.super.destroy();
//    }
//
//    private static class ModifyHttpServletRequestWrapper extends HttpServletRequestWrapper {
//        private final Map<String, String> mapCookies;
//
//        ModifyHttpServletRequestWrapper(HttpServletRequest request) {
//            super(request);
//            this.mapCookies = new HashMap<>();
//        }
//
//        void putCookie(String name, String value) {
//            this.mapCookies.put(name, value);
//        }
//
//        @Override
//        public Cookie[] getCookies() {
//            HttpServletRequest request = (HttpServletRequest) getRequest();
//            Cookie[] cookies = request.getCookies();
//            if (mapCookies == null || mapCookies.isEmpty()) {
//                return cookies;
//            }
//            if (cookies == null || cookies.length == 0) {
//                List<Cookie> cookieList = new LinkedList<>();
//                for (Map.Entry<String, String> entry : mapCookies.entrySet()) {
//                    String key = entry.getKey();
//                    if (key != null && !key.isEmpty()) {
//                        cookieList.add(new Cookie(key, entry.getValue()));
//                    }
//                }
//                if (cookieList.isEmpty()) {
//                    return cookies;
//                }
//                return cookieList.toArray(new Cookie[cookieList.size()]);
//            } else {
//                List<Cookie> cookieList = new ArrayList<>(Arrays.asList(cookies));
//                for (Map.Entry<String, String> entry : mapCookies.entrySet()) {
//                    String key = entry.getKey();
//                    if (key != null && !key.isEmpty()) {
//                        for (int i = 0; i < cookieList.size(); i++) {
//                            if (cookieList.get(i).getName().equals(key)) {
//                                cookieList.remove(i);
//                            }
//                        }
//                        cookieList.add(new Cookie(key, entry.getValue()));
//                    }
//                }
//                return cookieList.toArray(new Cookie[cookieList.size()]);
//            }
//        }
//    }
//}
