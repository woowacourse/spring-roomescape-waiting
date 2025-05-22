package roomescape.waiting.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationStatusRepository;
import roomescape.waiting.controller.response.WaitingInfoResponse;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WaitingQueryService {

    private static final ReservationStatus WAITING = ReservationStatus.WAITING;
    private final ReservationStatusRepository statusRepository;

    public Page<WaitingInfoResponse> getAllInfo(Pageable pageable) {
        return statusRepository.findByStatus(WAITING, pageable)
                .map(WaitingInfoResponse::from);
    }

    public List<MyReservationResponse> getWaitingReservations(Long memberId) {
        return statusRepository.findWithRankByMemberIdAndStatus(memberId, WAITING)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    public Reservation getWaiting(Long id) {
        return statusRepository.findByIdAndStatus(id, ReservationStatus.WAITING)
                .orElseThrow(() -> new NotFoundException("예약 대기를 찾을 수 없습니다."));
    }

    public boolean existWaiting(Long userId, LocalDate date, Long timeId) {
        return statusRepository.existsByMemberIdAndDateAndTimeIdAndStatus(userId, date, timeId, WAITING);
    }
}
