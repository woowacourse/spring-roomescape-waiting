package roomescape.domain.userreservation;

import java.util.List;
import java.util.Optional;

public interface UserReservationRepository {

    UserReservation save(UserReservation userReservation);

    List<UserReservation> findAll();

    Optional<UserReservation> findById(Long id);

    List<UserReservation> findByUserId(Long userId);

    Long countByReservationId(Long reservationId);

    List<UserReservation> findAllByReservationIdOrder(Long reservationId);

    Optional<UserReservation> update(Long id, UserReservation userReservation);

    void updateStatus(Long id, WaitingStatus status);

    boolean existsActiveByUserIdAndReservationId(Long userId, Long reservationId);

    void updateWaitingNumbers(List<UserReservation> userReservations);
}
