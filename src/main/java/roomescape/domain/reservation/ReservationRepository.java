package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ReservationRepository {

    List<Reservation> findAll();

    Long create(Reservation reservation);

    void deleteById(Long reservationId);

    Optional<Reservation> findById(Long reservationId);

    boolean existByTimeId(Long reservationTimeId);

    boolean existByDateAndTimeId(LocalDate reservationDate, Long timeId);

    boolean existByThemeId(Long themeId);

    List<Reservation> findByThemeIdAndReservationDate(Long themeId, LocalDate reservationDate);

    List<Reservation> findByThemeIdAndMemberIdBetweenDate(Long themeId, Long memberId, LocalDate from, LocalDate to);
}
