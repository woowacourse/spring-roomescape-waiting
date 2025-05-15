package roomescape.reservation.dao.reservation;

import java.time.LocalDate;
import java.util.List;

import roomescape.reservation.model.Reservation;

public interface ReservationDao {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate);

    List<Reservation> findByDateBetween(LocalDate startDate, LocalDate endDate);

    int deleteById(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
