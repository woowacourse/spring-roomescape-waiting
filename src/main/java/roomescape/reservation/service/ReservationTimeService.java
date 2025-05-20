package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.ExistedReservationException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ReservationTimeRequest;
import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.reservation.dto.response.TimeWithBookedResponse;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationTimeRepository;
import roomescape.reservation.infrastructure.ThemeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository, ThemeRepository themeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationTimeResponse> findAllTimes() {
        List<ReservationTime> reservationTimeDaoAll = reservationTimeRepository.findAll();

        return reservationTimeDaoAll.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse createTime(ReservationTimeRequest reservationTimeRequest) {
        ReservationTime reservationTime = reservationTimeRequest.toTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    public void deleteTimeById(Long id) {
        if (reservationTimeRepository.findById(id).isEmpty()) {
            throw new ReservationTimeNotFoundException();
        }
        if (reservationRepository.findByReservationTimeId(id).size() > 0) {
            throw new ExistedReservationException();
        }
        reservationTimeRepository.deleteById(id);
    }

    public List<TimeWithBookedResponse> findTimesByDateAndThemeIdWithBooked(LocalDate date, Long themeId) {
        Theme theme = themeRepository.findById(themeId).orElseThrow(ThemeNotFoundException::new);
        List<Reservation> reservations = reservationRepository.findByDateAndTheme(date, theme);

        List<ReservationTime> bookedReservationTimes = reservations.stream()
                .map(Reservation::getReservationTime)
                .toList();
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(time -> TimeWithBookedResponse.of(time, bookedReservationTimes.contains(time)))
                .toList();
    }
}
