package roomescape.fake;

import roomescape.domain.User;
import roomescape.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class FakeUserRepository implements UserRepository {
    private final Map<String, String> credentials;
    private final Map<String, Long> idsByUid;
    private final Map<Long, User> usersById;

    public FakeUserRepository(Map<String, String> credentials) {
        this(credentials, Map.of(), Map.of());
    }

    public FakeUserRepository(Map<String, String> credentials,
                              Map<String, Long> idsByUid,
                              Map<Long, User> usersById) {
        this.credentials = new HashMap<>(credentials);
        this.idsByUid = new HashMap<>(idsByUid);
        this.usersById = new HashMap<>(usersById);
    }

    @Override
    public Optional<Long> findIdByUidAndPassword(String uid, String password) {
        if (!Objects.equals(credentials.get(uid), password)) {
            return Optional.empty();
        }
        return Optional.ofNullable(idsByUid.get(uid));
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(usersById.get(id));
    }
}
