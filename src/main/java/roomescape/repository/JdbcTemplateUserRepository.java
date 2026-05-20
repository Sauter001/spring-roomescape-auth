package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.Role;
import roomescape.domain.User;

import java.util.Optional;

@Repository
public class JdbcTemplateUserRepository implements UserRepository {
    private static final RowMapper<User> USER_ROW_MAPPER =
            (rs, rowNum) -> new User(
                    rs.getLong("id"),
                    rs.getString("uid"),
                    rs.getString("name"),
                    Role.valueOf(rs.getString("role")));

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Long> findIdByUidAndPassword(String uid, String password) {
        try {
            Long id = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE uid = ? AND password = ?",
                    Long.class, uid, password);
            return Optional.ofNullable(id);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT id, uid, name, role FROM users WHERE id = ?",
                    USER_ROW_MAPPER, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
