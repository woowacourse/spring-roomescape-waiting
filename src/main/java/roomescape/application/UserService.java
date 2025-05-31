package roomescape.application;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.exception.AlreadyExistedException;
import roomescape.exception.NotFoundException;
import roomescape.presentation.response.UserReservedRecordsResponse;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public UserService(final UserRepository userRepository,
                       final ReservationRepository reservationRepository,
                       final WaitingRepository waitingRepository
    ) {
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public User saveUser(final String email, final String password, final String name) {
        validateEmailNotRegistered(email);
        User user = User.register(name, email, password);

        return userRepository.save(user);
    }

    private void validateEmailNotRegistered(String email) {
        boolean isEmailAlreadyRegistered = userRepository.existsByEmail(email);

        if (isEmailAlreadyRegistered) {
            throw new AlreadyExistedException("이미 해당 이메일로 가입된 사용자가 있습니다.");
        }
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<UserReservedRecordsResponse> findTotalRecordByUserId(Long userId) {
        validateUserExists(userId);
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        List<WaitingWithRank> waitings = waitingRepository.findWaitingWithRankByUserId(userId);

        List<UserReservedRecordsResponse> reservedResponses =
                UserReservedRecordsResponse.fromReservations(reservations);
        List<UserReservedRecordsResponse> waitingResponses =
                UserReservedRecordsResponse.fromWaitingsWithRank(waitings);

        List<UserReservedRecordsResponse> userReservedRecordsResponses = new ArrayList<>();
        userReservedRecordsResponses.addAll(reservedResponses);
        userReservedRecordsResponses.addAll(waitingResponses);
        return userReservedRecordsResponses;
    }

    private void validateUserExists(long userId) {
        boolean isUserExists = userRepository.existsById(userId);

        if (!isUserExists) {
            throw new NotFoundException("존재하지 않는 사용자입니다.");
        }
    }
}
