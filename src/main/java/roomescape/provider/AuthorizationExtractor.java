package roomescape.provider;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import roomescape.exception.UnauthorizedException;
import roomescape.exception.code.UnauthorizedCode;

public class AuthorizationExtractor {
    public static final String COOKIE_NAME = "token";
    private static final String HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private AuthorizationExtractor() {
    }

    public static String extract(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }

        String cookieToken = extractFromCookie(request);
        if (cookieToken != null) {
            return cookieToken;
        }

        throw new UnauthorizedException(UnauthorizedCode.LOGIN_REQUIRED);
    }

    private static String extractFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
