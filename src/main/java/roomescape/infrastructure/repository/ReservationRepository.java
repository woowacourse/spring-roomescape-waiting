package roomescape.infrastructure.repository;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationDateTime;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    List<Reservation> findAll();

    Reservation save(Member member, ReservationDateTime reservationDateTime, Theme theme);

    void deleteById(Long id);

    Optional<Reservation> findById(Long id);

    boolean existsByDateTimeAndTheme(ReservationDate reservationDate, Long timeId, Long themeId);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);
}
