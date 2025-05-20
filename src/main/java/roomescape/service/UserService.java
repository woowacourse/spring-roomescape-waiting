package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.dto.business.UserCreationContent;
import roomescape.dto.response.UserProfileResponse;
import roomescape.exception.local.DuplicatedEmailException;
import roomescape.exception.local.NotFoundUserException;
import roomescape.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Member getUserById(long id) {
        return loadUserById(id);
    }

    public List<UserProfileResponse> findAllUserProfile() {
        List<Member> members = userRepository.findAll();
        return members.stream()
                .map(UserProfileResponse::new)
                .toList();
    }

    public UserProfileResponse addUser(UserCreationContent request) {
        validateDuplicatedEmail(request.email());
        Member member = Member.createWithoutId(request.role(), request.name(), request.email(), request.password());
        Member savedMember = userRepository.save(member);
        return new UserProfileResponse(savedMember);
    }

    private Member loadUserById(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(NotFoundUserException::new);
    }

    private void validateDuplicatedEmail(String email) {
        boolean isDuplicatedEmail = userRepository.existsByEmail(email);
        if (isDuplicatedEmail) {
            throw new DuplicatedEmailException();
        }
    }
}
