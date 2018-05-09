package com.dongluhitec.iotweb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
public class CrosFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrosFilter.class);
    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.info("启动跨域支持");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse resp = (HttpServletResponse) servletResponse;
        HttpServletRequest rep = (HttpServletRequest) servletRequest;
        //允许进行跨域的主机ip
        resp.addHeader("Access-Control-Allow-Origin", rep.getHeader("Origin"));
        //允许跨域请求中携带cookie
        resp.addHeader("Access-Control-Allow-Credentials", "true");
        // 如果存在自定义的header参数，需要在此处添加，逗号分隔
        resp.addHeader("Access-Control-Allow-Headers", "authorization,Origin, No-Cache, X-Requested-With, "
                + "If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, " + "Content-Type, X-E4M-With");
        resp.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        LOGGER.info("销毁跨域支持");
    }

}
