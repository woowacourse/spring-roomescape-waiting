package roomescape.reservation.waiting.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.common.exception.BusinessException;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.waiting.domain.WaitingReservation;
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

    public List<WaitingReservation> findAll() {
        return waitingReservationRepository.findAll();
    }

    public WaitingReservation findById(final Long waitingId) {
        return waitingReservationRepository.findById(waitingId)
            .orElseThrow(() -> new BusinessException("대기된 예약을 찾을 수 없습니다."));
    }
}
