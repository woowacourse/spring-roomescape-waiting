package roomescape.user.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.domain.DomainTerm;
import roomescape.common.domain.Email;
import roomescape.common.exception.NotFoundException;
import roomescape.user.domain.User;
import roomescape.user.domain.UserId;
import roomescape.user.domain.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    public User getByEmail(final Email email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(DomainTerm.USER, email));
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public List<User> getAllByIds(final List<UserId> ids) {
        return userRepository.findAllByIds(ids);
    }

    public User getById(final UserId id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(DomainTerm.USER, id));
    }
}
