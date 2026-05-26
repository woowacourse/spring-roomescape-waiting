package roomescape.domain.userreservation;

import java.util.List;
import java.util.Optional;

public interface UserReservationRepository {

    UserReservation save(UserReservation userReservation);

    List<UserReservation> findAll();

    Optional<UserReservation> findById(Long id);

    List<UserReservation> findByUserId(Long userId);

    Long countByReservationId(Long reservationId);

    boolean existsActiveByUserIdAndReservationId(Long userId, Long reservationId);
}
