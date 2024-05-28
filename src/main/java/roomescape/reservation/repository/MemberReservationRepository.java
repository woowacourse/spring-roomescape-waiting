package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.entity.MemberReservation;
import roomescape.reservation.domain.entity.Reservation;
import roomescape.reservation.domain.entity.ReservationStatus;
import roomescape.reservation.domain.dto.WaitingReservationRanking;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long> {

    Optional<MemberReservation> findByReservationAndMember(Reservation reservation, Member member);

    Optional<MemberReservation> findFirstByReservationAndStatus(Reservation reservation, ReservationStatus status);

    @Query("select mr from MemberReservation as mr where mr.reservation = :reservation and mr.status = 'CONFIRMATION'")
    Optional<MemberReservation> findByReservationAndStatusIsConfirmation(Reservation reservation);

    List<MemberReservation> findByStatus(ReservationStatus status);

    List<MemberReservation> findByMemberIdAndStatus(Long memberId, ReservationStatus status);

    List<MemberReservation> findByMemberIdAndReservationIn(Long memberId, List<Reservation> reservations);

    List<MemberReservation> findByReservationDateBetween(LocalDate start, LocalDate end);

    @Query("select mr as memberReservation, " +
            "(select count(*) from MemberReservation as cmr " +
            "where cmr.reservation.id = mr.reservation.id and cmr.status = 'WAITING' and cmr.createdAt < mr.createdAt) as rank " +
            "from MemberReservation mr " +
            "where mr.status = 'WAITING' and mr.member.id = :memberId"
    )
    List<WaitingReservationRanking> findWaitingReservationRankingByMemberId(@Param("memberId") Long memberId);

    boolean existsByReservationThemeId(Long id);

    boolean existsByReservationTimeId(Long id);

    Long countByReservationAndCreatedAtBefore(Reservation reservation, LocalDateTime createdAt);
}
