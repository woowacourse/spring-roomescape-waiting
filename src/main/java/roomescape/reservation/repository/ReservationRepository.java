package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByMemberIdAndThemeIdAndDateRange(Long memberId, Long themeId, LocalDate from, LocalDate to);

    Optional<Reservation> findById(Long id);

    Optional<Reservation> findByTimeId(Long id);

    Optional<Reservation> findByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    void deleteById(Long id);
}
