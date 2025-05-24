package roomescape.reservation.application.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.application.exception.ReservationTimeAlreadyExistsException;
import roomescape.reservation.application.exception.UsingReservationTimeException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.presentation.dto.AvailableTimeResponse;
import roomescape.reservation.presentation.dto.ReservationTimeRequest;
import roomescape.reservation.presentation.dto.ReservationTimeResponse;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository timeRepository,
                                  ReservationRepository reservationRepository) {
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponse create(ReservationTimeRequest request) {

        if (timeRepository.existsByStartAt(request.startAt())) {
            throw new ReservationTimeAlreadyExistsException();
        }

        ReservationTime newReservationTime = new ReservationTime(request.startAt());
        return ReservationTimeResponse.from(timeRepository.save(newReservationTime));
    }

    public List<ReservationTimeResponse> getAll() {
        return ReservationTimeResponse.from(timeRepository.findAll());
    }

    public void deleteById(Long id) {

        if (isReservationExists(id)) {
            throw new UsingReservationTimeException();
        }
        timeRepository.deleteById(id);
    }

    public List<AvailableTimeResponse> getAvailableTimes(LocalDate date, Long themeId) {

        List<Long> bookedReservationTimesId = reservationRepository.findAllByDateAndThemeId(date, themeId).stream()
                .map(reservation -> reservation.getTime().getId())
                .toList();
        List<ReservationTime> reservationTimes = timeRepository.findAll();

        return reservationTimes.stream()
                .map(reservationTime -> {
                    boolean alreadyBooked = bookedReservationTimesId.contains(reservationTime.getId());
                    return AvailableTimeResponse.from(reservationTime, alreadyBooked);
                })
                .toList();
    }

    private boolean isReservationExists(Long id) {
        return reservationRepository.existsByTimeId(id);
    }
}
