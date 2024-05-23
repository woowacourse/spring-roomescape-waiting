package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Waiting;

@Repository
public interface WaitingRepository extends CrudRepository<Waiting, Long> {

    List<Waiting> findAllByMemberId(Long memberId);

    boolean existsByThemeIdAndDateAndReservationTimeStartAt(Long themeId, LocalDate date, LocalTime startAt);
}
