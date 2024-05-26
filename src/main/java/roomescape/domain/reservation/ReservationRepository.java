package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAll(Specification<Reservation> specification);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsActiveReservation(long themeId, LocalDate date, long timeId);

    Reservation getById(long id);

    Optional<Reservation> findFirstWaiting(Theme theme, LocalDate date, ReservationTime time);

    long getWaitingCount(Reservation reservation);

    List<Reservation> findActiveReservationByMemberId(long memberId);

    boolean existsAlreadyWaitingOrBooked(long memberId, long themeId, LocalDate date, long timeId);

    List<Reservation> findAllBookedReservations();

    List<Reservation> findAllWaitingReservations();
}
