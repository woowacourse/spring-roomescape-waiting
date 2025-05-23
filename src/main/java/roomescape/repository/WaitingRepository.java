package roomescape.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.model.Member;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.model.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndThemeAndReservationTimeAndMember(LocalDate date,
                                                            Theme theme,
                                                            ReservationTime reservationTime,
                                                            Member member);
}
