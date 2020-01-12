package cn.com.filter.shiro.filter;

import cn.com.SpringContextUtil;
import cn.com.entity.MyToken;
import cn.com.entity.Result;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Log4j
public class ShiroFilter extends AccessControlFilter {
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) {
        log.info("ShiroFilter onAccessDenied");
        Subject subject = getSubject(request, response);
//        if(!subject.isAuthenticated() && !subject.isRemembered()) {
//            log.info("onAccessDenied true");
//            return true;
//        }
        log.info(subject.isAuthenticated());
        boolean separation = SpringContextUtil.isSeparation((HttpServletRequest) request, (HttpServletResponse) response);
        if (separation){
            Object principal = subject.getPrincipal();
            log.info(principal);
            Session session = subject.getSession();
            log.info(session);
            return true;
        }else {
            String token = ((HttpServletRequest) request).getHeader("TOKEN");
            if (StringUtils.isBlank(token)){
                Result.failed((HttpServletResponse) response,200,"请重新登录");
            }
            MyToken t = (MyToken) JSONObject.parse(token);
            subject.login(t);
        }
        return false;
    }
}
