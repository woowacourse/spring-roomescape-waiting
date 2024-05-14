package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.admin.domain.FilterInfo;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.admin.dto.ReservationFilterRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.time.domain.Time;
import roomescape.time.repository.TimeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeRepository timeRepository;

    public ReservationService(ReservationRepository reservationRepository, TimeRepository timeRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
    }

    public ReservationResponse addReservation(ReservationRequest reservationRequest, long memberId) {
        Reservation reservation = reservationRequest.fromRequest(memberId);
        return ReservationResponse.fromReservation(reservationRepository.save(reservation));
    }

    public void addAdminReservation(AdminReservationRequest adminReservationRequest) {
        Reservation reservation = adminReservationRequest.fromRequest();
        ReservationResponse.fromReservation(reservationRepository.save(reservation));
    }


    public List<ReservationResponse> findReservations() {
        List<Reservation> reservations = reservationRepository.findAllReservationOrderByDateAndTimeStartAt();

        return reservations.stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public List<ReservationTimeAvailabilityResponse> findTimeAvailability(long themeId, LocalDate date) {
        List<Time> allTimes = timeRepository.findAllReservationTimes();
        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndDate(themeId, date);
        List<Time> bookedTimes = extractReservationTimes(reservations);

        return allTimes.stream()
                .map(time -> ReservationTimeAvailabilityResponse.fromTime(time, isTimeBooked(time, bookedTimes)))
                .toList();
    }

    private List<Time> extractReservationTimes(List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::getReservationTime)
                .toList();
    }

    private boolean isTimeBooked(Time time, List<Time> bookedTimes) {
        return bookedTimes.contains(time);
    }

    public void removeReservations(long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public List<ReservationResponse> findFilteredReservations(ReservationFilterRequest reservationFilterRequest) {
        FilterInfo filterInfo = reservationFilterRequest.toFilterInfo();

        return reservationRepository.findAllByUserIdAndThemeIdBetweenDate(filterInfo).stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

}
