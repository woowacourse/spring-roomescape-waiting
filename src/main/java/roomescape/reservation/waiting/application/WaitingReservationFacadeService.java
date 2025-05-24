package roomescape.reservation.waiting.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import roomescape.common.exception.BusinessException;
import roomescape.member.application.service.MemberQueryService;
import roomescape.member.domain.Member;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.waiting.application.service.WaitingReservationCommandService;
import roomescape.reservation.waiting.domain.WaitingReservation;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.WaitingReservationResponse;
import roomescape.reservation.time.application.service.ReservationTimeQueryService;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.application.service.ThemeQueryService;
import roomescape.theme.domain.Theme;

@Service
public class WaitingReservationFacadeService {

    private final ReservationQueryService reservationQueryService;
    private final WaitingReservationCommandService waitingReservationCommandService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ThemeQueryService themeQueryService;
    private final MemberQueryService memberQueryService;

    public WaitingReservationFacadeService(final ReservationQueryService reservationQueryService,
                                           final WaitingReservationCommandService waitingReservationCommandService,
                                           final ReservationTimeQueryService reservationTimeQueryService,
                                           final ThemeQueryService themeQueryService,
                                           final MemberQueryService memberQueryService) {
        this.reservationQueryService = reservationQueryService;
        this.waitingReservationCommandService = waitingReservationCommandService;
        this.reservationTimeQueryService = reservationTimeQueryService;
        this.themeQueryService = themeQueryService;
        this.memberQueryService = memberQueryService;
    }

    public WaitingReservationResponse createWaitingReservation(final ReservationRequest request, final Long memberId) {
        ReservationTime time = reservationTimeQueryService.findById(request.timeId());
        Theme theme = themeQueryService.findById(request.themeId());
        Member member = memberQueryService.findById(memberId);
        validateExistsReservation(theme.getId(), time.getId(), request.date());

        LocalDateTime now = LocalDateTime.now();
        WaitingReservation waitingReservation = new WaitingReservation(request.date(), time, theme, member);
        validateCanReserveDateTime(waitingReservation, now);

        WaitingReservation responseWaiting = waitingReservationCommandService.save(waitingReservation);

        return WaitingReservationResponse.from(responseWaiting);
    }

    private void validateExistsReservation(final Long themeId, final Long timeId, final LocalDate date) {
        if (!reservationQueryService.isExistsReservedReservation(themeId, timeId, date)) {
            throw new BusinessException("대기할 예약이 존재하지 않습니다.");
        }
    }

    private void validateCanReserveDateTime(final WaitingReservation waitingReservation, final LocalDateTime now) {
        if (waitingReservation.isCannotReserveDateTime(now)) {
            throw new BusinessException("예약할 수 없는 날짜와 시간입니다.");
        }
    }

    public void deleteByIdWithMemberId(final Long memberId, final Long reservationId) {
        waitingReservationCommandService.deleteByIdAndMemberId(reservationId, memberId);
    }
}
