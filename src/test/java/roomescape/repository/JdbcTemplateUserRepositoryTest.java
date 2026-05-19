package roomescape.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(JdbcTemplateUserRepository.class)
@Sql({"/test-truncate.sql", "/test-user.sql"})
class JdbcTemplateUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void 자격증명이_일치하면_사용자_id를_반환한다() {
        Optional<Long> id = userRepository.findIdByUidAndPassword("admin", "admin123");

        assertThat(id).contains(1L);
    }

    @Test
    void 비밀번호가_틀리면_empty를_반환한다() {
        assertThat(userRepository.findIdByUidAndPassword("admin", "wrong")).isEmpty();
    }

    @Test
    void 존재하지_않는_아이디면_empty를_반환한다() {
        assertThat(userRepository.findIdByUidAndPassword("nobody", "any")).isEmpty();
    }

    @Test
    void id로_사용자를_조회한다() {
        Optional<User> user = userRepository.findById(1L);

        assertThat(user).isPresent();
        assertThat(user.get().uid()).isEqualTo("admin");
        assertThat(user.get().name()).isEqualTo("관리자");
    }

    @Test
    void 존재하지_않는_id면_empty를_반환한다() {
        assertThat(userRepository.findById(9999L)).isEmpty();
    }
}
