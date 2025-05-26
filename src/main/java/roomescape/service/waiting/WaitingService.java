package roomescape.service.waiting;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.dto.reservation.AddReservationDto;
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

    @Transactional
    public long addWaiting(AddReservationDto newReservationDto, Long memberId) {
        Member member = memberService.getMemberById(memberId);
        ReservationTime reservationTime = reservationTimeService.getReservationTimeById(newReservationDto.timeId());
        Theme theme = themeService.getThemeById(newReservationDto.themeId());
        Waiting waiting = new Waiting(null, newReservationDto.date(), reservationTime, theme, member);
        validateDuplicateWaiting(waiting);
        validateAddReservationDateTime(waiting);
        return waitingRepository.save(waiting);
    }

    private void validateDuplicateWaiting(Waiting waiting) {
        if (waitingRepository.existsByDateAndTimeAndThemeAndMember(waiting)) {
            throw new IllegalArgumentException("이미 해당 예약에 대기를 신청한 상태입니다.");
        }
    }

    private void validateAddReservationDateTime(Waiting waiting) {
        LocalDateTime currentDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.now());
        if (waiting.isBeforeDateTime(currentDateTime)) {
            throw new IllegalArgumentException("과거 시간에 대기할 수 없습니다.");
        }
    }

    public List<Waiting> getAllWaitings() {
        return waitingRepository.findAll();
    }

    public Waiting getWaitingById(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약대기입니다."));
    }

    public List<Waiting> getAllByDateAndThemeId(LocalDate date, Long themeId) {
        return waitingRepository.findByDateAndThemeId(date, themeId);
    }

    public List<WaitingWithRank> getWaitingsWithRankByMemberId(Long memberId) {
        return waitingRepository.findWaitingsWithRankByMemberId(memberId);
    }

    public boolean existsByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        return waitingRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    public void deleteWaiting(Long id) {
        waitingRepository.deleteById(id);
    }
}
