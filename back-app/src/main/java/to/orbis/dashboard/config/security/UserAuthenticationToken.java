package to.orbis.dashboard.config.security;

import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@EqualsAndHashCode(callSuper = false)
public class UserAuthenticationToken extends AbstractAuthenticationToken {
    private final String userEmail;

    public UserAuthenticationToken(Collection<? extends GrantedAuthority> authorities, String userEmial) {
        super(authorities);
        this.userEmail = userEmial;
    }

    @Override
    public Object getCredentials() {
        return "n/a";
    }

    @Override
    public Object getPrincipal() {
        return userEmail;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }
}
