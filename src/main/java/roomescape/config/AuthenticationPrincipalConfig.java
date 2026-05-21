package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.domain.Role;
import roomescape.interceptor.RoleInterceptor;
import roomescape.provider.JwtProvider;
import roomescape.repository.UserRepository;
import roomescape.resolver.LoginUserArgumentResolver;

import java.util.List;

@Configuration
public class AuthenticationPrincipalConfig implements WebMvcConfigurer {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    public AuthenticationPrincipalConfig(UserRepository userRepository, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginUserArgumentResolver(userRepository, jwtProvider));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RoleInterceptor(jwtProvider, userRepository, Role.ADMIN))
                .addPathPatterns("/api/admin/**", "/admin/**");
        registry.addInterceptor(new RoleInterceptor(jwtProvider, userRepository, Role.MANAGER))
                .addPathPatterns("/api/manager/**", "/manager/**");
    }
}
