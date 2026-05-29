package roomescape.reservationwaiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    void deleteById(Long id);

    List<ReservationWaiting> findByMemberId(Long memberId);

    Optional<ReservationWaiting> findById(Long id);

    boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId);

    Long calculateTurn(Long waitingId, LocalDate date, Long timeId, Long themeId);
}