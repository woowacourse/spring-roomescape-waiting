package roomescape.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Waiting;

import java.util.List;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    boolean existsByMemberAndReservation(Member member, Reservation reservation);

    @Query("select exists(select 1 from Waiting w where w=:waiting and w.member = :member)")
    boolean existsByWaitingAndMember(Waiting waiting, Member member);

    List<Waiting> findByMember(Member user);

    List<Waiting> findByReservationOrderById(Reservation reservation);
}
