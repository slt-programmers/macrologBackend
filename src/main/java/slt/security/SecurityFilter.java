package slt.security;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(1)
@Slf4j
public class SecurityFilter implements Filter {

    private String allowOrigin = null;

    protected String getAllowOrigin() {
        return allowOrigin;
    }

    @Override
    public void init(final FilterConfig filterConfig) {
        if (allowOrigin == null) {
            final var allowOriginEnv = getFromEnvironment();
            allowOrigin = StringUtils.isEmpty(allowOriginEnv) ? "http://localhost:4200" : allowOriginEnv;
        }
        log.debug("Security filter init");
        log.debug("Only accepting requests form {}", allowOrigin);
    }

    protected String getFromEnvironment() {
        return System.getenv("allow.crossorigin"); //NOSONAR
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if ("OPTIONS".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) {
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
            ((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin", getAllowOrigin());
            ((HttpServletResponse) response).setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
            ((HttpServletResponse) response).setHeader("Access-Control-Allow-Headers", "Authorization,Access-Control-Allow-Headers,Access-Control-Allow-Origin,Access-Control-Allow-Methods,Content-Type,Authorization");
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin", getAllowOrigin());
            ((HttpServletResponse) response).setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
            ((HttpServletResponse) response).setHeader("Access-Control-Max-Age", "3600");
            ((HttpServletResponse) response).setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Access-Control-Allow-Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

            final var req = (HttpServletRequest) request;
            log.debug("Starting req : {}", req.getRequestURI());
            final var token = req.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer")) {
                final var jwtToken = token.substring("Bearer".length() + 1);
                try {
                    final var claimsJws = Jwts.parser().setSigningKey(SecurityConstants.SECRET.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(jwtToken);
                    final var userId = claimsJws.getBody().get("userId");
                    final var userInfo =  UserInfo.builder()
                            .userId(Long.valueOf(userId.toString()))
                            .build();
                    ThreadLocalHolder.getThreadLocal().set(userInfo);
                    chain.doFilter(request, response);
                } catch (ExpiredJwtException expiredEx) {
                    log.debug("Expired JWT token.");
                    ((HttpServletResponse) response).sendError(403, "Expired session");
                } catch (MalformedJwtException malformedJwt) {
                    log.debug("Incorrect token");
                    ((HttpServletResponse) response).sendError(403, "Invalid token");
                }
            } else if (isPublicResourceURL((HttpServletRequest) request)) {
                chain.doFilter(request, response);
            } else {
                ((HttpServletResponse) response).sendError(403);
            }
            log.debug("Finish req : {}", req.getRequestURI());
        }
    }

    protected boolean isPublicResourceURL(final HttpServletRequest request) {
        return request.getRequestURI().contains("/healthcheck") ||
                request.getRequestURI().contains("/api/authenticate") ||
                request.getRequestURI().contains("/swagger-ui/") ||
                request.getRequestURI().contains("/v3/api-docs") ||
                request.getRequestURI().contains("/h2-console") ||
                request.getRequestURI().contains("/webhooks/public");
    }

    @Override
    public void destroy() {
        // Do nothing
    }

}