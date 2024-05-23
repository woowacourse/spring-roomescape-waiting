package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long>, JpaSpecificationExecutor<MemberReservation> {

    List<MemberReservation> findAllByMember(Member member);

    List<MemberReservation> findAllByStatus(ReservationStatus status);

    Optional<MemberReservation> findFirstByReservationOrderByCreatedTime(Reservation reservation);

    @Query("""
    SELECT count(*)
    FROM MemberReservation  mr
    WHERE mr.reservation = 
    (SELECT mr.reservation
    FROM MemberReservation mr
    WHERE mr.id = :memberReservationId)
    AND mr.id < :memberReservationId
    """)
    int countWaitingMemberReservation(Long memberReservationId);

    boolean existsByReservation(Reservation reservation);

    void deleteByReservation_Id(long reservationId);

    boolean existsByReservationAndMember(Reservation reservation, Member member);
}
