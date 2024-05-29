package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.dto.AvailableTimeDto;
import roomescape.domain.theme.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    long countByTimeId(long timeId);

    @Query("""
            SELECT COUNT(r)
            FROM Reservation r
            WHERE r.theme.id = :themeId
            AND r.date = :date
            AND r.time.id = :timeId
            AND r.id < :id
            """)
    long countByOrder(Long id, LocalDate date, long timeId, long themeId);

    @EntityGraph(attributePaths = {"member", "time", "theme"})
    List<Reservation> findAllByStatus(Status status);

    Optional<Reservation> findFirstByDateAndTimeIdAndThemeIdAndStatus(LocalDate date, long timeId, long themeId, Status status);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByDateAndTimeIdAndThemeIdAndStatus(LocalDate date, long timeId, long themeId, Status status);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, long timeId, long themeId, long memberId);

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Reservation> findAllByMemberIdOrderByDateAsc(Long memberId);

    @Query("""
            select new roomescape.domain.dto.AvailableTimeDto(rt.id, rt.startAt,
            (select count(r) > 0 from Reservation r where r.time.id = rt.id and r.date = :date and r.theme.id = :themeId))
            from ReservationTime rt
            """)
    List<AvailableTimeDto> findAvailableReservationTimes(LocalDate date, long themeId);

    @Query("""
            select t from Theme t
            join Reservation r on r.theme.id = t.id
            and r.date between :startDate and :endDate
            group by t.id, t.name, t.description, t.thumbnail
            order by count(r.id) desc
            """)
    List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("""
            select r from Reservation r
            where r.theme.id = :themeId and
            r.member.id = :memberId and
            :dateFrom <= r.date and
            r.date <= :dateTo and
            r.status = roomescape.domain.reservation.Status.RESERVATION
            """)
    List<Reservation> findByCriteria(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);
}
