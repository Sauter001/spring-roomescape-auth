package roomescape.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.domain.Role;
import roomescape.domain.User;
import roomescape.exception.ForbiddenException;
import roomescape.exception.UnauthorizedException;
import roomescape.exception.code.ForbiddenCode;
import roomescape.exception.code.UnauthorizedCode;
import roomescape.provider.AuthorizationExtractor;
import roomescape.provider.JwtProvider;
import roomescape.repository.UserRepository;

import java.io.IOException;

public class RoleInterceptor implements HandlerInterceptor {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final Role requiredRole;

    public RoleInterceptor(JwtProvider jwtProvider, UserRepository userRepository, Role requiredRole) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.requiredRole = requiredRole;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        try {
            String token = AuthorizationExtractor.extract(request);
            long id = jwtProvider.getId(token);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UnauthorizedException(UnauthorizedCode.LOGIN_REQUIRED));
            if (!user.hasRole(requiredRole)) {
                throw new ForbiddenException(ForbiddenCode.ACCESS_DENIED);
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
