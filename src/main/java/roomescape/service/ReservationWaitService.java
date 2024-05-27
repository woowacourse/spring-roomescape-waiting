package roomescape.service;


import static roomescape.domain.ReservationStatus.RESERVE_NUMBER;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWait;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationWaitRepository;
import roomescape.exception.member.AuthenticationFailureException;
import roomescape.exception.reservation.NotFoundReservationException;
import roomescape.exception.wait.NotFoundWaitException;
import roomescape.service.dto.request.wait.WaitRequest;
import roomescape.service.dto.response.wait.AdminWaitResponse;
import roomescape.service.dto.response.wait.WaitResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationWaitService {
    private final ReservationWaitRepository waitRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public List<WaitResponse> findAllByMemberId(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(AuthenticationFailureException::new);

        return waitRepository.findAllByMember(member)
                .stream()
                .map(wait -> WaitResponse.from(wait,
                        waitRepository.countByReservationAndStatusPriorityIsLessThan(wait.getReservation(),
                                wait.getPriority())))
                .toList();
    }

    public List<AdminWaitResponse> findAllWaits() {
        return waitRepository.findAll()
                .stream()
                .map(AdminWaitResponse::from)
                .toList();
    }

    @Transactional
    public void saveReservationWait(WaitRequest request, long memberId) {
        Reservation reservation = reservationRepository.findByDateAndTimeIdAndThemeId(request.date(), request.timeId(),
                        request.themeId())
                .orElseThrow(NotFoundReservationException::new);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(AuthenticationFailureException::new);

        Long nextPriority = waitRepository.findTopByOrderByStatusPriorityDesc()
                .map(ReservationWait::getNextPriority)
                .orElse(RESERVE_NUMBER);

        ReservationWait wait = new ReservationWait(member, reservation, nextPriority);

        verifyWait(wait);
        waitRepository.save(wait);
    }

    private void verifyWait(ReservationWait wait) {
        boolean hasSameWait = waitRepository.existsByMemberAndReservation(wait.getMember(), wait.getReservation());
        wait.validateDuplicateWait(hasSameWait);
    }

    @Transactional
    public void deleteReservationWait(Long reservationId, Long memberId) {
        ReservationWait wait = waitRepository.findByMemberIdAndReservationId(memberId, reservationId)
                .orElseThrow(NotFoundWaitException::new);

        waitRepository.deleteById(wait.getId());
        proceedAutoScheduling(wait);
    }

    private void proceedAutoScheduling(ReservationWait wait) {
        if (wait.isReserved()) {
            waitRepository.findTopByReservationOrderByStatusPriorityAsc(wait.getReservation())
                    .ifPresentOrElse(
                            ReservationWait::reserve,
                            () -> reservationRepository.delete(wait.getReservation()));
        }
    }
}
