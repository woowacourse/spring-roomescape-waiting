package roomescape.service.reservation;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.domain.reservation.ReservationWaitingRepository;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.ReservationTimeRepository;
import roomescape.domain.schedule.Schedule;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.exception.InvalidMemberException;
import roomescape.exception.InvalidReservationException;
import roomescape.service.reservation.dto.ReservationRequest;
import roomescape.service.reservation.dto.ReservationWaitingResponse;

@Service
public class ReservationWaitingService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingService(ReservationTimeRepository reservationTimeRepository,
                                     ThemeRepository themeRepository, MemberRepository memberRepository,
                                     ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    public ReservationWaitingResponse create(ReservationRequest waitingRequest, long memberId) {
        return createReservationWaiting(
                waitingRequest.timeId(), waitingRequest.themeId(), memberId, waitingRequest.date()
        );
    }

    private ReservationWaitingResponse createReservationWaiting(
            long timeId, long themeId, long memberId, LocalDate date
    ) {
        ReservationDate reservationDate = ReservationDate.of(date);
        ReservationTime reservationTime = findTimeById(timeId);
        Schedule schedule = new Schedule(reservationDate, reservationTime);

        Theme theme = findThemeById(themeId);
        Member member = findMemberById(memberId);
        // TODO: 예약 대기 검증 추가

        ReservationWaiting waiting = reservationWaitingRepository.save(new ReservationWaiting(member, theme, schedule));

        return new ReservationWaitingResponse(waiting);
    }

    private ReservationTime findTimeById(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new InvalidReservationException("더이상 존재하지 않는 시간입니다."));
    }

    private Theme findThemeById(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidReservationException("더이상 존재하지 않는 테마입니다."));
    }

    private Member findMemberById(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 회원입니다."));
    }
}
