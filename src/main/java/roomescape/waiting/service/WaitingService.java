package roomescape.waiting.service;

import org.springframework.stereotype.Service;
import roomescape.global.auth.LoginMember;
import roomescape.global.exception.custom.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.dto.CreateWaitingRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public WaitingService(final WaitingRepository waitingRepository, final MemberRepository memberRepository,
                          final ReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    public WaitingResponse createWaiting(final CreateWaitingRequest request, final LoginMember loginMember) {
        final Member member = memberRepository.findById(loginMember.id())
                .orElseThrow(() -> new BadRequestException("회원 정보를 찾을 수 없습니다."));
        final Reservation reservation = reservationRepository.findFirstByDateAndThemeIdAndTimeId(request.date(),
                request.themeId(), request.timeId()
        ).orElseThrow(() -> new BadRequestException("예약 정보를 찾을 수 없습니다."));
        validateWaiting(reservation, member);
        final Waiting waiting = Waiting.register(member, reservation);
        final Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingResponse(savedWaiting);
    }

    private void validateWaiting(final Reservation reservation, final Member member) {
        if (reservation.hasOwner(member)) {
            throw new BadRequestException("이미 선점한 예약입니다. 추가 대기는 불가능합니다.");
        }
        if (waitingRepository.existsByMemberIdAndReservationId(member.getId(), reservation.getId())) {
            throw new BadRequestException("이미 대기 중인 예약입니다. 중복 대기는 불가능합니다.");
        }
    }
}
