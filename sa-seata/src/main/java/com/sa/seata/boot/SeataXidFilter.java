package com.sa.seata.boot;

import com.sa.seata.consts.SeataConsts;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class SeataXidFilter extends OncePerRequestFilter {
    protected Logger logger = LoggerFactory.getLogger(SeataXidFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String xid = RootContext.getXID();
        String restXid = request.getHeader(SeataConsts.XID);

        if(null == restXid || null != xid){
            filterChain.doFilter(request, response);
            return;
        }
        RootContext.bind(restXid);
        if (logger.isDebugEnabled()) {
            logger.debug("bind[{}] to RootContext", restXid);
        }
        try{
            filterChain.doFilter(request, response);
        } finally {
            String unbindXid = RootContext.unbind();
            if (logger.isDebugEnabled()) {
                logger.debug("unbind[{}] from RootContext", unbindXid);
            }
            if (!restXid.equalsIgnoreCase(unbindXid)) {
                logger.warn("xid in change during http rest from [{}] to [{}]", restXid, unbindXid);
                if (unbindXid != null) {
                    RootContext.bind(unbindXid);
                    logger.warn("bind [{}] back to RootContext", unbindXid);
                }
            }
        }
    }
}
