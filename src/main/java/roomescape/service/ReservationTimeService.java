package roomescape.service;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.handler.exception.CustomException;
import roomescape.handler.exception.ExceptionCode;
import roomescape.service.dto.request.AvailableTimeRequest;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.response.AvailableTimeResponse;
import roomescape.service.dto.response.ReservationTimeResponse;

@Service
public class ReservationTimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationRepository reservationRepository, ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTimeResponse createReservationTime(ReservationTimeRequest reservationTimeRequest) {
        ReservationTime reservationTime = reservationTimeRequest.toEntity();

        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    public List<ReservationTimeResponse> findAllReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAllReservationTimes();
        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableTimeResponse> findAvailableTimes(AvailableTimeRequest availableTimeRequest) {
        List<Reservation> reservations = reservationRepository.findReservationsByDateAndThemeId(
                availableTimeRequest.date(),
                availableTimeRequest.themeId()
        );

        List<ReservationTime> reservationTimes = reservationTimeRepository.findAllReservationTimes();

        return reservationTimes.stream()
                .map(reservationTime ->  AvailableTimeResponse. of(
                        reservationTime,
                        isReservedTime(reservations, reservationTime)
                ))
                .toList();
    }

    private boolean isReservedTime(List<Reservation> reservations, ReservationTime reservationTime) {
        return reservations.stream()
                .noneMatch(reservation -> reservation.isSameTime(reservationTime));
    }

    public void deleteReservationTime(Long id) {
        try {
            reservationTimeRepository.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ExceptionCode.TIME_IN_USE);
        }
    }
}
