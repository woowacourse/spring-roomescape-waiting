package roomescape.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.model.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    Waiting findById(Long id);

    void delete(Waiting waiting);

    List<Waiting> findForMember(Long id);

    int countWaitingBefore(Waiting waiting);

    List<Waiting> findAll();

    void rejectById(Long id);

    Optional<Waiting> findNextWaiting(LocalDate date, ReservationTime reservationTime, Theme theme);
}
