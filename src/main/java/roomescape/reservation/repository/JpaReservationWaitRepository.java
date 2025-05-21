package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;

@Repository
public interface JpaReservationWaitRepository extends JpaRepository<ReservationWait, Long>, ReservationWaitRepository {

    @Override
    boolean existsByInfoDateAndInfoTimeIdAndInfoThemeIdAndInfoMemberId(ReservationDate date, Long timeId, Long themeId, Long memberId);
}
