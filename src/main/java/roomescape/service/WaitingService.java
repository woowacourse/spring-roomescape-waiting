package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.NotFoundException;
import roomescape.model.ReservationTime;
import roomescape.model.Waiting;
import roomescape.model.WaitingWithRank;
import roomescape.model.member.LoginMember;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationDto;

import java.util.List;
import java.util.Optional;

@Service
public class WaitingService { // TODO: 본인이 대기한 예약은 대기할 수 없도록

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

    public List<Waiting> findAllWaiting() {
        return waitingRepository.findAll();
    }

    public Waiting saveWaiting(ReservationDto reservationDto) {
        // TODO: findById -> save vs save -> findById
        // TODO: 검증 로직 추가
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

    public List<WaitingWithRank> findWaitingByMember(LoginMember member) {
        return waitingRepository.findWaitingWithRankByMemberId(member.getId());
    }

    public void deleteWaitingOfMember(long id) {
        // TODO: 본인의 예약 대기가 맞는지 검증 or deleteByIdAndMemberId
        validateExistence(id);
        waitingRepository.deleteById(id);
    }

    public void deleteWaiting(long id) {
        validateExistence(id);
        waitingRepository.deleteById(id);
    }

    private void validateExistence(long id) {
        boolean isNotExist = !waitingRepository.existsById(id);
        if (isNotExist) {
            throw new NotFoundException("[ERROR] 존재하지 않는 예약 대기입니다.");
        }
    }
}
