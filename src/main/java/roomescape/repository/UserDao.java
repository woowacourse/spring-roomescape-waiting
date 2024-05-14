package roomescape.repository;

import java.util.List;
import java.util.Optional;

import roomescape.model.Member;

public interface UserDao {
    Optional<Member> findUserByEmailAndPassword(String email, String password);

    Optional<String> findUserNameByUserId(Long userId);

    Optional<Member> findUserById(Long userId);

    List<Member> findAllUsers();
}
