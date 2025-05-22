package roomescape.reservation.application;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import roomescape.common.exception.impl.BadRequestException;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.WaitingResponse;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(
        final WaitingRepository waitingRepository,
        final ReservationRepository reservationRepository,
        final ReservationTimeRepository reservationTimeRepository,
        final ThemeRepository themeRepository,
        final MemberRepository memberRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public WaitingResponse addWaiting(@Valid MemberReservationRequest request, Long memberId) {
        ReservationTime reservationTime = getReservationTime(request.timeId());
        Member member = getMember(memberId);
        Theme theme = getTheme(request.themeId());
        Waiting waiting = new Waiting(member, reservationTime, theme, request.date());

        boolean isAlreadyReserved = reservationRepository.existsByMemberIdAndThemeIdAndTimeIdAndDate(
            member.getId(), theme.getId(), reservationTime.getId(), request.date());

        if (isAlreadyReserved) {
            throw new IllegalArgumentException("이미 예약이 되어있는 상태에서는, 대기할 수 없습니다.");
        }

        boolean isAlreadyWaiting = waitingRepository.existsByMemberIdAndThemeIdAndReservationTimeIdAndDate(
            member.getId(), theme.getId(), reservationTime.getId(), request.date()
        );

        if (isAlreadyWaiting) {
            throw new IllegalArgumentException("이미 대기중인 상태에서는, 추가로 대기할 수 없습니다.");
        }

        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingResponse.from(savedWaiting);
    }

    private ReservationTime getReservationTime(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
            .orElseThrow(() -> new NotFoundException("선택한 예약 시간이 존재하지 않습니다."));
    }

    private Theme getTheme(final Long themeId) {
        return themeRepository.findById(themeId)
            .orElseThrow(() -> new NotFoundException("선택한 테마가 존재하지 않습니다."));
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException("선택한 멤버가 존재하지 않습니다."));
    }
}
