package roomescape.service.waiting;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.waiting.ApplyWaitingRequestDto;
import roomescape.repository.waiting.WaitingRepository;
import roomescape.service.member.MemberService;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.reserveticket.ReserveTicketService;
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
    private final ReserveTicketService reserveTicketService;

    public WaitingService(WaitingRepository waitingRepository, MemberService memberService, ReservationTimeService reservationTimeService, ThemeService themeService, ReserveTicketService reserveTicketService) {
        this.waitingRepository = waitingRepository;
        this.memberService = memberService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reserveTicketService = reserveTicketService;
    }

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
            throw new IllegalArgumentException("중복된 예약대기 신청입니다");
        }
    }

    private void validateAddReservationDateTime(Waiting waiting) {
        LocalDateTime currentDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.now());
        if (waiting.isBeforeDateTime(currentDateTime)) {
            throw new IllegalArgumentException("과거 시간에 예약할 수 없습니다.");
        }
    }

    public List<Waiting> getAllWaitings() {
        return waitingRepository.findAll();
    }

    public Waiting getWaitingById(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약대기입니다."));
    }

    public List<WaitingWithRank> getWaitingsWithRankByMemberId(Long memberId) {
        return waitingRepository.findWaitingsWithRankByMemberId(memberId);
    }

    public void deleteWaiting(Long id) {
        waitingRepository.deleteById(id);
    }

    @Transactional
    public Long apply(ApplyWaitingRequestDto applyWaitingRequestDto) {
        Long waitingId = applyWaitingRequestDto.id();
        Waiting waiting = getWaitingById(waitingId);
        Long reservationId = reserveTicketService.addReservation(new AddReservationDto(waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId()), waiting.getMember().getId());
        deleteWaiting(waitingId);
        return reservationId;
    }
}
