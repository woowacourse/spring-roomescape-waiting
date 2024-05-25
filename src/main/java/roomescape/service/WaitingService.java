package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.Waiting;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dto.request.WaitingRequest;
import roomescape.service.dto.response.WaitingResponse;
import roomescape.service.dto.response.WaitingResponses;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class WaitingService {
    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public WaitingService(
            WaitingRepository waitingRepository,
            ReservationRepository reservationRepository,
            MemberRepository memberRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
    }

    public WaitingResponse saveWaiting(WaitingRequest waitingRequest, long memberId) {
        Reservation alreadyBookedReservation = findAlreadyBookedReservation(waitingRequest);
        Member member = findMemberById(memberId);
        Waiting waiting = createWaiting(alreadyBookedReservation, member);

        validateAlreadyReservedMember(member, alreadyBookedReservation.getReservationSlot());
        validateDuplicatedWaiting(member, alreadyBookedReservation);

        Waiting savedWaiting = waitingRepository.save(waiting);

        return new WaitingResponse(savedWaiting);
    }

    private Reservation findAlreadyBookedReservation(WaitingRequest waitingRequest) {
        return reservationRepository.findByDateAndThemeIdAndTimeId(
                waitingRequest.date(),
                waitingRequest.themeId(),
                waitingRequest.timeId()
        ).orElseThrow(() -> new RoomEscapeBusinessException("이미 예약된 예약이 없습니다"));
    }

    public WaitingResponses findAllWaitings() {
        // TODO 배치 처리 고민해보기
        List<WaitingResponse> waitingResponses = waitingRepository.findAll()
                .stream()
                .map(WaitingResponse::new)
                .toList();
        return new WaitingResponses(waitingResponses);
    }

    public void deleteWaiting(long id) {
        waitingRepository.delete(findWaitingById(id));
    }

    public void deleteUserWaiting(long waitingId, long memberId) {
        Member member = findMemberById(memberId);
        Waiting waiting = findWaitingById(waitingId);
        validateWaitingOwn(waiting, member);
        waitingRepository.delete(waiting);
    }

    private void validateWaitingOwn(Waiting waiting, Member member) {
        if (!waitingRepository.existsByWaitingAndMember(waiting, member)) {
            throw new RoomEscapeBusinessException("본인의 대기가 아니면 삭제할 수 없습니다.");
        }
    }

    private void validateAlreadyReservedMember(Member member, ReservationSlot slot) {
        if (reservationRepository.existsByMemberAndReservationSlot(member, slot)) {
            throw new RoomEscapeBusinessException("예약에 성공한 유저는 대기를 요청할 수 없습니다.");
        }
    }

    private void validateDuplicatedWaiting(Member member, Reservation reservation) {
        if (waitingRepository.existsByMemberAndReservation(member, reservation)) {
            throw new RoomEscapeBusinessException("중복 예약 대기는 불가합니다.");
        }
    }

    private Waiting createWaiting(Reservation alreadyBookedReservation, Member member) {
        return new Waiting(
                LocalDateTime.now(),
                member,
                alreadyBookedReservation
        );
    }

    private Waiting findWaitingById(long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("예약 대기 기록을 찾을 수 없습니다."));
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));
    }
}
