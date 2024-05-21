package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import roomescape.member.dto.MemberRequest;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public WaitingResponse addWaiting(ReservationRequest reservationRequest, MemberRequest memberRequest) {
        ReservationTime reservationTime = reservationTimeRepository.getById(reservationRequest.timeId());
        Theme theme = themeRepository.getById(reservationRequest.themeId());

        Waiting waiting = new Waiting(reservationRequest.date(), reservationTime, theme, memberRequest.toMember());
        Waiting save = waitingRepository.save(waiting);

        return new WaitingResponse(save);
    }
}
