package roomescape.repository;

public interface UserRepository {

    boolean existUserWithIdAndPwd(String uid, String password);
}
