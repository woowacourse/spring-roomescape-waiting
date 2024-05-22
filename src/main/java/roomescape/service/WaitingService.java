package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.model.ReservationTime;
import roomescape.model.Waiting;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationDto;

import java.util.Optional;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(WaitingRepository waitingRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          MemberRepository memberRepository,
                          ThemeRepository themeRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
    }

    public Waiting saveWaiting(ReservationDto reservationDto) {
        // TODO: findById -> save vs save -> findById
        ReservationTime time = findReservationTime(reservationDto);
        Theme theme = findTheme(reservationDto);
        Member member = findMember(reservationDto);
        Waiting waiting = Waiting.of(reservationDto, time, theme, member);
        return waitingRepository.save(waiting);
    }

    private ReservationTime findReservationTime(ReservationDto reservationDto) {
        long timeId = reservationDto.getTimeId();
        Optional<ReservationTime> time = reservationTimeRepository.findById(timeId);
        return time.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    private Theme findTheme(ReservationDto reservationDto) {
        long themeId = reservationDto.getThemeId();
        Optional<Theme> theme = themeRepository.findById(themeId);
        return theme.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    private Member findMember(ReservationDto reservationDto) {
        long memberId = reservationDto.getMemberId();
        Optional<Member> member = memberRepository.findById(memberId);
        return member.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
    }
}
