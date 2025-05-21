package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    default Reservation getById(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다. id: " + id));
    }

    @EntityGraph(attributePaths = {"theme", "member", "time"})
    @Query("""
            select r from Reservation r
            where (:themeId is null or r.theme.id = :themeId)
              and (:memberId is null or r.member.id = :memberId)
              and (:localDateFrom is null or r.date >= :localDateFrom)
              and (:localDateTo is null or r.date <= :localDateTo)
            """)
    List<Reservation> findByCriteria(
            @Param("themeId") Long themeId,
            @Param("memberId") Long memberId,
            @Param("localDateFrom") LocalDate localDateFrom,
            @Param("localDateTo") LocalDate localDateTo
    );

    @Query("""
            select w.rank from Reservation r
            join r.waiting w
            where r.theme = :theme
            and r.date = :date
            and r.time = :reservationTime
            order by w.rank desc
            limit 1
            """)
    Optional<Long> getLastWaitingRank(
            @Param("theme") Theme theme,
            @Param("date") LocalDate date,
            @Param("reservationTime") ReservationTime reservationTime
    );

    boolean existsByDateAndTimeAndTheme(final LocalDate date, final ReservationTime time, final Theme theme);

    boolean existsByThemeId(final Long themeId);

    boolean existsByTimeId(final Long timeId);

    List<Reservation> findAllByMember(Member member);

    @Query("""
            select r.waiting
            from Reservation r
            where r.date = :date
              and r.time = :time
              and r.theme = :theme
            """)
    List<Waiting> findAllWaiting(
            @Param("date") LocalDate date,
            @Param("time") ReservationTime time,
            @Param("theme") Theme theme
    );
}
