package roomescape.service.reservationtime;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.dto.reservationtime.AvailableTimeResponse;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.exception.reservationtime.ReservationTimeAlreadyExistsException;
import roomescape.exception.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.reservationtime.UsingReservationTimeException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;

@Service
public class ReservationTimeServiceImpl implements ReservationTimeService {
    private final ReservationTimeRepository timeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeServiceImpl(ReservationTimeRepository timeRepository,
                                      ReservationRepository reservationRepository) {
        this.timeRepository = timeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponse create(ReservationTimeRequest request) {

        if (timeRepository.existsByStartAt(request.startAt())) {
            throw new ReservationTimeAlreadyExistsException();
        }

        ReservationTime newReservationTime = new ReservationTime(request.startAt());
        return ReservationTimeResponse.from(timeRepository.add(newReservationTime));
    }

    public List<ReservationTimeResponse> getAll() {
        return ReservationTimeResponse.from(timeRepository.findAll());
    }

    public void deleteById(Long id) {

        if (isReservationExists(id)) {
            throw new UsingReservationTimeException();
        }

        int affectedCount = timeRepository.deleteById(id);
        if (affectedCount != 1) {
            throw new ReservationTimeNotFoundException(id);
        }
    }

    private boolean isReservationExists(Long id) {
        return reservationRepository.existsByTimeId(id);
    }

    public List<AvailableTimeResponse> getAvailableTimes(LocalDate date, Long themeId) {

        List<Long> bookedTimeIds = reservationRepository.findTimeIdsByDateAndTheme(date, themeId);
        List<ReservationTime> reservationTimes = timeRepository.findAll();

        List<AvailableTimeResponse> availableTimeResponses = reservationTimes.stream()
                .map(reservationTime -> {
                    boolean alreadyBooked = bookedTimeIds.contains(reservationTime.getId());
                    return AvailableTimeResponse.from(reservationTime, alreadyBooked);
                })
                .toList();

        return availableTimeResponses;
    }
}
