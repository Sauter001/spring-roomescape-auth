package roomescape.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.exception.UnauthorizedException;
import roomescape.exception.code.UnauthorizedCode;
import roomescape.provider.JwtProvider;
import roomescape.request.LoginRequest;
import roomescape.service.AuthService;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService;
    private final JwtProvider jwtProvider;

    public AuthController(AuthService authService, JwtProvider jwtProvider) {
        this.authService = authService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest) {
        Long userId = authService.doLogin(loginRequest.uid(), loginRequest.password())
                .orElseThrow(() -> new UnauthorizedException(UnauthorizedCode.LOGIN_FAILED));

        String token = jwtProvider.createToken(userId);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .build();
    }
}
