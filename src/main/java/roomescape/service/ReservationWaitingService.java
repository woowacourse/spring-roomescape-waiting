package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationWaiting;
import roomescape.infrastructure.MemberRepository;
import roomescape.infrastructure.ReservationRepository;
import roomescape.infrastructure.ReservationWaitingRepository;
import roomescape.service.exception.PastReservationException;
import roomescape.service.request.ReservationWaitingAppRequest;
import roomescape.service.response.ReservationWaitingAppResponse;

@Service
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository,
                                     ReservationRepository reservationRepository,
                                     MemberRepository memberRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationWaitingAppResponse> findAll() {
        return reservationWaitingRepository.findAll().stream()
                .map(ReservationWaitingAppResponse::from)
                .toList();
    }

    public List<ReservationWaitingAppResponse> findAllByMemberId(Long memberId) {
        return reservationWaitingRepository.findAllByMemberId(memberId).stream()
                .map(ReservationWaitingAppResponse::from)
                .toList();
    }

    public ReservationWaitingAppResponse save(ReservationWaitingAppRequest request) {
        Member member = findMember(request.memberId());
        Reservation reservation = findReservation(request.date(), request.timeId(), request.themeId());
        ReservationWaiting newReservationWaiting = new ReservationWaiting(
                member,
                reservation,
                getPriority(reservation.getId())
        );
        validateDuplication(newReservationWaiting.getReservation().getId(), newReservationWaiting.getMember().getId());
        validatePast(newReservationWaiting);
        ;
        ReservationWaiting savedreservationWaiting = reservationWaitingRepository.save(newReservationWaiting);

        return ReservationWaitingAppResponse.from(savedreservationWaiting);
    }

    private long getPriority(Long reservationId) {
        return reservationWaitingRepository.countByReservationId(reservationId) + 1;
    }

    private void validateDuplication(Long reservationId, Long memberId) {
        if (reservationWaitingRepository.existsByReservationIdAndMemberId(reservationId, memberId)) {
            throw new IllegalStateException("중복 예약 대기는 불가능합니다.");
        }
    }

    private void validatePast(ReservationWaiting reservationWaiting) {
        if (reservationWaiting.isPast()) {
            throw new PastReservationException();
        }
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원 정보를 찾지 못했습니다."));
    }

    private Reservation findReservation(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(new ReservationDate(date.toString()), timeId,
                        themeId)
                .orElseThrow(() -> new NoSuchElementException("해당 예약이 없습니다. 예약해주세요."));
    }
}
