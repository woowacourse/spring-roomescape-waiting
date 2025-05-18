package roomescape.service.reservationtime;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.dto.reservationtime.AvailableTimeResponse;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.exception.reservationtime.ReservationTimeAlreadyExistsException;
import roomescape.exception.reservationtime.UsingReservationTimeException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;

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

    private boolean isReservationExists(Long id) {
        return reservationRepository.existsByTimeId(id);
    }

    public List<AvailableTimeResponse> getAvailableTimes(LocalDate date, Long themeId) {

        List<Long> bookedReservationTimesId = reservationRepository.findAllByDateAndThemeId(date, themeId).stream()
                .map(reservation -> reservation.getTime().getId())
                .toList();
        List<ReservationTime> reservationTimes = timeRepository.findAll();

        List<AvailableTimeResponse> availableTimeResponses = reservationTimes.stream()
                .map(reservationTime -> {
                    boolean alreadyBooked = bookedReservationTimesId.contains(reservationTime.getId());
                    return AvailableTimeResponse.from(reservationTime, alreadyBooked);
                })
                .toList();

        return availableTimeResponses;
    }
}
