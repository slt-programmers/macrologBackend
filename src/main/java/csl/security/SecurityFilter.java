package csl.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

//@Order(1)
public class SecurityFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
      LOGGER.debug("Security filter init");
    }

    @Override
    @CrossOrigin(origins = "http://localhost:4200")
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException

    {

        HttpServletRequest req = (HttpServletRequest) request;
        LOGGER.info("Starting req : {}", req.getRequestURI());
        String token = req.getHeader("token");
        LOGGER.info("Token = " + token);
        chain.doFilter(request, response);
        LOGGER.info("Finish req : {}", req.getRequestURI());
    }

    @Override
    public void destroy() {

    }

}