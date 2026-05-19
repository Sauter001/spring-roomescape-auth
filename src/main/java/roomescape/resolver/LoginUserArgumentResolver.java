package roomescape.resolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.annotation.LoginUser;
import roomescape.domain.User;
import roomescape.exception.UnauthorizedException;
import roomescape.exception.code.UnauthorizedCode;
import roomescape.repository.UserRepository;

import java.util.Objects;

public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
    private static final String SESSION_KEY = "uid";
    private final UserRepository userRepository;

    public LoginUserArgumentResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginUser.class);
        boolean isUser = User.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && isUser;
    }

    @Nullable
    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  @Nullable WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpSession session = Objects.requireNonNull(request).getSession();

        if (Objects.isNull(session)) {
            throwLoginRequiredException();
        }
        Long uid = (Long) session.getAttribute(SESSION_KEY);

        if (Objects.isNull(uid)) {
            throwLoginRequiredException();
        }

        return userRepository.findById(uid).orElseThrow(LoginUserArgumentResolver::throwLoginRequiredException);
    }

    private static UnauthorizedException throwLoginRequiredException() {
        throw new UnauthorizedException(UnauthorizedCode.LOGIN_REQUIRED);
    }
}
