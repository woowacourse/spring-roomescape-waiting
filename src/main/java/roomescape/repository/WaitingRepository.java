package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.model.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndThemeIdAndReservationTimeIdAndMemberId(
            LocalDate date,
            Long themeId,
            Long reservationTimeId,
            Long memberId);

    List<Waiting> findAllByMemberId(Long id);
}
