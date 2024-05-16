package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long> {

    @Query("""
                SELECT mr
                FROM MemberReservation mr
                JOIN FETCH mr.reservation r
                JOIN FETCH mr.member m
                JOIN FETCH r.time t
                JOIN FETCH r.theme th
                WHERE (:memberId IS NULL OR m.id = :memberId) 
                    AND (:themeId IS NULL OR th.id = :themeId) 
                    AND :startDate <= r.date 
                    AND r.date <= :endDate
            """)
    List<MemberReservation> findBy(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate);

    List<MemberReservation> findAllByMember(Member member);

    void deleteByReservation_Id(long reservationId);

    boolean existsByReservationAndMember(Reservation reservation, Member member);
}
