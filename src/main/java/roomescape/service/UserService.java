package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.User;
import roomescape.dto.business.ReservationWithBookStateDto;
import roomescape.dto.request.UserRequestDto;
import roomescape.dto.response.UserResponseDto;
import roomescape.exception.local.NotFoundUserException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public UserService(UserRepository userRepository, ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<UserResponseDto> findAll() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponseDto::of)
                .toList();
    }

    public User add(UserRequestDto userRequestDto) {
        User user = userRequestDto.toEntity();
        return userRepository.save(user);
    }

    public List<ReservationWithBookStateDto> findAllReservationByMember(User member) {
        validateExistsUser(member.getId());
        List<Reservation> reservations = reservationRepository.findByUser(member);
        return reservations.stream()
                .map(ReservationWithBookStateDto::new)
                .toList();
    }

    private void validateExistsUser(Long id) {
        findByIdOrThrow(id);
    }

    public User findByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(NotFoundUserException::new);
    }
}
