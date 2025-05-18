package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query(value = """
            SELECT r
            FROM Reservation r
            JOIN ReservationTime rt ON r.time.id = rt.id
            JOIN Theme t ON r.theme.id = t.id
            JOIN Member m ON r.member.id = m.id
            WHERE r.theme.id = :themeId AND r.member.id = :memberId AND r.date >= :dateFrom AND r.date <= :dateTo
            ORDER BY r.date, rt.startAt
            """)
    List<Reservation> findAllFiltered(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findAllByMember(Member member);

    boolean existsByTimeId(Long timeId);

    boolean existsByDateAndTimeId(LocalDate date, Long timeId);
}
