package com.whattheburger.backend.util.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.ttddyy.dsproxy.QueryCount;
import net.ttddyy.dsproxy.QueryCountHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Profile("sql-debug")
public class SqlCountFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(SqlCountFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        QueryCountHolder.clear();
        try {
            filterChain.doFilter(request, response);
        } finally {
            QueryCount count = QueryCountHolder.getGrandTotal();

            log.info(
                    "[SQL COUNT] {} {} | Total={} Select={} Update={} Insert={} Delete={} Time={}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    count.getTotal(),
                    count.getSelect(),
                    count.getUpdate(),
                    count.getInsert(),
                    count.getDelete(),
                    count.getTime()
            );

            QueryCountHolder.clear();
        }
    }
}
