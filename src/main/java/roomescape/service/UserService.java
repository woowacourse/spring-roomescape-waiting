package roomescape.service;

import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.controller.request.UserLoginRequest;
import roomescape.exception.AuthenticationException;
import roomescape.exception.NotFoundException;
import roomescape.model.User;
import roomescape.repository.UserDao;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User findUserByEmailAndPassword(UserLoginRequest request) {
        return userDao.findUserByEmailAndPassword(request.email(), request.password())
                .orElseThrow(() -> new AuthenticationException(
                        "사용자(email: %s, password: %s)가 존재하지 않습니다.".formatted(request.email(), request.password())));
    }

    public String findUserNameById(Long id) {
        return userDao.findUserNameByUserId(id)
                .orElseThrow(() -> new NotFoundException("id가 %s인 사용자가 존재하지 않습니다."));
    }

    public User findUserById(Long id) {
        return userDao.findUserById(id)
                .orElseThrow(() -> new NotFoundException("id가 %s인 사용자가 존재하지 않습니다."));
    }

    public List<User> findAllUsers() {
        return userDao.findAllUsers();
    }
}
