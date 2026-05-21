package roomescape.repository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class JdbcTemplateThemeRepository implements ThemeRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Theme> findAll() {
        return jdbcTemplate.query("SELECT id, branch_id, name, description, thumbnail_url FROM theme",
                (rs, rowNum) -> new Theme(
                        rs.getLong("id"),
                        rs.getLong("branch_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("thumbnail_url")
                ));
    }

    @Override
    public List<Theme> findThemesToManage(Long managerId) {
        return jdbcTemplate.query(
                "SELECT th.id, th.branch_id, th.name, th.description, th.thumbnail_url " +
                        "FROM theme th " +
                        "JOIN branch_manager bm ON th.branch_id = bm.branch_id " +
                        "WHERE bm.user_id = ?",
                (rs, rowNum) -> new Theme(
                        rs.getLong("id"),
                        rs.getLong("branch_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("thumbnail_url")
                ),
                managerId);
    }

    @Override
    public Theme save(Theme theme) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                conn -> {
                    PreparedStatement preparedStatement = conn.prepareStatement(
                            "INSERT INTO theme(branch_id, name, description, thumbnail_url) " +
                                    "VALUES (?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);

                    preparedStatement.setLong(1, theme.branchId());
                    preparedStatement.setString(2, theme.name());
                    preparedStatement.setString(3, theme.description());
                    preparedStatement.setString(4, theme.thumbnailUrl());

                    return preparedStatement;
                },
                keyHolder);

        return new Theme(
                Objects.requireNonNull(keyHolder.getKey()).longValue(),
                theme.branchId(),
                theme.name(),
                theme.description(),
                theme.thumbnailUrl());
    }

    @Override
    public void delete(Long id) {
        try {
            jdbcTemplate.update("DELETE FROM theme WHERE id = ?", id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public Optional<Theme> findById(Long id) {
        try {
            Theme theme = jdbcTemplate.queryForObject(
                    "SELECT id, branch_id, name, description, thumbnail_url FROM theme WHERE id = ?",
                    (rs, rowNum) -> new Theme(
                            rs.getLong("id"),
                            rs.getLong("branch_id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("thumbnail_url")
                    ),
                    id);

            return Optional.ofNullable(theme);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findPopularThemes(LocalDate startInclusive, LocalDate endInclusive, int limit) {
        return jdbcTemplate.query(
                "SELECT th.id, th.branch_id, th.name, th.description, th.thumbnail_url " +
                        "FROM reservation r JOIN theme th ON r.theme_id = th.id " +
                        "WHERE r.date BETWEEN ? AND ? " +
                        "GROUP BY th.id, th.branch_id, th.name, th.description, th.thumbnail_url " +
                        "ORDER BY COUNT(r.id) DESC " +
                        "LIMIT ?",
                (rs, rowNum) -> {
                    long id = rs.getLong("id");
                    long branchId = rs.getLong("branch_id");
                    String name = rs.getString("name");
                    String description = rs.getString("description");
                    String thumbnailUrl = rs.getString("thumbnail_url");
                    return new Theme(id, branchId, name, description, thumbnailUrl);
                },
                startInclusive, endInclusive, limit
        );
    }
}
