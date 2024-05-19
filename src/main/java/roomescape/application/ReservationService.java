package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationFactory;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Status;
import roomescape.dto.AdminReservationRequest;
import roomescape.dto.LoginMember;
import roomescape.dto.MyReservationResponse;
import roomescape.dto.ReservationCriteria;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class ReservationService {
    private final ReservationFactory reservationFactory;
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationFactory reservationFactory, ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationFactory = reservationFactory;
    }

    @Transactional
    public ReservationResponse saveByClient(LoginMember loginMember, ReservationRequest reservationRequest) {
        Reservation reservation = reservationFactory.create(
                loginMember.id(),
                reservationRequest.date(),
                reservationRequest.timeId(),
                reservationRequest.themeId()
        );
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    public ReservationResponse saveByAdmin(AdminReservationRequest adminReservationRequest) {
        Reservation reservation = reservationFactory.create(
                adminReservationRequest.memberId(),
                adminReservationRequest.date(),
                adminReservationRequest.timeId(),
                adminReservationRequest.themeId()
        );
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public void deleteById(long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(RoomescapeErrorCode.NOT_FOUND_RESERVATION,
                        String.format("존재하지 않는 예약입니다. 요청 예약 id:%d", id)));
        reservationRepository.deleteById(reservation.getId());
        updateReservation(reservation);
    }

    private void updateReservation(Reservation reservation) {
        if (reservation.getStatus() == Status.RESERVATION) {
            Reservation nextReservation = reservationRepository.findByDateAndTimeIdAndThemeId(
                    reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId()
            ).orElseThrow();
            nextReservation.setStatus(Status.RESERVATION);
        }
    }

    public List<ReservationResponse> findAllByStatus(Status status) {
        List<Reservation> reservations = reservationRepository.findAllByStatus(status);
        return convertToReservationResponses(reservations);
    }

    private List<ReservationResponse> convertToReservationResponses(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findByCriteria(ReservationCriteria reservationCriteria) {
        Long themeId = reservationCriteria.themeId();
        Long memberId = reservationCriteria.memberId();
        LocalDate dateFrom = reservationCriteria.dateFrom();
        LocalDate dateTo = reservationCriteria.dateTo();
        return reservationRepository.findByCriteria(themeId, memberId, dateFrom, dateTo).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findMyReservations(Long memberId) {
        return reservationRepository.findAllByMemberIdOrderByDateAsc(memberId).stream()
                .map(reservation -> new MyReservationResponse(reservation.getId(), reservation.getTheme().getName(),
                        reservation.getDate(), reservation.getTime().getStartAt(), getWaitingOrder(reservation)))
                .toList();
    }

    private String getWaitingOrder(Reservation reservation) {
        if (reservation.getStatus() == Status.RESERVATION) {
            return reservation.getStatus().getValue();
        }
        return reservationRepository.countByOrder(
                reservation.getId(), reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId()
        ) + "번째 " + reservation.getStatus().getValue();
    }
}
