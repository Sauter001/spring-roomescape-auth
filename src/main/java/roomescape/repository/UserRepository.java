package roomescape.repository;

import roomescape.domain.User;

import java.util.Optional;

public interface UserRepository {

    Optional<Long> findIdByUidAndPassword(String uid, String password);

    Optional<User> findById(Long id);
}
