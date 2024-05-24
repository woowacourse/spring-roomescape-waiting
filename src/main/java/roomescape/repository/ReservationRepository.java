package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.repository.dto.ReservationWithRank;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Boolean existsByTimeId(Long id);

    Boolean existsByThemeId(Long id);

    Boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Boolean existsByMemberIdAndDateAndTimeIdAndThemeIdAndStatus(
        Long memberId, LocalDate date, Long timeId, Long themeId, ReservationStatus status);

    Optional<Reservation> findByIdAndStatus(Long id, ReservationStatus status);

    List<Reservation> findAllByDateAndThemeIdOrderByTimeStartAtAsc(LocalDate date, Long themeId);

    List<Reservation> findAllByThemeIdAndMemberIdAndDateIsBetweenOrderByDateAscTimeStartAtAsc(
        Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findAllByStatusOrderByDateAscTimeStartAtAsc(ReservationStatus status);

    Optional<Reservation> findFirstByDateAndTimeIdAndThemeIdOrderByCreatedAtAsc(LocalDate date, Long timeId, Long themeId);

    @Query("""
        SELECT new roomescape.repository.dto.ReservationWithRank(
            r, (
                SELECT COUNT(r2)
                FROM Reservation r2
                WHERE r2.theme = r.theme
                AND r2.date = r.date
                AND r2.time = r.time
                AND r2.createdAt < r.createdAt
            )
        )
        FROM Reservation r
        WHERE r.member.id = :memberId
        ORDER BY
            r.date ASC,
            r.time.startAt ASC
        """)
    List<ReservationWithRank> findReservationsWithRankByMemberId(Long memberId);
}
