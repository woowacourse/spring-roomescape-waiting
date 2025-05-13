package roomescape.reservation.business.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.business.domain.Reservation;

public interface ReservationDao {

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    int deleteById(Long id);

    Optional<Reservation> findById(Long id);

    boolean existByTimeId(Long timeId);

    boolean existByThemeId(Long themeId);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findReservationByThemeIdAndMemberIdInDuration(long themeId, long memberId, LocalDate start,
                                                                    LocalDate end);
}
