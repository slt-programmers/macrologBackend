package slt.security;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import slt.util.JWTBuilder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurityFilterTest {

    @Test
    void testInitZonderAllowGezet() {
        final var securityFilter = new SecurityFilter();
        securityFilter.init(null);
        Assertions.assertThat(securityFilter.getAllowOrigin()).isEqualTo("http://localhost:4200");
    }

    @Test
    void testInitMetAllowGezet() {
        final var ssecurityFilter = Mockito.spy(SecurityFilter.class);
        when(ssecurityFilter.getFromEnvironment()).thenReturn("server1");
        ssecurityFilter.init(null);
        Assertions.assertThat(ssecurityFilter.getAllowOrigin()).isEqualTo("server1");
    }

    @Test
    void doFilterOPTIONS() throws IOException, ServletException {
        final var securityFilter = Mockito.spy(SecurityFilter.class);
        when(securityFilter.getAllowOrigin()).thenReturn("server1");

        HttpServletRequest requestMock = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse responseMock = Mockito.mock(HttpServletResponse.class);
        FilterChain filterChainMock = Mockito.mock(FilterChain.class);


        when(requestMock.getMethod()).thenReturn("OPTIONS");
        securityFilter.doFilter(requestMock, responseMock, filterChainMock);

        verify(responseMock).setHeader("Access-Control-Allow-Origin", "server1");
        verify(responseMock).setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        verify(responseMock).setHeader("Access-Control-Allow-Headers", "Authorization,Access-Control-Allow-Headers,Access-Control-Allow-Origin,Access-Control-Allow-Methods,Content-Type,Authorization");
        verify(filterChainMock).doFilter(any(), any());
    }

    @Test
    void doFilterZonderToken403() throws IOException, ServletException {
        final var securityFilter = Mockito.spy(SecurityFilter.class);
        when(securityFilter.getAllowOrigin()).thenReturn("server1");

        final var requestMock = Mockito.mock(HttpServletRequest.class);
        final var responseMock = Mockito.mock(HttpServletResponse.class);
        final var filterChainMock = Mockito.mock(FilterChain.class);

        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn(null);
        when(requestMock.getRequestURI()).thenReturn("/food");

        securityFilter.doFilter(requestMock, responseMock, filterChainMock);

        verify(responseMock).setHeader("Access-Control-Allow-Origin", "server1");
        verify(responseMock).setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        verify(responseMock).setHeader("Access-Control-Max-Age", "3600");
        verify(responseMock).setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Access-Control-Allow-Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        verify(responseMock).sendError(403);
    }

    @Test
    void doFilterZonderTokenSwagger() throws IOException, ServletException {
        final var securityFilter = Mockito.spy(SecurityFilter.class);
        when(securityFilter.getAllowOrigin()).thenReturn("server1");

        final var requestMock = Mockito.mock(HttpServletRequest.class);
        final var responseMock = Mockito.mock(HttpServletResponse.class);
        final var filterChainMock = Mockito.mock(FilterChain.class);

        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn(null);

        doReturn(true).when(securityFilter).isPublicResourceURL(any());

        securityFilter.doFilter(requestMock, responseMock, filterChainMock);

        verify(responseMock).setHeader("Access-Control-Allow-Origin", "server1");
        verify(responseMock).setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        verify(responseMock).setHeader("Access-Control-Max-Age", "3600");
        verify(responseMock).setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Access-Control-Allow-Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        verify(filterChainMock).doFilter(any(), any());
    }

    @Test
    void doFilterMetInvalidToken() throws IOException, ServletException {
        final var securityFilter = Mockito.spy(SecurityFilter.class);
        when(securityFilter.getAllowOrigin()).thenReturn("server1");

        final var requestMock = Mockito.mock(HttpServletRequest.class);
        final var responseMock = Mockito.mock(HttpServletResponse.class);
        final var filterChainMock = Mockito.mock(FilterChain.class);

        when(requestMock.getMethod()).thenReturn("GET");
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer 1234.1234.1234");

        doReturn(true).when(securityFilter).isPublicResourceURL(any());

        securityFilter.doFilter(requestMock, responseMock, filterChainMock);

        verify(responseMock).setHeader("Access-Control-Allow-Origin", "server1");
        verify(responseMock).setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        verify(responseMock).setHeader("Access-Control-Max-Age", "3600");
        verify(responseMock).setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Access-Control-Allow-Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        verify(responseMock).sendError(403, "Invalid token");
    }

    @Test
    void doFilterMetExpiredToken() throws IOException, ServletException {
        final var securityFilter = Mockito.spy(SecurityFilter.class);
        when(securityFilter.getAllowOrigin()).thenReturn("server1");

        final var requestMock = Mockito.mock(HttpServletRequest.class);
        final var responseMock = Mockito.mock(HttpServletResponse.class);
        final var filterChainMock = Mockito.mock(FilterChain.class);

        when(requestMock.getMethod()).thenReturn("GET");

        final var jwtBuilder = new JWTBuilder();
        final var gisteren = LocalDate.now().minusDays(1);

        final var expiredJWT = jwtBuilder.generateJWT("junit", 1L, Date.from(gisteren.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + expiredJWT);

        doReturn(true).when(securityFilter).isPublicResourceURL(any());

        securityFilter.doFilter(requestMock, responseMock, filterChainMock);

        verify(responseMock).setHeader("Access-Control-Allow-Origin", "server1");
        verify(responseMock).setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        verify(responseMock).setHeader("Access-Control-Max-Age", "3600");
        verify(responseMock).setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Access-Control-Allow-Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        verify(responseMock).sendError(403, "Expired session");
    }

    @Test
    void doFilterMetOKToken() throws IOException, ServletException {
        final var securityFilter = Mockito.spy(SecurityFilter.class);
        when(securityFilter.getAllowOrigin()).thenReturn("server1");

        final var requestMock = Mockito.mock(HttpServletRequest.class);
        final var responseMock = Mockito.mock(HttpServletResponse.class);
        final var filterChainMock = Mockito.mock(FilterChain.class);

        when(requestMock.getMethod()).thenReturn("GET");

        final var jwtBuilder = new JWTBuilder();
        final var morgen = LocalDate.now().plusDays(1);

        final var expiredJWT = jwtBuilder.generateJWT("junit", 1L, Date.from(morgen.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        when(requestMock.getHeader("Authorization")).thenReturn("Bearer " + expiredJWT);

        doReturn(true).when(securityFilter).isPublicResourceURL(any());

        securityFilter.doFilter(requestMock, responseMock, filterChainMock);

        verify(responseMock).setHeader("Access-Control-Allow-Origin", "server1");
        verify(responseMock).setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        verify(responseMock).setHeader("Access-Control-Max-Age", "3600");
        verify(responseMock).setHeader("Access-Control-Allow-Headers", "Access-Control-Allow-Headers, Access-Control-Allow-Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

        verify(filterChainMock).doFilter(any(), any());
    }

}