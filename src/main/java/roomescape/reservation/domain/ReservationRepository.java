package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberIdAndThemeIdAndDateValueBetween(Long memberId,
                                                                  Long themeId,
                                                                  LocalDate dateFrom,
                                                                  LocalDate dateTo);

    List<Reservation> findByDateValueAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllByStatus(Status status);

    Optional<Reservation> findFirstByDateValueAndTimeIdAndThemeIdAndStatus(LocalDate date,
                                                                          Long timeId,
                                                                          Long themeId,
                                                                          Status status);

    @Query("""
            SELECT count (r.id) FROM Reservation AS r
            WHERE r.date.value = :date
                AND r.time.id = :timeId
                AND r.theme.id = :themeId
                AND r.id < :reservationId
            """)
    int countWaitingRank(LocalDate date, Long timeId, Long themeId, Long reservationId);

    boolean existsByDateValueAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateValueAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
