package roomescape.reservationtime;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeConflictException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeUsedException;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepository;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationTimeService(
            final ReservationTimeRepository reservationTimeRepository,
            final ReservationRepository reservationRepository
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponse create(final ReservationTimeRequest request) {
        validateDuplicateTime(request);

        final ReservationTime reservationTime = new ReservationTime(request.startAt());
        final Long id = reservationTimeRepository.save(reservationTime);
        final ReservationTime savedReservationTime = reservationTimeRepository.findById(id);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAllAvailableTimes(final Long themeId, final LocalDate date) {
        final List<ReservationTime> times = reservationTimeRepository.findAll();
        final Set<ReservationTime> reservationTimesByThemeAndDate = reservationRepository.findAllByThemeIdAndDate(themeId, date).stream()
                .map(Reservation::getReservationTime)
                .collect(Collectors.toSet());

        return times.stream()
                .map(reservationTime ->
                        AvailableReservationTimeResponse.from(
                                reservationTime,
                                reservationTimesByThemeAndDate.contains(reservationTime)
                        )
                )
                .toList();
    }

    public void deleteById(final Long id) {
        validateExistsReservationTime(id);
        validateUnusedReservationTime(id);

        reservationTimeRepository.deleteById(id);
    }

    private void validateUnusedReservationTime(final Long id) {
        if (reservationRepository.existsByReservationTime(id)) {
            throw new ReservationTimeUsedException();
        }
    }

    private void validateExistsReservationTime(final Long id) {
        if (!reservationTimeRepository.existsById(id)) {
            throw new ReservationTimeNotFoundException();
        }
    }

    private void validateDuplicateTime(final ReservationTimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new ReservationTimeConflictException();
        }
    }
}
