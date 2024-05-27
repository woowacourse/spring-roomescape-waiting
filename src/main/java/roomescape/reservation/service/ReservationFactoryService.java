package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.request.WaitingReservationSaveRequest;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationFactoryService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationFactoryService(
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public Reservation createSuccess(ReservationSaveRequest saveRequest) {
        Member member = findMember(saveRequest.memberId());
        Theme theme = findTheme(saveRequest.themeId());
        ReservationTime reservationTime = findReservationTime(saveRequest.timeId());

        return saveRequest.toReservation(member, theme, reservationTime);
    }

    public Reservation createWaiting(WaitingReservationSaveRequest saveRequest) {
        Member member = findMember(saveRequest.memberId());
        Theme theme = findTheme(saveRequest.themeId());
        ReservationTime reservationTime = findReservationTime(saveRequest.timeId());

        return saveRequest.toWaitingReservation(member, theme, reservationTime);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));
    }

    private ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));
    }
}
