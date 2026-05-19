package roomescape.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.exception.UnauthorizedException;
import roomescape.exception.code.UnauthorizedCode;

@Component
public class AdminLoginInterceptor implements HandlerInterceptor {
    private static final String SESSION_KEY = "uid";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_KEY) == null) {
            throw new UnauthorizedException(UnauthorizedCode.LOGIN_REQUIRED);
        }
        return true;
    }
}
