package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberId(final Long memberId);

    List<Reservation> findByDateAndThemeId(final LocalDate date, final Long themeId);

    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(final Long themeId, final Long memberId, final LocalDate dateFrom, final LocalDate dateTo);

    List<Reservation> findByStatus(final ReservationStatus status);

    int countByTimeId(final Long timeId);

    int countByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId);

    boolean existsById(final Long id);
}
