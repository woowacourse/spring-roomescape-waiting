package roomescape.reservationwaiting.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.domain.ReservationWaitingFactory;
import roomescape.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.reservationwaiting.dto.ReservationWaitingResponse;
import roomescape.reservationwaiting.dto.ReservationWaitingTurnResponse;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationWaitingFactory reservationWaitingFactory;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationWaitingFactory reservationWaitingFactory,
                                     ReservationTimeService reservationTimeService,
                                     ThemeService themeService) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationWaitingFactory = reservationWaitingFactory;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @Transactional
    public ReservationWaitingResponse createWaiting(Member member, ReservationWaitingRequest request) {
        ReservationTime time = reservationTimeService.getById(request.timeId());
        Theme theme = themeService.getById(request.themeId());

        if (reservationWaitingRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(
                member.getId(), request.date(), request.timeId(), request.themeId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_WAITING);
        }

        ReservationWaiting saved = reservationWaitingRepository.save(
                reservationWaitingFactory.create(member, request.date(), time, theme));
        return ReservationWaitingResponse.from(saved);
    }

    @Transactional
    public void deleteWaiting(Long id, Long memberId) {
        ReservationWaiting waiting = reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        if (!waiting.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (waiting.isPast()) {
            throw new BusinessException(ErrorCode.PAST_WAITING_CANCEL);
        }
        reservationWaitingRepository.deleteById(id);
    }

    public List<ReservationWaitingTurnResponse> getWaitingByMemberId(Long memberId) {
        return reservationWaitingRepository.findByMemberId(memberId).stream()
                .map(waiting -> ReservationWaitingTurnResponse.from(waiting,
                        reservationWaitingRepository.calculateTurn(
                                waiting.getId(), waiting.getDate(), waiting.getTime().getId(),
                                waiting.getTheme().getId())))
                .collect(Collectors.toList());
    }
}