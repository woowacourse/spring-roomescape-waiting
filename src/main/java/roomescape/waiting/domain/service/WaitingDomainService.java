package roomescape.waiting.domain.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.reservation.presentation.dto.response.MyReservationResponse;
import roomescape.waiting.exception.WaitingOwnerException;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.repository.WaitingRepository;

@Service
public class WaitingDomainService {

    private final WaitingRepository waitingRepository;

    public WaitingDomainService(final WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public Waiting save(final Waiting waiting) {
        return waitingRepository.save(waiting);
    }

    public List<MyReservationResponse> findMyReservations(final MemberInfo memberInfo) {
        return waitingRepository.findByWaitingMemberId(memberInfo.id())
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteByReservationIdAndMemberId(final Long reservationId, final Long memberId) {
        validateWaitingOwner(reservationId, memberId);
        waitingRepository.deleteByReservationId(reservationId, memberId);
    }

    public void validateWaitingOwner(final Long reservationId, final Long memberId) {
        boolean doesExists = waitingRepository.existsByReservationIdAndMemberId(reservationId, memberId);
        if (!doesExists) {
            throw new WaitingOwnerException("자신의 예약 대기가 아닙니다.");
        }
    }
}
