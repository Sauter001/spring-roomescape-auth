package roomescape.repository;

import roomescape.domain.User;

import java.util.Optional;

public interface UserRepository {

    boolean existUserWithIdAndPwd(String uid, String password);

    Optional<User> findById(Long uid);
}
