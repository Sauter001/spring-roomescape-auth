package roomescape.service;

import org.junit.jupiter.api.Test;
import roomescape.fake.FakeUserRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceTest {

    @Test
    void 아이디와_비밀번호가_일치하면_true를_반환한다() {
        AuthService authService = new AuthService(new FakeUserRepository(Map.of("admin", "admin123")));

        assertThat(authService.doLogin("admin", "admin123")).isTrue();
    }

    @Test
    void 비밀번호가_틀리면_false를_반환한다() {
        AuthService authService = new AuthService(new FakeUserRepository(Map.of("admin", "admin123")));

        assertThat(authService.doLogin("admin", "wrong")).isFalse();
    }

    @Test
    void 존재하지_않는_아이디면_false를_반환한다() {
        AuthService authService = new AuthService(new FakeUserRepository(Map.of("admin", "admin123")));

        assertThat(authService.doLogin("nobody", "anything")).isFalse();
    }
}
