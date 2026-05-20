package roomescape.resolver;

import jakarta.servlet.http.HttpServletRequest;
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
import roomescape.provider.AuthorizationExtractor;
import roomescape.provider.JwtProvider;
import roomescape.repository.UserRepository;

import java.util.Objects;

public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public LoginUserArgumentResolver(UserRepository userRepository, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
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
        HttpServletRequest request = Objects.requireNonNull(webRequest.getNativeRequest(HttpServletRequest.class));

        String token = AuthorizationExtractor.extract(request);
        long uid = jwtProvider.getId(token);

        return userRepository.findById(uid).orElseThrow(LoginUserArgumentResolver::throwLoginRequiredException);
    }

    private static UnauthorizedException throwLoginRequiredException() {
        throw new UnauthorizedException(UnauthorizedCode.LOGIN_REQUIRED);
    }
}
