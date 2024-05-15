package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long> {

    @Query("""
                SELECT mr
                FROM MemberReservation mr
                JOIN mr.reservation r
                JOIN mr.member m
                JOIN r.time t
                JOIN r.theme th
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
