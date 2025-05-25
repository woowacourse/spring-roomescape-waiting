package roomescape.reservation.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.Waiting;

public interface WaitingRepository extends Repository<Waiting, Long>, WaitingRepositoryCustom {

    Waiting save(Waiting waiting);

    Optional<Waiting> findById(Long id);

    boolean existsByReservationTimeAndMemberAndTheme(ReservationTime time, Member member, Theme theme);

    void delete(Waiting waiting);

    Optional<Waiting> findFirstByReservationTimeAndThemeOrderById(ReservationTime time, Theme theme);

    @Query("""
            SELECT w
            FROM Waiting w
            JOIN FETCH w.member
            JOIN FETCH w.theme
            JOIN FETCH w.reservationTime.timeSlot
            """)
    List<Waiting> findAll();
}
