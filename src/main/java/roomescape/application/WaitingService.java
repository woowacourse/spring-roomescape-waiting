package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.Waiting;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.infrastructure.repository.WaitingRepository;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.request.ReservationCreateRequest;
import roomescape.presentation.dto.response.WaitingResponse;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberService memberService;

    public WaitingService(WaitingRepository waitingRepository,
                          ReservationRepository reservationRepository,
                          MemberService memberService
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberService = memberService;
    }

    @Transactional
    public WaitingResponse createWaiting(ReservationCreateRequest request, LoginMember loginMember) {
        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId());
        Member member = memberService.findMemberByEmail(loginMember.email());
        long rank = waitingRepository.countByReservation(reservation) + 1;
        Waiting waiting = Waiting.create(reservation, member, rank);

        Waiting saved = waitingRepository.save(waiting);
        return WaitingResponse.from(saved);
    }
}
