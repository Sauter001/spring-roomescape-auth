package roomescape.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcTemplateUserRepository.class)
@Sql({"/test-truncate.sql", "/test-user.sql"})
class JdbcTemplateUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 아이디와_비밀번호가_일치하는_사용자가_있으면_true를_반환한다() {
        assertThat(userRepository.existUserWithIdAndPwd("admin", "admin123")).isTrue();
    }

    @Test
    void 비밀번호가_틀리면_false를_반환한다() {
        assertThat(userRepository.existUserWithIdAndPwd("admin", "wrong")).isFalse();
    }

    @Test
    void 존재하지_않는_아이디면_false를_반환한다() {
        assertThat(userRepository.existUserWithIdAndPwd("nobody", "any")).isFalse();
    }
}
