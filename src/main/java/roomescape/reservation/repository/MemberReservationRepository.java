package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.WaitingReservationRanking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long> {

    Optional<MemberReservation> findByReservationAndStatus(Reservation reservation, ReservationStatus status);

    List<MemberReservation> findByMemberIdAndStatus(Long memberId, ReservationStatus status);
    List<MemberReservation> findByStatus(ReservationStatus status);

    @Query("select mr from MemberReservation mr where mr.member.id = :memberId AND mr.reservation in :reservations")
    List<MemberReservation> findByMemberIdAndReservations(@Param("memberId") Long memberId, @Param("reservations") List<Reservation> reservations);

    List<MemberReservation> findByReservationDateBetween(LocalDate start, LocalDate end);

    @Query("select mr as memberReservation, " +
            "(select count(*) from MemberReservation as cmr " +
            "where cmr.reservation.id = mr.reservation.id and cmr.status = 'WAITING' and cmr.createdAt < mr.createdAt) as rank " +
            "from MemberReservation mr " +
            "where mr.status = 'WAITING' and mr.member.id = :memberId"
    )
    List<WaitingReservationRanking> findWaitingReservationRankingByMemberId(@Param("memberId") Long memberId);

    Optional<MemberReservation> findByReservationAndMember(Reservation reservation, Member member);

    Long countByReservationAndCreatedAtBefore(Reservation reservation, LocalDateTime createdAt);

    boolean existsByReservationThemeId(Long id);

    boolean existsByReservationTimeId(Long id);
}
