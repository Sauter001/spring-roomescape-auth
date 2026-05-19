package roomescape.fake;

import roomescape.domain.User;
import roomescape.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FakeUserRepository implements UserRepository {
    private final Map<String, String> credentials;
    private final Map<Long, User> usersById;

    public FakeUserRepository(Map<String, String> credentials) {
        this(credentials, Map.of());
    }

    public FakeUserRepository(Map<String, String> credentials, Map<Long, User> usersById) {
        this.credentials = new HashMap<>(credentials);
        this.usersById = new HashMap<>(usersById);
    }

    @Override
    public boolean existUserWithIdAndPwd(String uid, String password) {
        return Objects.equals(credentials.get(uid), password);
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(usersById.get(id));
    }
}
