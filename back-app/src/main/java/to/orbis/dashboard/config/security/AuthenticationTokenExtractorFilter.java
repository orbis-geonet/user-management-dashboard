package to.orbis.dashboard.config.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@AllArgsConstructor
public class AuthenticationTokenExtractorFilter implements Filter {

    private JwtService jwtService;
    private String authHeader;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        authenticate(((HttpServletRequest) servletRequest).getHeader(authHeader));
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void authenticate(String authorizationHeader) {
        try {
            var jwt = authorizationHeader.replace("Bearer ", "");
            log.debug("authenticate: header=$authHeader, jwt=$jwt");

            if (jwtService.verifyJwt(jwt)) {
                var roles = new ArrayList<SimpleGrantedAuthority>();
                roles.add(new SimpleGrantedAuthority("SUPER_USER"));

                var user = new UserAuthenticationToken(roles, jwtService.getPayload(jwt).get("userEmail").toString());
                SecurityContextHolder.setContext(new SecurityContextImpl(user));
            }
        } catch (Exception e) {
            log.warn("authenticate: wrong auth header {}", authorizationHeader);
        }
    }
}
