package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Branch;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Sql({"/test-truncate.sql", "/test-user.sql", "/test-theme.sql", "/test-branch-manager.sql"})
class JdbcTemplateBranchRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private BranchRepository branchRepository;

    @BeforeEach
    void setUp() {
        branchRepository = new JdbcTemplateBranchRepository(jdbcTemplate);
    }

    @Test
    void 매니저의_점포를_조회한다() {
        Optional<Branch> branch = branchRepository.findByManagerId(3L);

        assertThat(branch).isPresent();
        assertThat(branch.get().id()).isEqualTo(1L);
        assertThat(branch.get().name()).isEqualTo("1호점");
    }

    @Test
    void 매니저가_아닌_사용자는_empty를_반환한다() {
        assertThat(branchRepository.findByManagerId(1L)).isEmpty();
    }
}
