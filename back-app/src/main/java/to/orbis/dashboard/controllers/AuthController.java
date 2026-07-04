package to.orbis.dashboard.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;
import to.orbis.dashboard.models.dto.AuthUserDto;
import to.orbis.dashboard.models.dto.AuthDto;
import to.orbis.dashboard.services.admin.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @GetMapping("/ping")
    public String ping() {
        log.info("Service is alive");
        return "Service is alive";
    }

    @PostMapping("/login")
    public AuthDto login(@RequestBody AuthUserDto user) {
        try {
            log.debug("login: email={}", user.getUsername());
            return userService.login(user);
        } catch (Exception e) {
            throw new AuthenticationCredentialsNotFoundException("Wrong username/password");
        }
    }

    @ResponseStatus(value= HttpStatus.FORBIDDEN,
            reason="Wrong username/password")
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public void wrongUserPassword() {
        log.debug("wrong user/password");
    }
}
