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
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationRepositoryFacade;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepositoryFacade;

@Service
@AllArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepositoryFacade reservationTimeRepositoryFacade;
    private final ReservationRepositoryFacade reservationRepositoryFacade;
    private final ThemeRepositoryFacade themeRepositoryFacade;

    public ReservationTimeResponse create(final ReservationTimeRequest request) {
        validateDuplicate(request);

        final ReservationTime reservationTime = new ReservationTime(request.startAt());
        final ReservationTime savedReservationTime = reservationTimeRepositoryFacade.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepositoryFacade.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAllAvailable(final Long themeId, final LocalDate date) {
        final List<ReservationTime> times = reservationTimeRepositoryFacade.findAll();
        final Theme theme = themeRepositoryFacade.findById(themeId)
                .orElseThrow(ReservationTimeNotExistsThemeException::new);

        final Set<ReservationTime> reservationTimesByThemeAndDate = reservationRepositoryFacade.findAllByThemeAndDate(
                        theme,
                        date).stream()
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
        final ReservationTime reservationTime = reservationTimeRepositoryFacade.findById(id)
                .orElseThrow(ReservationTimeNotFoundException::new);

        if (reservationRepositoryFacade.existsByReservationTime(reservationTime)) {
            throw new ReservationTimeUsedException();
        }

        reservationTimeRepositoryFacade.delete(reservationTime);
    }

    private void validateDuplicate(final ReservationTimeRequest request) {
        if (reservationTimeRepositoryFacade.existsByStartAt(request.startAt())) {
            throw new ReservationTimeConflictException();
        }
    }
}
