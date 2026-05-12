package roomescape.facade;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.Reservations;
import roomescape.dto.ReservationRequest;
import roomescape.dto.TimeWithStatusResponse;
import roomescape.service.ReservationService;
import roomescape.domain.ReservationTime;
import roomescape.service.ReservationTimeService;
import roomescape.domain.Theme;
import roomescape.service.ThemeService;

import java.time.LocalDate;
import java.util.List;

@Component
public class ReservationFacade {

    private static final String CANNOT_DELETE_TIME_IN_USE = "ID %d번 시간을 사용 중인 예약이 존재하여 시간을 삭제할 수 없습니다.";
    private static final String CANNOT_DELETE_THEME_IN_USE = "ID %d번 테마를 사용 중인 예약이 존재하여 테마를 삭제할 수 없습니다.";
    private static final String ALREADY_EXISTS_ADD_RESERVATION = "해당 날짜와 시간, 테마에 이미 예약이 존재합니다.";

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ReservationFacade(
            ReservationService reservationService,
            ReservationTimeService reservationTimeService,
            ThemeService themeService
    ) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @Transactional
    public void deleteTime(Long id) {
        if (reservationService.hasReservationsByTimeId(id)) {
            throw new IllegalArgumentException(String.format(CANNOT_DELETE_TIME_IN_USE, id));
        }
        reservationTimeService.deleteTime(id);
    }

    @Transactional
    public void deleteTheme(Long id) {
        if (reservationService.hasReservationsByThemeId(id)) {
            throw new IllegalArgumentException(String.format(CANNOT_DELETE_THEME_IN_USE, id));
        }
        themeService.deleteTheme(id);
    }

    @Transactional
    public Reservation addReservation(ReservationRequest request) {
        ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());

        Reservations existing = reservationService.findByDateAndThemeId(request.date(), theme.getId());
        if (existing.isOccupied(reservationTime)) {
            throw new IllegalArgumentException(ALREADY_EXISTS_ADD_RESERVATION);
        }

        return reservationService.addReservation(
                new Reservation(
                        request.name(),
                        request.date(),
                        reservationTime,
                        theme
                ));
    }

    public List<TimeWithStatusResponse> getTimesWithAvailability(LocalDate date, Long themeId) {
        List<ReservationTime> times = reservationTimeService.getReservationTimes();
        Reservations reservations = reservationService.findByDateAndThemeId(date, themeId);

        return times.stream()
                .map(time -> TimeWithStatusResponse.from(time, reservations.isOccupied(time)))
                .toList();
    }
}
