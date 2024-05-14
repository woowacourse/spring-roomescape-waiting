package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long>, MemberReservationRepositoryCustom {

    Optional<MemberReservation> findById(long id);

    List<MemberReservation> findAllByMember(Member member);

    void deleteById(long memberReservationId);

    void deleteByReservation_Id(long reservationId);

    boolean existsByReservation_DateAndReservationTimeAndReservationTheme(LocalDate date, ReservationTime time, Theme theme);
}
