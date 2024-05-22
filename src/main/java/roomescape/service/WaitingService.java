package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.user.Member;
import roomescape.exception.NotExistException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.output.WaitingOutput;

import java.util.List;

import static roomescape.exception.ExceptionDomainType.WAITING;

@Service
public class WaitingService {
    private final MemberRepository memberRepository;
    private final ReservationInfoCreateValidator reservationInfoCreateValidator;
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public WaitingService(final MemberRepository memberRepository, final ReservationInfoCreateValidator reservationInfoCreateValidator, final ReservationRepository reservationRepository, final WaitingRepository waitingRepository) {
        this.memberRepository = memberRepository;
        this.reservationInfoCreateValidator = reservationInfoCreateValidator;
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public WaitingOutput createWaiting(final ReservationInput input) {
        final ReservationInfo reservationInfo = reservationInfoCreateValidator.validateReservationInput(input.parseReservationInfoInput());
        final Member member = memberRepository.getById(input.memberId());

        if (waitingRepository.existsByMemberAndReservationInfo(member, reservationInfo) || reservationRepository.existsByMemberAndReservationInfo(member, reservationInfo)) {
            throw new IllegalArgumentException(String.format("%s는 %s에 대한 대기가 이미 존재합니다.", member.getName(), reservationInfo.getLocalDateTimeFormat()));
        }
        if (reservationRepository.notExistsByReservationInfo(reservationInfo)) {
            throw new IllegalArgumentException(String.format("%s에 대한 예약이 없습니다.", reservationInfo));
        }
        final Waiting waiting = waitingRepository.save(new Waiting(member, reservationInfo));
        return WaitingOutput.toOutput(waiting, getOrderWaitingByReservationInfo(waiting));
    }

    public List<WaitingOutput> getAllMyWaiting(final long memberId) {
        final List<Waiting> waitings = waitingRepository.findAllByMemberId(memberId);
        return waitings.stream()
                .map(waiting -> WaitingOutput.toOutput(waiting, getOrderWaitingByReservationInfo(waiting)))
                .toList();
    }

    private int getOrderWaitingByReservationInfo(final Waiting waiting) {
        return waitingRepository.findAllByReservationInfoOrderByCreatedDateAsc(waiting.getReservationInfo())
                .indexOf(waiting) + 1;
    }

    public void deleteWaiting(final long id,final long memberId) {
        final Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(()->new NotExistException(WAITING,id));
        if(waiting.getMember().isEqualId(memberId)){
            waitingRepository.deleteById(id);
        }
        throw new IllegalArgumentException("현재 사용자의 대기가 아닙니다.");
    }
}
