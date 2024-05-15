package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByThemeId(long themeId);

    List<Reservation> findByMemberIdAndThemeIdAndDateBetween(long memberId, long themeId, LocalDate dateFrom,
                                                             LocalDate dateTo);

    List<Reservation> findByMemberId(long memberId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(long timeId);
}
