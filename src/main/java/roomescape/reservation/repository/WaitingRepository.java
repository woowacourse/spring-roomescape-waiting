package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.theme.domain.Theme;

public interface WaitingRepository extends CrudRepository<Waiting, Long> {

    @Query("""
                SELECT w
                FROM Waiting w
                JOIN FETCH w.time
                JOIN FETCH w.theme
                WHERE w.member = :member
            """)
    List<Waiting> findByMember(final Member member);

    boolean existsByDateAndTimeAndTheme(final LocalDate date, final ReservationTime reservationTime, final Theme theme);

    @Query("""
            SELECT COUNT(w)
            FROM Waiting w
            WHERE w.theme = :theme
              AND w.date = :date
              AND w.time = :time
              AND w.id < :id
            """)
    long countBefore(final Theme theme, final LocalDate date, final ReservationTime time, final Long id);
}
