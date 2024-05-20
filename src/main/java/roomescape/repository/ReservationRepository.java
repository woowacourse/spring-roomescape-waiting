package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Reservation;
import roomescape.service.exception.ReservationNotFoundException;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(long timeId);

    boolean existsByThemeIdAndTimeIdAndDate(long themeId, long timeId, LocalDate date);

    boolean existsByThemeId(long themeId);

    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(long themeId, long memberId,
                                                                LocalDate from, LocalDate until);

    @EntityGraph(attributePaths = {"theme", "time"})
    List<Reservation> findAllByMemberId(Long id);

    @EntityGraph(attributePaths = "theme")
    List<Reservation> findAllByDateBetween(LocalDate from, LocalDate until);

    @EntityGraph(attributePaths = "time")
    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

    default Reservation findByIdOrThrow(long id) {
        return findById(id).orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
    }
}
