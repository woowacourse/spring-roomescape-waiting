package roomescape.reservationwaiting.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.business.BusinessException;
import roomescape.reservationwaiting.domain.ReservationWaiting;
import roomescape.reservationwaiting.repository.ReservationWaitingRepository;
import roomescape.reservationwaiting.repository.WaitingWithTurn;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Transactional
    public void deleteWaiting(Long id, Long memberId) {
        ReservationWaiting waiting = reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 대기입니다."));
        if (!waiting.isOwnedBy(memberId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        if (waiting.isPast()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 지난 시간에는 대기를 취소할 수 없습니다.");
        }
        reservationWaitingRepository.deleteById(id);
    }

    public List<WaitingWithTurn> getWaitingByMemberId(Long memberId) {
        return reservationWaitingRepository.findWithTurnByMemberId(memberId);
    }

    public List<ReservationWaiting> getAllWaitings() {
        return reservationWaitingRepository.findAll();
    }

    @Transactional
    public void deleteWaitingByAdmin(Long id) {
        if (!reservationWaitingRepository.existsById(id)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 대기입니다.");
        }
        reservationWaitingRepository.deleteById(id);
    }
}
