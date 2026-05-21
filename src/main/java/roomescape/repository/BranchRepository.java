package roomescape.repository;

import roomescape.domain.Branch;

import java.util.Optional;

public interface BranchRepository {
    Optional<Branch> findByManagerId(Long managerId);
}
