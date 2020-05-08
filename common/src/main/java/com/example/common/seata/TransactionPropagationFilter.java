package com.example.common.seata;

import com.jds.core.utils.BaseUtil;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TransactionPropagationFilter extends OncePerRequestFilter {
    protected Logger logger = LoggerFactory.getLogger(TransactionPropagationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String xid = RootContext.getXID();
        String restXid = request.getHeader("rpc_xid");
        boolean bind = false;
        if (BaseUtil.isNullOrEmpty(xid) && !BaseUtil.isNullOrEmpty(restXid)) {
            RootContext.bind(restXid);
            bind = true;

            if (logger.isDebugEnabled()) {
                logger.debug("bind[" + restXid + "] to RootContext");
            }
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (bind) {
                String unbindXid = RootContext.unbind();
                if (logger.isDebugEnabled()) {
                    logger.debug("unbind[" + unbindXid + "] from RootContext");
                }
                if (!restXid.equalsIgnoreCase(unbindXid)) {
                    logger.warn("xid in change during http rest from " + restXid + " to " + unbindXid);
                    if (unbindXid != null) {
                        RootContext.bind(unbindXid);
                        logger.warn("bind [" + unbindXid + "] back to RootContext");
                    }
                }
            }
        }
    }
}
