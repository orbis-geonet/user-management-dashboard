package to.orbis.dashboard.config;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.filter.CorsFilter;
import to.orbis.dashboard.config.security.AuthenticationTokenExtractorFilter;
import to.orbis.dashboard.config.security.JwtService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Setter
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtService jwtService;
    @Value("${app.auth.header}")
    private String authHeader;

    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private static final String PING_ENDPOINT = "/api/v1/keep-alive";

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers(LOGIN_ENDPOINT);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .logout().disable()
                .formLogin().disable()
                .sessionManagement().disable()
                .requestCache().disable()
                .anonymous().and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, LOGIN_ENDPOINT).permitAll()
                .antMatchers(HttpMethod.GET, PING_ENDPOINT).permitAll()
                .antMatchers("/api/v1/public/**").permitAll()
                .antMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
                .and()
                .addFilterBefore(
                        new AuthenticationTokenExtractorFilter(jwtService, authHeader),
                        CorsFilter.class
                );

        http.cors();
    }
}
