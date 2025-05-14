package roomescape.application;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.User;
import roomescape.domain.repository.UserRepository;
import roomescape.exception.custom.AlreadyExistedException;
import roomescape.exception.custom.NotFoundException;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(final UserRepository repository) {
        this.repository = repository;
    }

    public User register(final String email, final String password, final String name) {
        var optionalUser = repository.findByEmail(email);
        if (optionalUser.isPresent()) {
            throw new AlreadyExistedException("이미 해당 이메일로 가입된 사용자가 있습니다.");
        }

        var user = User.createUser(name, email, password);
        return repository.save(user);
    }

    public User getById(final long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다. id : " + id));
    }

    public List<Reservation> getReservations(long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다. id : " + id));
        return user.reservations();
    }

    public List<User> findAllUsers() {
        return repository.findAll();
    }
}
