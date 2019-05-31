package csl.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Order(1)
public class SecurityFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.debug("Security filter init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        if ("OPTIONS".equalsIgnoreCase(((HttpServletRequest) request).getMethod())) {
            LOGGER.debug("precheck");
            ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
            ((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin", "*");
            ((HttpServletResponse) response).setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE,PUT");
            ((HttpServletResponse) response).setHeader("Access-Control-Allow-Headers","Authorization,Access-Control-Allow-Headers,Access-Control-Allow-Origin,Access-Control-Allow-Methods,Content-Type,Authorization");
            chain.doFilter(request, response);
        } else {
            LOGGER.debug("actual");
            String getenv = System.getenv("allow.crossorigin");
            if (getenv == null || getenv.equals("")){
                getenv="http://localhost:4200";
            }

            ((HttpServletResponse) response).setHeader("Access-Control-Allow-Origin", getenv);
            ((HttpServletResponse) response).setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE,PUT");
            ((HttpServletResponse) response).setHeader("Access-Control-Max-Age", "3600");
            ((HttpServletResponse) response).setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Access-Control-Allow-Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");


            HttpServletRequest req = (HttpServletRequest) request;
            LOGGER.info("Starting req : {}", req.getRequestURI());
            String token = req.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer")) {
                String jwtToken = token.substring("Bearer".length() + 1);
                LOGGER.debug(jwtToken);
                Object userId;
                try {
                    Jws<Claims> claimsJws = Jwts.parser().setSigningKey(SecurityConstants.SECRET.getBytes(StandardCharsets.UTF_8)).parseClaimsJws(jwtToken);
                    userId = claimsJws.getBody().get("userId");
                    LOGGER.debug("Userid from token = " + userId);
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserId(Integer.valueOf(userId.toString()));
                    ThreadLocalHolder.getThreadLocal().set(userInfo);
                    chain.doFilter(request, response);
                } catch (ExpiredJwtException expiredEx) {
                    LOGGER.debug("ExpiredJWT token.");
                    ((HttpServletResponse) response).sendError(403,"Expired session");
                }
            } else if (((HttpServletRequest) request).getRequestURI().startsWith("/swagger-resources") ||
                    ((HttpServletRequest) request).getRequestURI().startsWith("/webjars/") ||
                    ((HttpServletRequest) request).getRequestURI().startsWith("/v2/api-docs") ||
                    ((HttpServletRequest) request).getRequestURI().startsWith("/swagger-ui.html")) {
                LOGGER.debug("Swagger");
                chain.doFilter(request, response);

            } else if (((HttpServletRequest) request).getRequestURI().startsWith("/api/")) {
                LOGGER.debug("Unsecured section of website");
                chain.doFilter(request, response);
            } else {
                ((HttpServletResponse) response).sendError(403);
            }
            LOGGER.info("Token = " + token);
            LOGGER.info("Finish req : {}", req.getRequestURI());
        }
    }

    @Override
    public void destroy() {

    }

}