package roomescape.reservation.application.waiting.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.application.waiting.dto.WaitingCreateCommand;
import roomescape.reservation.application.waiting.dto.WaitingInfo;
import roomescape.reservation.domain.reservation.ReservationRepository;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.theme.ThemeRepository;
import roomescape.reservation.domain.timeslot.TimeSlot;
import roomescape.reservation.domain.timeslot.TimeSlotRepository;
import roomescape.reservation.domain.waiting.Waiting;
import roomescape.reservation.domain.waiting.WaitingRepository;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public WaitingService(final WaitingRepository waitingRepository, final ReservationRepository reservationRepository,
                          final TimeSlotRepository timeSlotRepository, final ThemeRepository themeRepository,
                          final MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public WaitingInfo createWaiting(final WaitingCreateCommand command) {
        final Waiting waiting = makeWaiting(command);
        final Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingInfo(savedWaiting);
    }

    private Waiting makeWaiting(final WaitingCreateCommand command) {
        final TimeSlot timeSlot = findTimeSlot(command.timeId());
        final Member member = findMember(command.memberId());
        final Theme theme = findTheme(command.themeId());
        validateDuplicateWaiting(command.date(), command.timeId(), command.themeId(), member.id());
        return command.convertToEntity(command.date(), timeSlot, theme, member);
    }

    private void validateDuplicateWaiting(final LocalDate date, final long timeId, final long themeId, final long memberId) {
        if (waitingRepository.existsByReservationAndMemberId(date, timeId, themeId, memberId)) {
            throw new IllegalArgumentException("해당 예약 대기에 이미 대기가 존재합니다.");
        }
    }

    public void cancelWaitingById(final long id) {
        waitingRepository.deleteById(id);
    }

    public List<WaitingInfo> findAll() {
        return waitingRepository.findAll()
                .stream()
                .map(WaitingInfo::new)
                .toList();
    }

    private TimeSlot findTimeSlot(final long timeId) {
        return timeSlotRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("예약 시간이 존재하지 않습니다."));
    }

    private Theme findTheme(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("테마가 존재하지 않습니다."));
    }

    private Member findMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));
    }
}
