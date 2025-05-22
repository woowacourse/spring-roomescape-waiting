package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;
import roomescape.reservation.application.dto.request.CreateReservationServiceRequest;
import roomescape.reservation.application.dto.response.WaitingServiceResponse;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.reservation.model.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class ReservationWaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationThemeRepository reservationThemeRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;

    public WaitingServiceResponse create(CreateReservationServiceRequest request) {
        ReservationTheme theme = reservationThemeRepository.getById(request.themeId());
        ReservationTime time = reservationTimeRepository.getById(request.timeId());
        Member member = memberRepository.getById(request.memberId());
        Waiting waiting = Waiting.builder()
            .date(request.date())
            .theme(theme)
            .time(time)
            .member(member)
            .build();

        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingServiceResponse.from(savedWaiting);
    }
}
