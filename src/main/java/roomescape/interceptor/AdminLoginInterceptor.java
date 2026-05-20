package roomescape.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.provider.AuthorizationExtractor;
import roomescape.provider.JwtProvider;

@Component
public class AdminLoginInterceptor implements HandlerInterceptor {
    private final JwtProvider jwtProvider;

    public AdminLoginInterceptor(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = AuthorizationExtractor.extract(request);
        jwtProvider.getId(token); // 토큰 유효성 검증, 실패 시 UnauthorizedException
        return true;
    }
}
