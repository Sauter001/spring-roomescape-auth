package roomescape.fake;

import roomescape.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FakeUserRepository implements UserRepository {
    private final Map<String, String> users;

    public FakeUserRepository(Map<String, String> users) {
        this.users = new HashMap<>(users);
    }

    @Override
    public boolean existUserWithIdAndPwd(String uid, String password) {
        return Objects.equals(users.get(uid), password);
    }
}
