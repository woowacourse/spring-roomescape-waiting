package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.*;
import roomescape.domain.repository.*;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dto.request.WaitingRequest;

import java.time.LocalDateTime;

@Service
public class WaitingService {
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;
    private final WaitingRepository waitingRepository;

    public WaitingService(
            ReservationRepository reservationRepository,
            MemberRepository memberRepository,
            ThemeRepository themeRepository,
            ReservationTimeRepository timeRepository,
            WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.timeRepository = timeRepository;
        this.waitingRepository = waitingRepository;
    }

    public void saveWaiting(WaitingRequest waitingRequest) {

        if (hasNoneReservation(waitingRequest)) {
            saveReservation(waitingRequest);
            return;
        }

        Reservation alreadyBookedReservation = findReservation(waitingRequest);
        Waiting waiting = createWaiting(waitingRequest);
        alreadyBookedReservation.addWaiting(waiting);
        waitingRepository.save(waiting);
    }

    private void saveReservation(WaitingRequest waitingRequest) {
        reservationRepository.save(createReservation(waitingRequest));
    }

    private boolean hasNoneReservation(WaitingRequest waitingRequest) {
        return reservationRepository.existsByDateAndTimeAndTheme(
                waitingRequest.date(),
                findTimeById(waitingRequest.timeId()),
                findThemeById(waitingRequest.themeId())
        );
    }

    private Waiting createWaiting(WaitingRequest waitingRequest) {
        return new Waiting(
                LocalDateTime.now(),
                findMemberById(waitingRequest.memberId()),
                findReservation(waitingRequest)
        );
    }

    private Reservation findReservation(WaitingRequest waitingRequest) {
        return reservationRepository.findByDateAndThemeAndTime(
                waitingRequest.date(),
                findThemeById(waitingRequest.themeId()),
                findTimeById(waitingRequest.timeId())
        ).orElseThrow(() -> new RoomEscapeBusinessException("예약이 없습니다"));
    }

    private Reservation createReservation(WaitingRequest waitingRequest) {
        return new Reservation(
                findMemberById(waitingRequest.memberId()),
                waitingRequest.date(),
                findTimeById(waitingRequest.timeId()),
                findThemeById(waitingRequest.themeId())
        );
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));
    }

    private ReservationTime findTimeById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약 시간입니다."));
    }

}
