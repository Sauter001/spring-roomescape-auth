package roomescape.controller.user;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.exception.UnauthorizedException;
import roomescape.exception.code.UnauthorizedCode;
import roomescape.provider.AuthorizationExtractor;
import roomescape.provider.JwtProvider;
import roomescape.request.LoginRequest;
import roomescape.service.AuthService;

import java.time.Duration;

@RestController
@RequestMapping("/api")
public class AuthController {
    private static final Duration TOKEN_VALIDITY = Duration.ofHours(1);

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
                .header(HttpHeaders.SET_COOKIE, tokenCookie(token, TOKEN_VALIDITY).toString()) // 브라우저
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)                           // 모바일/API
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokenCookie("", Duration.ZERO).toString())
                .build();
    }

    private static ResponseCookie tokenCookie(String value, Duration maxAge) {
        return ResponseCookie.from(AuthorizationExtractor.COOKIE_NAME, value)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();
    }
}
