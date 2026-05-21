package roomescape.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.annotation.RequireRole;
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
import java.util.Arrays;
import java.util.List;

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
            authorize(user, handler);
            return true;
        } catch (UnauthorizedException | ForbiddenException e) {
            if (request.getRequestURI().startsWith("/api/")) {
                throw e;
            }
            response.sendRedirect("/");
            return false;
        }
    }

    private void authorize(User user, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            return;
        }
        List<Role> roles = Arrays.asList(requireRole.value());
        if (!roles.contains(user.role())) {
            throw new ForbiddenException(ForbiddenCode.ACCESS_DENIED);
        }
    }
}
