package com.dongluhitec.iotweb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SessionFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFilter.class);
    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.info("启动会话支持");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest rep = (HttpServletRequest) servletRequest;
        LOGGER.info("请求：{}",rep.getRequestURI());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        LOGGER.info("销毁跨域支持");
    }

}
