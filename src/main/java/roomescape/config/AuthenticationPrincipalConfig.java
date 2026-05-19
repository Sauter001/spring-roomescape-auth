package roomescape.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.interceptor.AdminLoginInterceptor;
import roomescape.repository.UserRepository;
import roomescape.resolver.LoginUserArgumentResolver;

import java.util.List;

@Configuration
public class AuthenticationPrincipalConfig implements WebMvcConfigurer {
    private final UserRepository userRepository;
    private final AdminLoginInterceptor adminLoginInterceptor;

    public AuthenticationPrincipalConfig(UserRepository userRepository,
                                         AdminLoginInterceptor adminLoginInterceptor) {
        this.userRepository = userRepository;
        this.adminLoginInterceptor = adminLoginInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginUserArgumentResolver(userRepository));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminLoginInterceptor)
                .addPathPatterns("/api/admin/**");
    }
}
