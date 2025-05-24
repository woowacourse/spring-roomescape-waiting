package roomescape.reservation.waiting.application.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.waiting.domain.WaitingReservationRepository;
import roomescape.reservation.waiting.domain.dto.WaitingReservationWithRank;

@Service
public class WaitingReservationQueryService {

    private final WaitingReservationRepository waitingReservationRepository;

    public WaitingReservationQueryService(WaitingReservationRepository waitingReservationRepository) {
        this.waitingReservationRepository = waitingReservationRepository;
    }

    public List<WaitingReservationWithRank> findWaitingsWithRankByMemberId(Long memberId) {
        return waitingReservationRepository.findWaitingsWithRankByMember_Id(memberId);
    }

    public boolean isExistsWaitingReservation(final Long themeId, final Long timeId, final LocalDate date, final Long memberId) {
        return waitingReservationRepository.existsByThemeIdAndTimeIdAndDateAndMemberId(themeId, timeId, date, memberId);
    }
}
