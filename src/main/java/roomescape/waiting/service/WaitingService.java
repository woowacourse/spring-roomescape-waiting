package roomescape.waiting.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.exception.BadArgumentRequestException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.dto.WaitingRequest;
import roomescape.waiting.dto.WaitingResponse;
import roomescape.waiting.repository.WaitingRepository;

@Service
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public WaitingService(WaitingRepository waitingRepository,
                          ReservationRepository reservationRepository,
                          MemberRepository memberRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public List<WaitingResponse> findWaitings() {
        return waitingRepository.findAll()
                .stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findWaitingsByMemberId(Long memberId) {
        return waitingRepository.findByMemberId(memberId)
                .stream()
                .map(waiting -> MyReservationResponse.from(waiting, countOrderOfWaiting(waiting)))
                .toList();

    }

    private Long countOrderOfWaiting(Waiting waiting) {
        return waitingRepository.countByReservationAndCreatedAtLessThanEqual(
                waiting.getReservation(), waiting.getCreatedAt());
    }

    public Optional<WaitingResponse> findHighPriorityWaiting(Long reservationId) {
        return waitingRepository.findTopByReservationIdOrderByCreatedAtAsc(reservationId)
                .map(WaitingResponse::from);
    }

    private Waiting findWaiting(Long waitingId) {
        return waitingRepository.findById(waitingId)
                .orElseThrow(() -> new BadArgumentRequestException("해당 예약 대기가 존재하지 않습니다."));
    }

    public WaitingResponse createWaiting(WaitingRequest request, Long requestMemberId) {
        Reservation reservation = findReservation(request);
        Member member = findMember(requestMemberId);
        Waiting waiting = new Waiting(reservation, member);

        validateIsAvailable(waiting);
        waitingRepository.save(waiting);
        return WaitingResponse.from(waiting);
    }

    private Reservation findReservation(WaitingRequest request) {
        return reservationRepository.findByDateAndTimeIdAndThemeId(
                        request.date(), request.timeId(), request.themeId())
                .orElseThrow(() -> new BadArgumentRequestException("아직 예약되지 않았습니다."));
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BadArgumentRequestException("해당 유저가 존재하지 않습니다."));
    }

    private void validateIsAvailable(Waiting waiting) {
        if (waiting.isBefore(LocalDateTime.now())) {
            throw new BadArgumentRequestException("예약 대기는 현재 시간 이후이어야 합니다.");
        }
        if (isAlreadyWaited(waiting.getReservation(), waiting.getMember())) {
            throw new BadArgumentRequestException("이미 예약 했습니다.");
        }
    }

    private boolean isAlreadyWaited(Reservation reservation, Member member) {
        return reservation.getMember().equals(member)
                || waitingRepository.existsByReservationIdAndMemberId(reservation.getId(), member.getId());
    }

    public void confirmReservation(Long waitingId) {
        Waiting waiting = findWaiting(waitingId);
        waiting.confirmReservation();
    }

    public void deleteWaiting(Long id) {
        waitingRepository.deleteById(id);
    }

    public void deleteWaiting(Long waitingId, Long requestMemberId) {
        Waiting waiting = findWaiting(waitingId);

        validateIsOwner(waiting, requestMemberId);
        waitingRepository.delete(waiting);
    }

    private void validateIsOwner(Waiting waiting, Long requestMemberId) {
        if (waiting.isNotWaitingOwner(requestMemberId)) {
            throw new BadArgumentRequestException("해당 예약을 취소할 수 없습니다.");
        }
    }
}
