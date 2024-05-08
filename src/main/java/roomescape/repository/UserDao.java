package roomescape.repository;

import java.util.List;
import java.util.Optional;

import roomescape.model.User;

public interface UserDao {
    Optional<User> findUserByEmailAndPassword(String email, String password);

    Optional<String> findUserNameByUserId(Long userId);

    Optional<User> findUserById(Long userId);

    List<User> findAllUsers();
}
