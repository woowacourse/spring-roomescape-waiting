package roomescape.service.waiting;

import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.exception.reservation.InvalidReservationException;
import roomescape.repository.waiting.WaitingRepository;
import roomescape.service.member.MemberService;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberService memberService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public WaitingService(WaitingRepository waitingRepository, MemberService memberService, ReservationTimeService reservationTimeService, ThemeService themeService) {
        this.waitingRepository = waitingRepository;
        this.memberService = memberService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    public long addWaiting(AddReservationDto newReservationDto, Long memberId) {
        Member member = memberService.getMemberById(memberId);
        ReservationTime reservationTime = reservationTimeService.getReservationTimeById(newReservationDto.timeId());
        Theme theme = themeService.getThemeById(newReservationDto.themeId());
        Waiting waiting = new Waiting(null, newReservationDto.date(), reservationTime, theme, member);
        validateDuplicateWaiting(waiting);
        LocalDateTime currentDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.now());
        validateAddReservationDateTime(waiting, currentDateTime);
        return waitingRepository.save(waiting);
    }

    private void validateDuplicateWaiting(Waiting waiting) {
        if (waitingRepository.existsByDateAndTimeAndThemeAndMember(waiting)) {
            throw new InvalidReservationException("중복된 예약대기 신청입니다");
        }
    }

    private void validateAddReservationDateTime(Waiting waiting, LocalDateTime currentDateTime) {
        if (waiting.isBeforeDateTime(currentDateTime)) {
            throw new InvalidReservationException("과거 시간에 예약할 수 없습니다.");
        }
    }

    public List<Waiting> getAllWaitings() {
        return waitingRepository.findAll();
    }

    public Waiting getWaitingById(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new InvalidReservationException("존재하지 않는 예약대기입니다."));
    }

    public List<WaitingWithRank> getWaitingsWithRankByMemberId(Long memberId) {
        return waitingRepository.findWaitingsWithRankByMemberId(memberId);
    }

    public void deleteWaiting(Long id) {
        waitingRepository.deleteById(id);
    }
}
