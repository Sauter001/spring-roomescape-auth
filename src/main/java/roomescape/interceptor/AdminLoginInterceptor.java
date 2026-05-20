package roomescape.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.User;
import roomescape.exception.ForbiddenException;
import roomescape.exception.UnauthorizedException;
import roomescape.exception.code.ForbiddenCode;
import roomescape.exception.code.UnauthorizedCode;
import roomescape.provider.AuthorizationExtractor;
import roomescape.provider.JwtProvider;
import roomescape.repository.UserRepository;

import java.io.IOException;

@Component
public class AdminLoginInterceptor implements HandlerInterceptor {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public AdminLoginInterceptor(JwtProvider jwtProvider, UserRepository userRepository) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        try {
            String token = AuthorizationExtractor.extract(request);
            long id = jwtProvider.getId(token);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UnauthorizedException(UnauthorizedCode.LOGIN_REQUIRED));
            if (!user.role().hasAdminAccess()) {
                throw new ForbiddenException(ForbiddenCode.ADMIN_ACCESS_DENIED);
            }
            return true;
        } catch (UnauthorizedException | ForbiddenException e) {
            if (request.getRequestURI().startsWith("/api/")) {
                throw e;
            }
            response.sendRedirect("/");
            return false;
        }
    }
}
