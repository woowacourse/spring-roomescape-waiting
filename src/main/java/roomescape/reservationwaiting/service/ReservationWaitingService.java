package roomescape.reservationwaiting.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.reservationwaiting.domain.ReservationWaiting;
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
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final ReservationRepository reservationRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationTimeService reservationTimeService,
                                     ThemeService themeService,
                                     ReservationRepository reservationRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationWaitingResponse createWaiting(Member member, ReservationWaitingRequest request) {
        ReservationTime time = reservationTimeService.getById(request.timeId());
        Theme theme = themeService.getById(request.themeId());

        if (!reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "예약이 없는 슬롯에는 대기를 신청할 수 없습니다.");
        }

        if (reservationWaitingRepository.existsByMemberIdAndDateAndTimeIdAndThemeId(
                member.getId(), request.date(), request.timeId(), request.themeId())) {
            throw new BusinessException(HttpStatus.CONFLICT, "같은 슬롯에 중복 대기할 수 없습니다.");
        }

        ReservationWaiting saved = reservationWaitingRepository.save(
                ReservationWaiting.of(member, request.date(), time, theme));
        return ReservationWaitingResponse.from(saved);
    }

    @Transactional
    public void deleteWaiting(Long id, Long memberId) {
        ReservationWaiting waiting = reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 대기입니다."));
        if (!waiting.isOwnedBy(memberId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        if (waiting.isPast()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 지난 시간에는 대기를 취소할 수 없습니다.");
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
