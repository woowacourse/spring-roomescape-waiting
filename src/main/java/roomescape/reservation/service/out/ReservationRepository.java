package roomescape.reservation.service.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository {

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(Long id);

    void deleteById(Long id);

    boolean existsSameDateTime(LocalDate date, Long timeId);

    boolean existReservationByTimeId(Long timeId);

    boolean existReservationByThemeId(Long themeId);

    int countReservationByThemeIdAndDuration(LocalDate from, LocalDate to, Long themeId);

    List<Long> findReservedTimeIdsByDateAndTheme(LocalDate date, Long themeId);

    List<Reservation> findFilteredReservations(Long themeId, Long memberId, LocalDate from, LocalDate to);
}
