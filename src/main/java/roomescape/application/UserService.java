package roomescape.application;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.user.Email;
import roomescape.domain.user.Password;
import roomescape.domain.user.User;
import roomescape.domain.user.UserName;
import roomescape.domain.user.UserRepository;
import roomescape.exception.AlreadyExistedException;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository repository;

    public User register(final String email, final String password, final String name) {
        if (existsAlready(email)) {
            throw new AlreadyExistedException("이미 해당 이메일로 가입된 사용자가 있습니다.");
        }

        var user = new User(new UserName(name), new Email(email), new Password(password));
        return repository.save(user);
    }

    private boolean existsAlready(final String email) {
        return repository.findByEmail(new Email(email)).isPresent();
    }

    @Transactional(readOnly = true)
    public List<Reservation> getReservations(final long id) {
        var user = repository.getById(id);
        return user.reservations();
    }

    public List<User> findAllUsers() {
        return repository.findAll();
    }

    public User getById(final long id) {
        return repository.getById(id);
    }
}
