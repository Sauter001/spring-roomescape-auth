package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.User;

import java.util.Objects;
import java.util.Optional;

@Repository
public class JdbcTemplateUserRepository implements UserRepository {
    private static final RowMapper<User> USER_ROW_MAPPER =
            (rs, rowNum) -> new User(rs.getString("uid"), rs.getString("name"));

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

    @Override
    public Optional<User> findById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT uid, name FROM users WHERE id = ?",
                    USER_ROW_MAPPER, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
