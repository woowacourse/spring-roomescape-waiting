package roomescape.reservation.repository;

import java.time.LocalDate;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.theme.domain.Theme;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {

    boolean existsByDateAndReservationTimeAndThemeAndMember(
            LocalDate date,
            ReservationTime reservationTime,
            Theme theme,
            Member member
    );
}
