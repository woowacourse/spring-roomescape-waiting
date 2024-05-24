package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Reservation;
import roomescape.domain.Status;
import roomescape.service.exception.ReservationNotFoundException;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsByThemeIdAndTimeIdAndDateAndStatus(long themeId, long timeId,
                                                     LocalDate date, Status status);

    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(
            long memberId, long themeId, long timeId, LocalDate date);

    List<Reservation> findAllByThemeIdAndMemberIdAndDateBetween(long themeId, long memberId,
                                                                LocalDate from, LocalDate until);

    @EntityGraph(attributePaths = {"theme", "time", "member"})
    List<Reservation> findAllByStatus(Status status);

    @EntityGraph(attributePaths = {"theme", "time"})
    List<Reservation> findAllByMemberId(long id);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.theme
            WHERE r.status = :status
            AND r.date BETWEEN :from AND :until
            """)
    List<Reservation> findAllJoinThemeByStatusAndDateBetween(Status status, LocalDate from,
                                                             LocalDate until);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.time
            WHERE r.status = :status
            AND r.date = :date
            AND r.theme.id = :themeId
            """)
    List<Reservation> findAllJoinTimeByStatusAndDateAndThemeId(Status status, LocalDate date,
                                                               long themeId);

    Optional<Reservation> findFirstByTimeIdAndThemeIdAndDateAndStatus(Long timeId, Long themeId,
                                                                      LocalDate date,
                                                                      Status status);

    List<Reservation> findAllByTimeIdAndThemeIdAndDateAndStatus(Long timeId, Long themeId,
                                                                LocalDate date, Status status);

    default Reservation findByIdOrThrow(long id) {
        return findById(id).orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
    }
}
