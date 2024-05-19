package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMember_Id(final Long memberId);

    List<Reservation> findByDateAndThemeId(final LocalDate date, final Long themeId);

    List<Reservation> findByTheme_IdAndMember_IdAndDateBetween(final Long themeId, final Long memberId, final LocalDate dateFrom, final LocalDate dateTo);

    int countByTime_Id(final Long timeId);

    int countByDateAndTime_IdAndTheme_Id(final LocalDate date, final Long timeId, final Long themeId);

    boolean existsById(final Long id);
}
