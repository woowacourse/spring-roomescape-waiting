package roomescape.reservation.waiting.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.waiting.domain.dto.WaitingReservationWithRank;

public interface WaitingReservationRepository {

    WaitingReservation save(WaitingReservation waitingReservation);

    Optional<WaitingReservation> findById(Long id);

    List<WaitingReservationWithRank> findWaitingsWithRankByMember_Id(Long memberId);

    List<WaitingReservation> findAll();

    void deleteById(Long id);

    void deleteByIdAndMemberId(Long id, Long memberId);

    boolean existsById(Long id);

    boolean existsByIdAndMemberId(Long id, Long memberId);

    boolean existsByThemeIdAndTimeIdAndDateAndMemberId(Long themeId, Long timeId, LocalDate date, Long memberId);
}
