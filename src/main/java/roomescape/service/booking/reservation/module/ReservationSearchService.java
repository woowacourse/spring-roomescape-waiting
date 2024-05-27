package roomescape.service.booking.reservation.module;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.reservation.ReservationFilter;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.UserReservationResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;

@Service
public class ReservationSearchService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public ReservationSearchService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse findReservation(Long reservationId) {
        Reservation reservation = findReservationById(reservationId);
        return ReservationResponse.from(reservation);
    }

    public List<ReservationResponse> findAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findReservationsByFilter(ReservationFilter filter) {
        List<Reservation> reservations = reservationRepository.findByMemberOrThemeOrDateRange(
                filter.getMemberId(),
                filter.getThemeId(),
                filter.getStartDate(),
                filter.getEndDate()
        );

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<UserReservationResponse> findReservationByMemberId(Long memberId) {
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(this::createUserReservationResponse)
                .toList();
    }

    private UserReservationResponse createUserReservationResponse(Reservation reservation) {
        if (reservation.isReserved()) {
            return UserReservationResponse.creat(reservation);
        }

        Waiting waiting = findWaitingByReservationId(reservation.getId());
        return UserReservationResponse.createByWaiting(waiting);
    }

    private Reservation findReservationById(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 예약 정보 입니다.",
                        new Throwable("reservation_id : " + reservationId)
                ));
    }

    private Waiting findWaitingByReservationId(Long reservationId) {
        return waitingRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 예약 정보와 일치하는 대기 정보가 존재하지 않습니다.",
                        new Throwable("reservation_id : " + reservationId)
                ));
    }
}
