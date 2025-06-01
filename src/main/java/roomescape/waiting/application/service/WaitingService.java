package roomescape.waiting.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.datetime.CurrentDateTime;
import roomescape.common.exception.RoomescapeException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.application.dto.WaitingInfo;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final CurrentDateTime currentDateTime;

    @Transactional
    public WaitingInfo createWaiting(final WaitingCreateCommand command) {
        final Waiting waiting = makeWaiting(command);
        final Waiting savedWaiting = waitingRepository.save(waiting);
        return new WaitingInfo(savedWaiting);
    }

    private Waiting makeWaiting(final WaitingCreateCommand command) {
        final Reservation reservation = findReservation(command);
        final Member member = findMember(command);
        validateReservationOfWaiting(reservation, member);
        return command.convertToEntity(reservation, member);
    }

    private void validateReservationOfWaiting(final Reservation reservation, final Member member) {
        validateOwnedReservation(reservation, member);
        validateDuplicateWaiting(reservation, member);
        validatePastWaiting(reservation);
    }

    private void validateOwnedReservation(final Reservation reservation, final Member member) {
        if (reservation.isOwnedBy(member)) {
            throw new RoomescapeException("이미 예약한 슬롯에 예약 대기를 할 수 없습니다.");
        }
    }

    private void validateDuplicateWaiting(final Reservation reservation, final Member member) {
        if (waitingRepository.existsByReservationIdAndMemberId(reservation.id(), member.id())) {
            throw new RoomescapeException("이미 예약 대기한 슬롯에 예약 대기를 할 수 없습니다.");
        }
    }

    private void validatePastWaiting(final Reservation reservation) {
        if (reservation.isPast(currentDateTime.getDate(), currentDateTime.getTime())) {
            throw new RoomescapeException("이미 지난 슬롯에 예약 대기를 할 수 없습니다.");
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

    private Reservation findReservation(final WaitingCreateCommand command) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(command.date(), command.timeId(), command.themeId())
                .orElseThrow(() -> new RoomescapeException("예약이 존재하지 않습니다."));
    }

    private Member findMember(final WaitingCreateCommand command) {
        return memberRepository.findById(command.memberId())
                .orElseThrow(() -> new RoomescapeException("멤버가 존재하지 않습니다."));
    }
}
