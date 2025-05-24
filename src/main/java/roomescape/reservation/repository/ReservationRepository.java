package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository {

    List<Reservation> findAll();
    Reservation save(Reservation reservation);
    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
    boolean existsByTimeId(long timeId);
    boolean existsByThemeId(Long themeId);
    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);
    List<Reservation> findAllByMemberId(long id);
    List<Reservation> findAllByCondition(Long memberId, Long themeId, LocalDate from, LocalDate to);
    boolean existsByDateAndThemeIdAndTimeIdAndMemberId(LocalDate date, long themeId, long timeId, long memberId);
    void delete(Reservation reservation);
    Optional<Reservation> findById(long reservationId);
}
