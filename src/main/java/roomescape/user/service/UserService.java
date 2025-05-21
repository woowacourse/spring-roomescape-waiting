package roomescape.user.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.dto.ReservationWithBookStateDto;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.user.domain.User;
import roomescape.user.domain.dto.UserRequestDto;
import roomescape.user.domain.dto.UserResponseDto;
import roomescape.user.exception.NotFoundUserException;
import roomescape.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
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

    @Transactional
    public User add(UserRequestDto userRequestDto) {
        User user = userRequestDto.toEntity();
        return userRepository.save(user);
    }

    public List<ReservationWithStateDto> findAllReservationByMember(User member) {
        validateExistsUser(member.getId());
        List<Reservation> reservations = reservationRepository.findByUser(member);
        return reservations.stream()
                .map(ReservationWithStateDto::new)
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
