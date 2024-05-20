package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;

import java.util.List;
import java.util.Optional;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long> {

    Optional<MemberReservation> findByMemberAndReservation(Member member, Reservation reservation);

    List<MemberReservation> findByMemberId(Long memberId);

    @Query("select mr from MemberReservation mr where mr.member.id = :memberId AND mr.reservation in :reservations")
    List<MemberReservation> findByMemberIdAndReservations(@Param("memberId") Long memberId, @Param("reservations") List<Reservation> reservations);
}
