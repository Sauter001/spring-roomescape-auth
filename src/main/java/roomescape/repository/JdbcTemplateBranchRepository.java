package roomescape.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.Branch;

import java.util.Optional;

@Repository
public class JdbcTemplateBranchRepository implements BranchRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateBranchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Branch> findByManagerId(Long managerId) {
        try {
            Branch branch = jdbcTemplate.queryForObject(
                    "SELECT b.id, b.name FROM branch b " +
                            "JOIN branch_manager bm ON b.id = bm.branch_id " +
                            "WHERE bm.user_id = ?",
                    (rs, rowNum) -> new Branch(rs.getLong("id"), rs.getString("name")),
                    managerId);
            return Optional.ofNullable(branch);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
