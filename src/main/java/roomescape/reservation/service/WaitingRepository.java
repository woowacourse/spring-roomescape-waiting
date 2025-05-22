package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    boolean existsBySameReservation(
            Long memberId,
            Long themeId,
            Long reservationTimeId,
            LocalDate date
    );

    void deleteById(Long id);

    Optional<Waiting> findById(Long id);

    List<Waiting> findAllByMemberId(Long id);

    List<Waiting> findAll();
}
