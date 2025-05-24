package roomescape.reservation.waiting.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.BusinessException;
import roomescape.member.application.service.MemberQueryService;
import roomescape.member.domain.Member;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.waiting.application.service.WaitingReservationCommandService;
import roomescape.reservation.waiting.application.service.WaitingReservationQueryService;
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
    private final WaitingReservationQueryService waitingReservationQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ThemeQueryService themeQueryService;
    private final MemberQueryService memberQueryService;
    private final ReservationCommandService reservationCommandService;

    public WaitingReservationFacadeService(final ReservationQueryService reservationQueryService,
                                           final ReservationCommandService reservationCommandService,
                                           final WaitingReservationCommandService waitingReservationCommandService,
                                           final WaitingReservationQueryService waitingReservationQueryService,
                                           final ReservationTimeQueryService reservationTimeQueryService,
                                           final ThemeQueryService themeQueryService,
                                           final MemberQueryService memberQueryService) {
        this.reservationQueryService = reservationQueryService;
        this.reservationCommandService = reservationCommandService;
        this.waitingReservationCommandService = waitingReservationCommandService;
        this.waitingReservationQueryService = waitingReservationQueryService;
        this.reservationTimeQueryService = reservationTimeQueryService;
        this.themeQueryService = themeQueryService;
        this.memberQueryService = memberQueryService;
    }

    public WaitingReservationResponse createWaitingReservation(final ReservationRequest request, final Long memberId) {
        ReservationTime time = reservationTimeQueryService.findById(request.timeId());
        Theme theme = themeQueryService.findById(request.themeId());
        Member member = memberQueryService.findById(memberId);
        validateExistsReservation(theme.getId(), time.getId(), request.date());
        validateExistsWaiting(request, memberId);

        LocalDateTime now = LocalDateTime.now();
        WaitingReservation waitingReservation = new WaitingReservation(request.date(), time, theme, member);
        validateCanReserveDateTime(waitingReservation, now);

        WaitingReservation responseWaiting = waitingReservationCommandService.save(waitingReservation);

        return WaitingReservationResponse.from(responseWaiting);
    }

    private void validateExistsWaiting(final ReservationRequest request, final Long memberId) {
        if (waitingReservationQueryService.isExistsWaitingReservation(request.themeId(), request.timeId(), request.date(), memberId)) {
            throw new BusinessException("중복 예약 대기 할 수 없습니다.");
        }
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

    public void acceptWaiting(final Long waitingId) {
        WaitingReservation waiting = waitingReservationQueryService.findById(waitingId);
        validateAlreadyExistsReservation(waiting);

        waitingReservationCommandService.deleteById(waiting.getId());
        Reservation newReservation = new Reservation(waiting.getDate(), waiting.getTime(), waiting.getTheme(), waiting.getMember());

        reservationCommandService.save(newReservation);
    }

    private void validateAlreadyExistsReservation(WaitingReservation waiting) {
        if (reservationQueryService.isExistsReservedReservation(waiting.getTheme().getId(), waiting.getTime().getId(), waiting.getDate())) {
            throw new BusinessException("예약이 이미 존재합니다.");
        }
    }

    public void denyWaiting(final Long waitingId) {
        waitingReservationCommandService.deleteById(waitingId);
    }

    public List<ReservationResponse> getWaitingReservations() {
        return waitingReservationQueryService.findAll().stream()
            .map(ReservationResponse::from)
            .toList();
    }
}
