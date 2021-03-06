package cn.com;

import cn.com.utils.AuthFilterItemProperties;
import cn.com.utils.ex.AppConfigException;
import lombok.NonNull;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@Log4j
@Order(1)
public class SpringContextUtil<T> implements ApplicationContextAware, Serializable {
    public static final Long serialVersionUID = 24L;

    public static AuthFilterItemProperties authFilterItemProperties;

    private static ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext args0) throws BeansException {
        if (null == applicationContext)applicationContext = args0;
    }

    public static <T> T getBean(@NonNull String name){
        return (T)applicationContext.getBean(name);
    }

    //通过类型获取上下文中的bean
    public static <T> T getBean(@NonNull Class<T> requiredType){
        return (T)applicationContext.getBean(requiredType);
    }

    public static HttpServletRequest getRequest(){
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    public static HttpServletResponse getResponse(){
        HttpServletResponse response = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        if (null != response){
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=utf-8");
        }
        return response;
    }

    public static boolean isAjax(HttpServletRequest req) {
        String contentTypeHeader = req.getHeader("content-type");
        String acceptHeader = req.getHeader("accept");
        String xRequestedWith = req.getHeader("x-requested-wit");
        return (contentTypeHeader != null && contentTypeHeader.contains("application/json"))
                || (acceptHeader != null && acceptHeader.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(xRequestedWith);
    }

    public static boolean isAjax() {
        HttpServletRequest req = getRequest();
        String contentTypeHeader = req.getHeader("content-type");
        String acceptHeader = req.getHeader("accept");
        String xRequestedWith = req.getHeader("x-requested-wit");
        return (contentTypeHeader != null && contentTypeHeader.contains("application/json"))
                || (acceptHeader != null && acceptHeader.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(xRequestedWith);
    }

    public static void write(String success) {
        write(success,200);
    }

    public static void write(HttpServletResponse response,String success) {
        write(response, success,200);
    }

    public static void write(String success, int statusCode) {
        HttpServletResponse response = getResponse();
        write(response, success, statusCode);
    }

    public synchronized static void write(HttpServletResponse response,String success, int statusCode) {
        if (response == null)throw new NullPointerException("write response is null");
        response.setStatus(statusCode);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        ServletOutputStream os = null;
        try {
            os = response.getOutputStream();
            os.write(success.getBytes());
        } catch (IOException e) {
            try {
                throw new IOException("SpringContextUtil 写回数据出现异常");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }finally {
            if (null != os){
                try {
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * <p> 返回是否创建 TOKEN
     * @return boolean
     * @author Haidar
     * @date 2020/1/10 15:16
     **/
    public static boolean isSeparation(HttpServletResponse response) throws AppConfigException {
        if (authFilterItemProperties == null){
            authFilterItemProperties = SpringContextUtil.getBean(AuthFilterItemProperties.class);
        }
        int separation = authFilterItemProperties.getIsSeparation();
        String bo = response.getHeader("separation");
        if (!StringUtils.isBlank(bo)){
            return Boolean.parseBoolean(bo);
        }
        if(separation == 1){
            response.addHeader("separation","true");
            return Boolean.TRUE;
        }else if (separation == 2){
            response.addHeader("separation","false");
            return Boolean.FALSE;
        }
        throw new AppConfigException("请配置 isSeparation 属性");
    }
    public static int getStatus(HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (status != null) {
            return status;
        }
        return 500;
    }
    public static int getStatus(HttpServletResponse response) {
        return response.getStatus();
    }

    public static void clearCookie(){
        HttpServletRequest request = getRequest();
        HttpServletResponse response = getResponse();
        Cookie[] cookies = request.getCookies();
        if (cookies!=null){
            for (Cookie c: cookies) {
                Cookie cookie = new Cookie(c.getName(),null);
                cookie.setSecure(false);
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }public static void clearCookie(HttpServletRequest request,HttpServletResponse response){
        Cookie[] cookies = request.getCookies();
        log.info("cookies : " + cookies);
        if (cookies!=null){
            for (Cookie c: cookies) {
                Cookie cookie = new Cookie(c.getName(),null);
                cookie.setSecure(false);
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
        request.setAttribute("Cookie","");
    }

    public static Map<String, Object> object2Map(Object obj) {
        Map<String, Object> map = new HashMap<>();
        if (obj == null) {
            return map;
        }
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                map.put(field.getName(), field.get(obj));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void ModeLog(HttpServletRequest request){
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String addr = request.getRemoteAddr();
        String token = request.getHeader("TOKEN");
        String s = "当前请求 [ %s ] - 请求类型 [ %s ] - 请求 IP [ %s ] - 请求 TOKEN [ %s ]";
        log.info(String.format(s, uri,method,addr,token));
    }

    public static AuthFilterItemProperties getAuthFilterItemProperties() {
        return authFilterItemProperties;
    }

    public static void setAuthFilterItemProperties(AuthFilterItemProperties authFilterItemProperties) {
        SpringContextUtil.authFilterItemProperties = authFilterItemProperties;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
