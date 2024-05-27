package roomescape.reservation.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.dto.request.WaitingReservationSaveRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.WaitingResponse;
import roomescape.reservation.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class WaitingReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationFactoryService reservationFactoryService;

    public WaitingReservationService(
            ReservationRepository reservationRepository,
            ReservationFactoryService reservationFactoryService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationFactoryService = reservationFactoryService;
    }

    @Transactional
    public ReservationResponse save(WaitingReservationSaveRequest saveRequest) {
        Reservation reservation = reservationFactoryService.createWaiting(saveRequest);
        validateMemberReservationUnique(reservation);
        validateWaitingAvailable(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.toResponse(savedReservation);
    }

    private void validateMemberReservationUnique(Reservation reservation) {
        Optional<Reservation> duplicatedReservation = reservationRepository.findFirstByDateAndReservationTimeAndThemeAndMember(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getMember()
        );
        duplicatedReservation.ifPresent(this::throwExceptionByStatus);
    }

    private void throwExceptionByStatus(Reservation memberReservation) {
        if (memberReservation.isWaitingReservation()) {
            throw new IllegalArgumentException("이미 회원이 예약 대기한 내역이 있습니다.");
        }
        if (memberReservation.isSuccessReservation()) {
            throw new IllegalArgumentException("이미 회원이 예약한 내역이 있습니다.");
        }
    }

    private void validateWaitingAvailable(Reservation reservation) {
        Optional<Reservation> savedReservation = reservationRepository.findFirstByDateAndReservationTimeAndTheme(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
        if (savedReservation.isEmpty()) {
            throw new IllegalArgumentException("추가된 예약이 없습니다. 예약을 추가해 주세요.");
        }
    }

    public List<WaitingResponse> findAll() {
        return reservationRepository.findAllByStatus(Status.WAIT)
                .stream()
                .map(WaitingResponse::toResponse)
                .toList();
    }

    @Transactional
    public void approveReservation(Long id) {
        Reservation waitingReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약 대기 내역이 없습니다."));
        validateUniqueReservation(waitingReservation);
        waitingReservation.updateSuccessStatus();
    }

    private void validateUniqueReservation(Reservation reservation) {
        Optional<Reservation> savedReservation = reservationRepository.findFirstByDateAndReservationTimeAndThemeAndStatus(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                Status.SUCCESS
        );
        if (savedReservation.isPresent()) {
            throw new IllegalArgumentException("이미 확정된 예약이 있습니다.");
        }
    }
}
