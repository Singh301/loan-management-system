package com.sudhanshu.loanmanagement.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Populates MDC with correlation ID, user, API path and timing for structured logging.
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String CORRELATION_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_CORRELATION_ID = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();

        try {
            String correlationId = request.getHeader(CORRELATION_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            MDC.put(MDC_REQUEST_ID, correlationId);
            MDC.put(MDC_CORRELATION_ID, correlationId);
            MDC.put("method", request.getMethod());
            MDC.put("api", request.getRequestURI());
            MDC.put("ip", clientIp(request));

            // Echo correlation id back to client
            response.setHeader(CORRELATION_HEADER, correlationId);

            filterChain.doFilter(request, response);

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null
                    && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                MDC.put("username", authentication.getName());
            } else {
                MDC.put("username", "Anonymous");
            }

        } finally {
            long executionTime = System.currentTimeMillis() - start;
            MDC.put("status", String.valueOf(response.getStatus()));
            MDC.put("executionTime", executionTime + " ms");

            log.info("Request completed");

            MDC.clear();
        }
    }

    private String clientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
