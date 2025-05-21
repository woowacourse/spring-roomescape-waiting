package roomescape.reservation.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationWithStatus;

public interface ReservationWithStatusRepository extends JpaRepository<ReservationWithStatus, Long> {
    @Query("SELECT rws FROM ReservationWithStatus rws JOIN FETCH rws.reservation r WHERE r.member = :member")
    List<ReservationWithStatus> findByReservation_Member(Member member);
}
