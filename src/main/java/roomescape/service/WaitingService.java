package roomescape.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.dto.request.CreateWaitingRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.entity.Member;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.entity.Waiting;
import roomescape.exception.custom.InvalidMemberException;
import roomescape.exception.custom.InvalidReservationTimeException;
import roomescape.exception.custom.InvalidThemeException;
import roomescape.global.ReservationStatus;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@Service
public class WaitingService {

    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository, MemberRepository memberRepository,
                          ReservationRepository reservationRepository,
                          ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public Waiting addWaiting(CreateWaitingRequest request, LoginMemberRequest loginMemberRequest) {
        return createReservation(loginMemberRequest.id(), request.themeId(), request.date(), request.timeId());
    }

    private Waiting createReservation(
            long memberId,
            long themeId,
            LocalDate date,
            long timeId
    ) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidMemberException("존재하지 않는 멤버 ID입니다."));
        ReservationTime reservationTime = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new InvalidReservationTimeException("존재하지 않는 예약 시간입니다."));
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidThemeException("존재하지 않는 테마입니다."));

        Waiting waiting = new Waiting(member, date, reservationTime, theme);
        return waitingRepository.save(waiting);
    }

}
