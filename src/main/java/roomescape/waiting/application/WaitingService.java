package roomescape.waiting.application;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.application.ReservationScheduleService;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.MyReservation;
import roomescape.reservation.domain.ReservationSchedule;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.waiting.application.dto.WaitingIdResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;
import roomescape.waiting.application.dto.WaitingInfoResponse;
import roomescape.waiting.domain.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final ReservationScheduleService reservationScheduleService;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;

    public WaitingService(
        final ReservationScheduleService reservationScheduleService,
        final ReservationRepository reservationRepository,
        final WaitingRepository waitingRepository,
        final MemberRepository memberRepository
    ) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationScheduleService = reservationScheduleService;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public WaitingIdResponse addWaiting(@Valid final MemberReservationRequest request,
        final Long memberId) {
        ReservationSchedule schedule = reservationScheduleService.createReservationSchedule(
            request);
        Member member = findMemberById(memberId);
        Waiting waiting = new Waiting(member, schedule);
        validateAlreadyReserved(waiting);
        validateAlreadyWaiting(waiting);
        Waiting savedWaiting = waitingRepository.save(waiting);
        return WaitingIdResponse.from(savedWaiting);
    }

    private void validateAlreadyReserved(final Waiting waiting) {
        Member member = waiting.getMember();
        ReservationSchedule schedule = waiting.getReservationSchedule();
        boolean isAlreadyReserved = reservationRepository.existsByMemberIdAndReservationSchedule_Theme_IdAndReservationSchedule_ReservationTime_IdAndReservationSchedule_Date(
            member.getId(),
            schedule.getThemeId(),
            schedule.getReservationTimeId(),
            schedule.getDate()
        );
        if (isAlreadyReserved) {
            throw new IllegalArgumentException("이미 예약이 되어있는 상태에서는, 대기할 수 없습니다.");
        }
    }

    private void validateAlreadyWaiting(final Waiting waiting) {
        Member member = waiting.getMember();
        ReservationSchedule schedule = waiting.getReservationSchedule();
        boolean isAlreadyWaiting = waitingRepository.existsWaiting(
            member.getId(), schedule.getThemeId(), schedule.getReservationTimeId(),
            schedule.getDate());
        if (isAlreadyWaiting) {
            throw new IllegalArgumentException("이미 대기중인 상태에서는, 추가로 대기할 수 없습니다.");
        }
    }

    public List<MyReservation> getWaitingsFromMember(final Long memberId) {
        List<WaitingWithRank> waitingWithRanks = waitingRepository.findWaitingsWithRankByMemberId(
            memberId);
        return waitingWithRanks.stream()
            .map(MyReservation::from)
            .toList();
    }

    @Transactional
    public void cancel(final Long memberId, final Long waitingId) {
        Waiting waiting = findWaitingById(waitingId);
        if (!waiting.isSameMemberId(memberId)) {
            throw new IllegalArgumentException("본인의 대기만 삭제할 수 있습니다.");
        }
        waitingRepository.deleteById(waitingId);
    }

    @Transactional
    public void cancelFromAdmin(final Long waitingId) {
        findWaitingById(waitingId);
        waitingRepository.deleteById(waitingId);
    }

    public List<WaitingInfoResponse> getAllWaitingInfos() {
        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
            .map(WaitingInfoResponse::from)
            .toList();
    }

    private Member findMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException("선택한 멤버가 존재하지 않습니다."));
    }

    private Waiting findWaitingById(final Long waitingId) {
        return waitingRepository.findById(waitingId)
            .orElseThrow(() -> new NotFoundException("선택한 웨이팅이 존재하지 않습니다."));
    }
}
