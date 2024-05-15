package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long>, MemberReservationRepositoryCustom {

    List<MemberReservation> findAllByMember(Member member);

    void deleteByReservation_Id(long reservationId);

    @Query("""
            SELECT COUNT(mr) > 0 
            FROM MemberReservation mr 
            JOIN mr.reservation r
            JOIN r.time rt
            JOIN r.theme th
            WHERE r.date = :date AND rt = :time AND th = :theme
            """)
    boolean existsBy(LocalDate date, ReservationTime time, Theme theme);
}
