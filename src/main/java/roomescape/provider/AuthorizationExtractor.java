package roomescape.provider;

import jakarta.servlet.http.HttpServletRequest;
import roomescape.exception.UnauthorizedException;
import roomescape.exception.code.UnauthorizedCode;

public class AuthorizationExtractor {
    private static final String HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private AuthorizationExtractor() {
    }

    public static String extract(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException(UnauthorizedCode.LOGIN_REQUIRED);
        }
        return header.substring(BEARER_PREFIX.length());
    }
}
