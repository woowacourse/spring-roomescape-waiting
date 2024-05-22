package roomescape.reservation.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.model.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {
    boolean existsByDateAndReservationTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateAndReservationTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
