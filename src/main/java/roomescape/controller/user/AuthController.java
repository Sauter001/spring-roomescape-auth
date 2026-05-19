package roomescape.controller.user;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.exception.UnauthorizedException;
import roomescape.exception.code.UnauthorizedCode;
import roomescape.request.LoginRequest;
import roomescape.service.AuthService;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        boolean success = authService.doLogin(loginRequest.uid(), loginRequest.password());

        if (success) {
            session.setAttribute("uid", loginRequest.uid());
            return ResponseEntity.ok().build();
        }
        throw new UnauthorizedException(UnauthorizedCode.LOGIN_FAILED);
    }
}
