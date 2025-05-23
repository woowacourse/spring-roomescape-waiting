package roomescape.reservation.model.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.dto.ReservationWaitingWithRank;

public interface ReservationWaitingRepository {

    void save(ReservationWaiting reservationWaiting);

    List<ReservationWaitingWithRank> findAllWithRankByMemberId(Long memberId);

    ReservationWaiting getById(Long reservationWaitingId);

    void remove(ReservationWaiting reservationWaiting);

    Optional<ReservationWaiting> findFirstByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<ReservationWaiting> getAll();

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
