package roomescape.reservation.application.waiting.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.application.waiting.dto.ReservationWaitingCreateCommand;
import roomescape.reservation.application.waiting.dto.ReservationWaitingInfo;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.reservation.ReservationRepository;
import roomescape.reservation.domain.waiting.ReservationWaiting;
import roomescape.reservation.domain.waiting.ReservationWaitingRepository;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public ReservationWaitingService(final ReservationWaitingRepository reservationWaitingRepository, final ReservationRepository reservationRepository, final MemberRepository memberRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationWaitingInfo createReservationWaiting(final ReservationWaitingCreateCommand command) {
        final ReservationWaiting reservationWaiting = makeReservationWaiting(command);
        final ReservationWaiting savedReservationWaiting = reservationWaitingRepository.save(reservationWaiting);
        return new ReservationWaitingInfo(savedReservationWaiting);
    }

    private ReservationWaiting makeReservationWaiting(final ReservationWaitingCreateCommand command) {
        final Reservation reservation = findReservation(command.date(), command.timeId(), command.themeId());
        final Member member = findMember(command.memberId());
        validateDuplicateReservationWaiting(reservation.id(), member.id());
        return command.convertToEntity(reservation, member);
    }

    private void validateDuplicateReservationWaiting(final long reservationId, final long memberId) {
        if (reservationWaitingRepository.existsByReservationIdAndMemberId(reservationId, memberId)) {
            throw new IllegalArgumentException("해당 예약 대기에 이미 대기가 존재합니다.");
        }
    }

    private Reservation findReservation(final LocalDate date, final long timeId, final long themeId) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new IllegalArgumentException("예약이 존재하지 않습니다."));
    };

    private Member findMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));
    }

    public void cancelReservationWaitingById(final long id) {
        reservationWaitingRepository.deleteById(id);
    }

    public List<ReservationWaitingInfo> findAll() {
        return reservationWaitingRepository.findAll()
                .stream()
                .map(ReservationWaitingInfo::new)
                .toList();
    }
}
