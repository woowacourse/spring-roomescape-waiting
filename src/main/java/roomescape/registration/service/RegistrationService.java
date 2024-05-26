package roomescape.registration.service;

import org.springframework.stereotype.Service;
import roomescape.exception.RoomEscapeException;
import roomescape.exception.model.ReservationExceptionCode;
import roomescape.exception.model.ReservationTimeExceptionCode;
import roomescape.exception.model.ThemeExceptionCode;
import roomescape.exception.model.WaitingExceptionCode;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.registration.domain.reservation.domain.Reservation;
import roomescape.registration.domain.reservation.repository.ReservationRepository;
import roomescape.registration.domain.waiting.domain.Waiting;
import roomescape.registration.domain.waiting.service.WaitingService;
import roomescape.registration.dto.RegistrationRequest;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class RegistrationService {

    private final WaitingService waitingService;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;

    public RegistrationService(ReservationRepository reservationRepository, ThemeRepository themeRepository,
                               ReservationTimeRepository reservationTimeRepository, MemberRepository memberRepository,
                               WaitingService waitingService) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.waitingService = waitingService;
    }

    public void approveWaitingToReservation(long waitingId) {
        Waiting waiting = waitingService.findWaitingById(waitingId);
        RegistrationRequest registrationRequest = RegistrationRequest.from(waiting);

        validateCanApprove(registrationRequest);

        ReservationTime time = reservationTimeRepository.findById(registrationRequest.timeId())
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeExceptionCode.FOUND_TIME_IS_NULL_EXCEPTION));
        Theme theme = themeRepository.findById(registrationRequest.themeId())
                .orElseThrow(() -> new RoomEscapeException(ThemeExceptionCode.FOUND_THEME_IS_NULL_EXCEPTION));
        Member member = memberRepository.findMemberById(registrationRequest.memberId())
                .orElseThrow(() -> new RoomEscapeException(ThemeExceptionCode.FOUND_MEMBER_IS_NULL_EXCEPTION));

        Reservation saveReservation = new Reservation(registrationRequest.date(), time, theme, member);
        reservationRepository.save(saveReservation);
        waitingService.removeWaiting(waitingId);
    }

    private void validateCanApprove(RegistrationRequest registrationRequest) {
        boolean existReservaton = reservationRepository.existsByDateAndThemeIdAndReservationTimeId(
                registrationRequest.date(),
                registrationRequest.themeId(), registrationRequest.timeId());
        if (existReservaton) {
            throw new RoomEscapeException(ReservationExceptionCode.SAME_RESERVATION_EXCEPTION);
        }

        long rank = waitingService.countWaitingRank(registrationRequest);
        if (rank != 0) {
            throw new RoomEscapeException(WaitingExceptionCode.APPROVE_ORDER_EXCEPTION);
        }
    }
}
