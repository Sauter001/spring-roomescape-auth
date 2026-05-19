package roomescape.service;

import org.junit.jupiter.api.Test;
import roomescape.fake.FakeUserRepository;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceTest {

    @Test
    void 아이디와_비밀번호가_일치하면_userId를_반환한다() {
        AuthService authService = new AuthService(
                new FakeUserRepository(Map.of("admin", "admin123"), Map.of("admin", 1L), Map.of()));

        Optional<Long> result = authService.doLogin("admin", "admin123");

        assertThat(result).contains(1L);
    }

    @Test
    void 비밀번호가_틀리면_empty를_반환한다() {
        AuthService authService = new AuthService(
                new FakeUserRepository(Map.of("admin", "admin123"), Map.of("admin", 1L), Map.of()));

        Optional<Long> result = authService.doLogin("admin", "wrong");

        assertThat(result).isEmpty();
    }

    @Test
    void 존재하지_않는_아이디면_empty를_반환한다() {
        AuthService authService = new AuthService(
                new FakeUserRepository(Map.of("admin", "admin123"), Map.of("admin", 1L), Map.of()));

        Optional<Long> result = authService.doLogin("nobody", "anything");

        assertThat(result).isEmpty();
    }
}
