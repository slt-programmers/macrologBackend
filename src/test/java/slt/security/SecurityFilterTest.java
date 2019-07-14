package slt.security;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slt.util.JWTBuilder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityFilterTest {

   @Test
    void testInitZonderAllowGezet() {

        SecurityFilter sf = new SecurityFilter();
        sf.init(null);
        Assertions.assertThat(sf.getAllowOrigin()).isEqualTo("http://localhost:4200");

    }

    @Test
    void testIniMetAllowGezet() {

        SecurityFilter sf = Mockito.spy(SecurityFilter.class);
        when(sf.getFromEnvironment()).thenReturn("server1");
        sf.init(null);
        Assertions.assertThat(sf.getAllowOrigin()).isEqualTo("server1");

    }


    @Test
    void doFilterOPTIONS() throws IOException, ServletException {

        SecurityFilter sf = Mockito.spy(SecurityFilter.class);
        when(sf.getAllowOrigin()).thenReturn("server1");

        HttpServletRequest requestMock= Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChainMock = Mockito.mock(FilterChain.class);


        when(requestMock.getMethod()).thenReturn("OPTIONS");
        sf.doFilter(requestMock, responseMock,filterChainMock );

        verify(responseMock).setHeader("Access-Control-Allow-Origin","server1");
        verify(responseMock).setHeader("Access-Control-Allow-Methods","POST, GET, OPTIONS, DELETE, PUT");
        verify(responseMock).setHeader("Access-Control-Allow-Headers","Authorization,Access-Control-Allow-Headers,Access-Control-Allow-Origin,Access-Control-Allow-Methods,Content-Type,Authorization");
        verify(filterChainMock).doFilter(any(), any());
    }

    @Test
    void doFilterZonderToken403() throws IOException, ServletException {

        SecurityFilter sf = Mockito.spy(SecurityFilter.class);
        when(sf.getAllowOrigin()).thenReturn("server1");

        HttpServletRequest requestMock= Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChainMock = Mockito.mock(FilterChain.class);


        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn(null);
        when(requestMock.getRequestURI()).thenReturn("/food");

        sf.doFilter(requestMock, responseMock,filterChainMock );

        verify(responseMock).setHeader("Access-Control-Allow-Origin","server1");
        verify(responseMock).setHeader("Access-Control-Allow-Methods","POST, GET, OPTIONS, DELETE, PUT");
        verify(responseMock).setHeader("Access-Control-Max-Age","3600");
        verify(responseMock).setHeader("Access-Control-Allow-Headers","Access-Control-Allow-Headers, Access-Control-Allow-Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        verify(responseMock).sendError(403);
    }

    @Test
    void doFilterZonderTokenSwagger() throws IOException, ServletException {

        SecurityFilter sf = Mockito.spy(SecurityFilter.class);
        when(sf.getAllowOrigin()).thenReturn("server1");

        HttpServletRequest requestMock= Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChainMock = Mockito.mock(FilterChain.class);

        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn(null);

        doReturn(true).when(sf).isPublicResourceURL(any());

        sf.doFilter(requestMock, responseMock,filterChainMock );

        verify(responseMock).setHeader("Access-Control-Allow-Origin","server1");
        verify(responseMock).setHeader("Access-Control-Allow-Methods","POST, GET, OPTIONS, DELETE, PUT");
        verify(responseMock).setHeader("Access-Control-Max-Age","3600");
        verify(responseMock).setHeader("Access-Control-Allow-Headers","Access-Control-Allow-Headers, Access-Control-Allow-Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        verify(filterChainMock).doFilter(any(), any());
    }

    @Test
    void doFilterMetInvalidToken() throws IOException, ServletException {

        SecurityFilter sf = Mockito.spy(SecurityFilter.class);
        when(sf.getAllowOrigin()).thenReturn("server1");

        HttpServletRequest requestMock= Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChainMock = Mockito.mock(FilterChain.class);

        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer 1234.1234.1234");

        doReturn(true).when(sf).isPublicResourceURL(any());

        sf.doFilter(requestMock, responseMock,filterChainMock );

        verify(responseMock).setHeader("Access-Control-Allow-Origin","server1");
        verify(responseMock).setHeader("Access-Control-Allow-Methods","POST, GET, OPTIONS, DELETE, PUT");
        verify(responseMock).setHeader("Access-Control-Max-Age","3600");
        verify(responseMock).setHeader("Access-Control-Allow-Headers","Access-Control-Allow-Headers, Access-Control-Allow-Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        verify(responseMock).sendError(403,"Invalid token");
    }

    @Test
    void doFilterMetExpiredToken() throws IOException, ServletException {

        SecurityFilter sf = Mockito.spy(SecurityFilter.class);
        when(sf.getAllowOrigin()).thenReturn("server1");

        HttpServletRequest requestMock= Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChainMock = Mockito.mock(FilterChain.class);

        when(requestMock.getMethod()).thenReturn("GET");

        JWTBuilder jwtBuilder = new JWTBuilder();
        LocalDate gisteren = LocalDate.now().minusDays(1);

        String expiredJWT = jwtBuilder.generateJWT("junit", 1l, Date.from(gisteren.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + expiredJWT);

        doReturn(true).when(sf).isPublicResourceURL(any());

        sf.doFilter(requestMock, responseMock,filterChainMock );

        verify(responseMock).setHeader("Access-Control-Allow-Origin","server1");
        verify(responseMock).setHeader("Access-Control-Allow-Methods","POST, GET, OPTIONS, DELETE, PUT");
        verify(responseMock).setHeader("Access-Control-Max-Age","3600");
        verify(responseMock).setHeader("Access-Control-Allow-Headers","Access-Control-Allow-Headers, Access-Control-Allow-Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        verify(responseMock).sendError(403,"Expired session");
    }

    @Test
    void doFilterMetOKToken() throws IOException, ServletException {

        SecurityFilter sf = Mockito.spy(SecurityFilter.class);
        when(sf.getAllowOrigin()).thenReturn("server1");

        HttpServletRequest requestMock= Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChainMock = Mockito.mock(FilterChain.class);

        when(requestMock.getMethod()).thenReturn("GET");

        JWTBuilder jwtBuilder = new JWTBuilder();
        LocalDate morgen = LocalDate.now().plusDays(1);

        String expiredJWT = jwtBuilder.generateJWT("junit", 1l, Date.from(morgen.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + expiredJWT);

        doReturn(true).when(sf).isPublicResourceURL(any());

        sf.doFilter(requestMock, responseMock,filterChainMock );

        verify(responseMock).setHeader("Access-Control-Allow-Origin","server1");
        verify(responseMock).setHeader("Access-Control-Allow-Methods","POST, GET, OPTIONS, DELETE, PUT");
        verify(responseMock).setHeader("Access-Control-Max-Age","3600");
        verify(responseMock).setHeader("Access-Control-Allow-Headers","Access-Control-Allow-Headers, Access-Control-Allow-Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        verify(filterChainMock).doFilter(any(), any());
    }


}