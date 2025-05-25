package roomescape.reservation.application.waiting.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.datetime.CurrentDateTime;
import roomescape.common.exception.RoomescapeException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.application.waiting.dto.WaitingCreateCommand;
import roomescape.reservation.application.waiting.dto.WaitingInfo;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.theme.ThemeRepository;
import roomescape.reservation.domain.timeslot.TimeSlot;
import roomescape.reservation.domain.timeslot.TimeSlotRepository;
import roomescape.reservation.domain.waiting.Waiting;
import roomescape.reservation.domain.waiting.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final CurrentDateTime currentDateTime;

    public WaitingService(final WaitingRepository waitingRepository, final TimeSlotRepository timeSlotRepository,
                          final ThemeRepository themeRepository, final MemberRepository memberRepository,
                          final CurrentDateTime dateTimeGenerator) {
        this.waitingRepository = waitingRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.currentDateTime = dateTimeGenerator;
    }

    @Transactional
    public WaitingInfo createWaiting(final WaitingCreateCommand command) {
        final Waiting waiting = makeWaiting(command);
        final Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingInfo(savedWaiting);
    }

    private Waiting makeWaiting(final WaitingCreateCommand command) {
        final TimeSlot timeSlot = findTimeSlot(command.timeId());
        final Member member = findMember(command.memberId());
        final Theme theme = findTheme(command.themeId());
        validatePastDateTime(command.date(), timeSlot);
        validateDuplicateWaiting(command.date(), command.timeId(), command.themeId(), member.id());
        return command.convertToEntity(command.date(), timeSlot, theme, member);
    }

    private void validatePastDateTime(final LocalDate date, final TimeSlot timeSlot) {
        final LocalDate currentDate = currentDateTime.getDate();
        final LocalTime currentTime = currentDateTime.getTime();

        final boolean isPastDate = date.isBefore(currentDate);
        final boolean isSameDateButPastTime = date.isEqual(currentDate) && timeSlot.isBefore(currentTime);

        if (isPastDate || isSameDateButPastTime) {
            throw new RoomescapeException("지나간 날짜와 시간은 예약 대기할 수 없습니다.");
        }
    }

    private void validateDuplicateWaiting(final LocalDate date, final long timeId, final long themeId,
                                          final long memberId) {
        if (waitingRepository.existsByReservationAndMemberId(date, timeId, themeId, memberId)) {
            throw new RoomescapeException("해당 시간에 이미 예약 대기가 존재합니다.");
        }
    }

    @Transactional
    public void cancelWaitingById(final long id) {
        waitingRepository.deleteById(id);
    }

    public List<WaitingInfo> findWaitings() {
        return waitingRepository.findAll().stream()
                .map(WaitingInfo::new)
                .toList();
    }

    private TimeSlot findTimeSlot(final long timeId) {
        return timeSlotRepository.findById(timeId)
                .orElseThrow(() -> new RoomescapeException("예약 시간이 존재하지 않습니다."));
    }

    private Theme findTheme(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomescapeException("테마가 존재하지 않습니다."));
    }

    private Member findMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomescapeException("멤버가 존재하지 않습니다."));
    }
}
