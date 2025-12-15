package com.jsalva.trainerworkload.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class TransactionIdFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(TransactionIdFilter.class);

    private static final String TX_HEADER = "X-Transaction-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String transactionId = httpRequest.getHeader(TX_HEADER);

        if (transactionId == null || transactionId.isBlank()) {
            transactionId = "WLMS-"+UUID.randomUUID().toString().substring(0, 8);
        }

        try {
            MDC.put("transactionId", transactionId);

            logger.debug(
                    "Incoming request [{}] {} {}",
                    transactionId,
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI()
            );

            filterChain.doFilter(request, response);

        } finally {
            httpResponse.setHeader(TX_HEADER, transactionId);
            MDC.clear();
        }
    }
}
