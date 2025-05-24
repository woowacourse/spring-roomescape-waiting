package roomescape.reservationtime;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeConflictException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeNotExistsThemeException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeNotFoundException;
import roomescape.exception.custom.reason.reservationtime.ReservationTimeUsedException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@AllArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeResponse create(final ReservationTimeRequest request) {
        validateDuplicate(request);

        final ReservationTime reservationTime = new ReservationTime(request.startAt());
        final ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAllAvailable(final Long themeId, final LocalDate date) {
        final List<ReservationTime> times = reservationTimeRepository.findAll();
        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(ReservationTimeNotExistsThemeException::new);
        final ReservationDate queryDate = ReservationDate.fromQuery(date);

        final Set<ReservationTime> reservationTimesByThemeAndDate = reservationRepository.findAllByThemeAndDate(
                        theme,
                        queryDate).stream()
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
        final ReservationTime reservationTime = reservationTimeRepository.findById(id)
                .orElseThrow(ReservationTimeNotFoundException::new);

        if (reservationRepository.existsByReservationTime(reservationTime)) {
            throw new ReservationTimeUsedException();
        }

        reservationTimeRepository.delete(reservationTime);
    }

    private void validateDuplicate(final ReservationTimeRequest request) {
        if (reservationTimeRepository.existsByStartAt(request.startAt())) {
            throw new ReservationTimeConflictException();
        }
    }
}
