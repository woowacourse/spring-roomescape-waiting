package roomescape.service;

import static roomescape.exception.ExceptionType.DELETE_USED_TIME;
import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION_TIME;
import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.domain.ReservationTimes;
import roomescape.domain.Reservations;
import roomescape.dto.AvailableTimeResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@Service
public class ReservationTimeService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeService(ReservationRepository reservationRepository,
                                  ReservationTimeRepository reservationTimeRepository,
                                  ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationTimeResponse save(ReservationTimeRequest reservationTimeRequest) {
        if (reservationTimeRepository.existsByStartAt(reservationTimeRequest.startAt())) {
            throw new RoomescapeException(DUPLICATE_RESERVATION_TIME);
        }
        ReservationTime beforeSavedReservationTime = reservationTimeRequest.toReservationTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(beforeSavedReservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    public List<ReservationTimeResponse> findAll() {
        return new ReservationTimes(reservationTimeRepository.findAll()).getReservationTimes().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableTimeResponse> findByThemeAndDate(LocalDate date, long themeId) {
        Theme requestedTheme = themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));
        Reservations findReservations = new Reservations(reservationRepository.findByThemeAndDate(requestedTheme, date));

        return new ReservationTimes(reservationTimeRepository.findAll()).getReservationTimes().stream()
                .map(reservationTime -> AvailableTimeResponse.of(reservationTime, findReservations))
                .toList();
    }

    public void delete(long timeId) {
        if (isUsedTime(timeId)) {
            throw new RoomescapeException(DELETE_USED_TIME);
        }
        reservationTimeRepository.deleteById(timeId);
    }

    private boolean isUsedTime(long timeId) {
        return reservationRepository.existsByTimeId(timeId);
    }
}
