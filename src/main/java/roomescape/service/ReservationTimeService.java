package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Status;
import roomescape.exception.BadRequestException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.request.ReservationAvailabilityTimeRequest;
import roomescape.service.dto.request.ReservationTimeRequest;
import roomescape.service.dto.response.ReservationAvailabilityTimeResponse;
import roomescape.service.dto.response.ReservationTimeResponse;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse save(ReservationTimeRequest reservationTimeRequest) {
        ReservationTime reservationTime = reservationTimeRequest.toReservationTime();

        validateDuplication(reservationTime);

        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    private void validateDuplication(ReservationTime reservationTime) {
        reservationTimeRepository.findByStartAt(reservationTime.getStartAt())
                .ifPresent(reservationTime::validateDuplicatedTime);
    }

    public void deleteById(long id) {
        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationAvailabilityTimeResponse> findReservationAvailabilityTimes(
            ReservationAvailabilityTimeRequest timeRequest) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<Reservation> reservations = reservationRepository.findByThemeIdAndStatusIn(
                        timeRequest.themeId(),
                        List.of(Status.CREATED, Status.WAITING))
                .orElseThrow(() -> new BadRequestException("예약을 찾을 수 없습니다."));

        return reservationTimes.stream()
                .map(reservationTime -> {
                    boolean isBooked = isReservationBooked(reservations, timeRequest.date(), reservationTime);
                    return ReservationAvailabilityTimeResponse.from(reservationTime, isBooked);
                })
                .toList();
    }

    private boolean isReservationBooked(
            List<Reservation> reservations,
            LocalDate date,
            ReservationTime reservationTime) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.hasDateTime(date, reservationTime));
    }
}
