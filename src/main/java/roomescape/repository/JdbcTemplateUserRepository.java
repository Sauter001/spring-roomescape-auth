package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class JdbcTemplateUserRepository implements UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existUserWithIdAndPwd(String uid, String password) {
        Integer rows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE uid = ? AND password = ?",
                Integer.class, uid, password);
        return Objects.requireNonNull(rows).equals(1);
    }
}
