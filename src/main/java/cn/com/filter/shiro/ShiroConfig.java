package cn.com.filter.shiro;

import cn.com.entity.Permission;
import cn.com.filter.shiro.base.MyDefaultWebSubjectFactory;
import cn.com.filter.shiro.base.ShiroRealm;
import cn.com.filter.shiro.filter.MyCredentialsMatcher;
import cn.com.filter.shiro.filter.MyFormAuthenticationFilter;
import cn.com.filter.shiro.base.MyShiroFilterFactoryBean;
import cn.com.filter.shiro.filter.ShiroFilter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@Log4j
@Order(3)
public class ShiroConfig {
    /**
     * <p> 凭证匹配器
     * @return HashedCredentialsMatcher
     * @author Haidar
     * @date 2020/1/9 12:08
     **/
    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher(){
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName("md5");//散列算法:这里使用MD5算法;
        hashedCredentialsMatcher.setHashIterations(2);//散列的次数，比如散列两次，相当于 md5(md5(""));
        return hashedCredentialsMatcher;
    }

    /**
     * <p> 权限认证
     * @return ShiroRealm
     * @author Haidar
     * @date 2020/1/9 12:09
     **/
    @Bean
    public ShiroRealm shiroRealm(){
        ShiroRealm myRealm = new ShiroRealm();
        String hashAlgorithmName = "md5";
        if (StringUtils.equals(hashAlgorithmName.toUpperCase(),"MD5")){
            log.info(">>>>>>>>>>>>>>>ShiroRealm 注入 MD5 加密<<<<<<<<<<<<<");
            myRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        }else {
            log.info(">>>>>>>>>>>>>>>ShiroRealm 注入自定义加密<<<<<<<<<<<<<");
            myRealm.setCredentialsMatcher(new MyCredentialsMatcher());
        }
        log.info(">>>>>>>>>>>>>>>ShiroRealm注册完成<<<<<<<<<<<<<");
        return myRealm;
    }

    /**
     * <p> 安全管理器
     * @return DefaultWebSecurityManager
     * @author Haidar
     * @date 2020/1/9 12:09
     **/
    @Bean
    public DefaultWebSecurityManager securityManager(){
        DefaultWebSecurityManager webSecurityManager = new DefaultWebSecurityManager();
        webSecurityManager.setRealm(shiroRealm());
        webSecurityManager.setSubjectFactory(new MyDefaultWebSubjectFactory());
        log.info(">>>>>>>>>>>>>>>DefaultWebSecurityManager注册完成<<<<<<<<<<<<<");
        return webSecurityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
        log.info(">>>>>>>>>>>>>>>shiroFilterFactoryBean<<<<<<<<<<<<<");
        MyShiroFilterFactoryBean shiroFilterFactoryBean = new MyShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager());
        //拦截器.
        Map<String,String> filterChainDefinitionMap = new LinkedHashMap<String,String>();
        // 配置不会被拦截的链接 顺序判断，因为前端模板采用了thymeleaf，这里不能直接使用 ("/static/**", "anon")来配置匿名访问，必须配置到每个静态目录
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/img/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/html/**", "anon");
        filterChainDefinitionMap.put("/hello/**", "anon");
        //配置退出 过滤器,其中的具体的退出代码Shiro已经替我们实现了
        filterChainDefinitionMap.put("/logout", "logout");
        //<!-- 过滤链定义，从上向下顺序执行，一般将/**放在最为下边 -->:这是一个坑呢，一不小心代码就不好使了;
        //<!-- authc:所有url都必须认证通过才可以访问; anon:所有url都都可以匿名访问-->
        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面
        shiroFilterFactoryBean.setLoginUrl("/login");
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/index");
        //未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl("/403");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);

        HashMap<String, Filter> filterHashMap = new HashMap<>();
        filterHashMap.put("authc",new MyFormAuthenticationFilter());
        filterHashMap.put("shiroFilter",new ShiroFilter());
        shiroFilterFactoryBean.setFilters(filterHashMap);
        return shiroFilterFactoryBean;
    }

}