package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.ReservationTimeErrorCode;
import roomescape.common.exception.code.ThemeErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.dto.command.CreateReservationTimeCommand;
import roomescape.dto.response.CreateReservationTimeResponse;
import roomescape.dto.response.ReservationTimeResponse;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public CreateReservationTimeResponse createReservationTime(CreateReservationTimeCommand command) {
        ReservationTime reservationTime = ReservationTime.createWithoutId(command.startAt());
        ReservationTime newReservationTime = reservationTimeRepository.save(reservationTime);
        return CreateReservationTimeResponse.from(newReservationTime);
    }

    public List<ReservationTimeResponse> getReservationTimes(Long themeId, LocalDate date) {
        validateTheme(themeId);

        List<Reservation> reservations = reservationRepository.findByThemeIdAndDate(themeId, date);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(time -> ReservationTimeResponse.from(time, time.isNotReserved(reservations)))
                .toList();
    }

    @Transactional
    public void deleteReservationTime(long reservationTimeId) {
        Optional<ReservationTime> reservationTime = reservationTimeRepository.findById(reservationTimeId);
        if (reservationTime.isEmpty()) {
            throw new RoomEscapeException(ReservationTimeErrorCode.NOT_FOUND);
        }

        validateTimeIncludeReservation(reservationTimeId);
        reservationTimeRepository.deleteById(reservationTimeId);
    }

    private void validateTheme(Long themeId) {
        boolean exists = themeRepository.existsById(themeId);
        if (!exists) {
            throw new RoomEscapeException(ThemeErrorCode.NOT_FOUND);
        }
    }

    private void validateTimeIncludeReservation(long reservationTimeId) {
        boolean existsByTimeId = reservationRepository.existsById(reservationTimeId);
        if (existsByTimeId) {
            throw new RoomEscapeException(ReservationTimeErrorCode.RESERVATION_TIME_CANNOT_DELETE);
        }
    }
}
