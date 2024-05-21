package roomescape.core.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.core.domain.Member;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByDateAndTheme(final LocalDate date, final Theme theme);

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.theme t
            JOIN FETCH r.member m
            WHERE t.id = :themeId
            AND m.id = :memberId
            AND r.date >= :from
            AND r.date <= :to
            """)
    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(@Param("themeId") final Long themeId,
                                                                @Param("memberId") final Long memberId,
                                                                @Param("from") final LocalDate from,
                                                                @Param("to") final LocalDate to);

    List<Reservation> findAllByMember(final Member member);

    List<Reservation> findAllByDateAndTimeAndThemeOrderByCreateAtAsc(final LocalDate date, final ReservationTime time,
                                                                     final Theme theme);

    Integer countByTime(final ReservationTime reservationTime);

    Integer countByTheme(final Theme theme);

    Integer countByDateAndTimeAndTheme(final LocalDate date, final ReservationTime time, final Theme theme);

    Integer countByMemberAndDateAndTimeAndTheme(final Member member, final LocalDate date, final ReservationTime time,
                                                final Theme theme);

    void deleteById(final long id);
}
