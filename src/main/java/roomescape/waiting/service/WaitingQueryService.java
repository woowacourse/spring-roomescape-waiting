package roomescape.waiting.service;

import java.time.LocalDate;
import java.util.Comparator;
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
import roomescape.reservation.dto.ReservationWithRank;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.waiting.controller.response.WaitingInfoResponse;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WaitingQueryService {

    private static final ReservationStatus WAITING = ReservationStatus.WAITING;
    private final ReservationRepository reservationRepository;

    public Page<WaitingInfoResponse> getAllInfo(Pageable pageable) {
        return reservationRepository.findByStatus(WAITING, pageable)
                .map(WaitingInfoResponse::from);
    }

    public List<MyReservationResponse> getMyWaitingReservations(Long memberId) {
        List<Reservation> myWaitings = reservationRepository.findByMemberIdAndStatus(memberId, WAITING);

        List<ReservationWithRank> responses = myWaitings.stream()
                .map(this::calculateRankForReservation)
                .toList();

        return responses.stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    private ReservationWithRank calculateRankForReservation(Reservation myWaiting) {
        List<Reservation> waitings = reservationRepository.findByDateAndTimeIdAndStatus(
                myWaiting.getDate(), myWaiting.getTimeId(), WAITING);

        waitings.sort(Comparator.comparingLong(Reservation::getId));

        for (int i = 0; i < waitings.size(); i++) {
            Reservation waiting = waitings.get(i);
            if (waiting.getId().equals(myWaiting.getId())) {
                return new ReservationWithRank(waiting, i + 1);
            }
        }

        throw new NotFoundException("예약 대기를 찾을 수 없습니다.");
    }

    public Reservation getWaiting(Long id) {
        return reservationRepository.findByIdAndStatus(id, ReservationStatus.WAITING)
                .orElseThrow(() -> new NotFoundException("예약 대기를 찾을 수 없습니다."));
    }

    public Reservation getFirstByDateAndTimeId(LocalDate date, Long timeId) {
        return reservationRepository.findByDateAndTimeIdAndStatus(date, timeId, WAITING).getFirst();
    }

    public boolean existWaiting(Long userId, LocalDate date, Long timeId) {
        return reservationRepository.existsByMemberIdAndDateAndTimeIdAndStatus(userId, date, timeId, WAITING);
    }

    public boolean existsByDateAndTimeId(LocalDate date, Long timeId) {
        return reservationRepository.existsByDateAndTimeIdAndStatus(date, timeId, WAITING);
    }
}
