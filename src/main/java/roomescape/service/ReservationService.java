package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ServiceReservationCreateRequest;
import roomescape.service.dto.response.ServiceReceptionResponse;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              Clock clock) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    @Transactional
    public Reservation create(ServiceReservationCreateRequest request) {
        ReservationTime reservationTime = readReservationTime(request.timeId());
        Theme theme = readTheme(request.themeId());

        Reservation reservationWithoutId = request.toReservation(reservationTime, theme);
        validateCreateReservation(reservationWithoutId);

        return reservationRepository.create(reservationWithoutId);
    }

    private ReservationTime readReservationTime(Long timeId) {
        return reservationTimeRepository.read(timeId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_RESERVATION_TIME));
    }

    private Theme readTheme(Long themeId) {
        return themeRepository.read(themeId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_THEME));
    }

    private void validateCreateReservation(Reservation reservation) {
        validatePastReservation(reservation, ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_CREATE);
    }

    private void validatePastReservation(Reservation reservation, ErrorCode errorCode) {
        if (reservation.isPast(LocalDateTime.now(clock))) {
            throw new CustomInvalidRequestException(errorCode);
        }
    }

    public List<ServiceReceptionResponse> readByName(String name) {
        List<Reservation> reservations = reservationRepository.readByName(name);

        return reservations.stream()
                .map(reservation -> ServiceReceptionResponse.of(reservation, 0L, ReservationStatus.CONFIRMED.name()))
                .toList();
    }

    public List<ServiceReceptionResponse> readAll() {
        List<Reservation> reservations = reservationRepository.readAll();

        return reservations.stream()
                .map(reservation -> ServiceReceptionResponse.of(reservation, 0L, ReservationStatus.CONFIRMED.name()))
                .toList();
    }

    public Reservation readReservation(Long reservationId) {
        return reservationRepository.readById(reservationId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_RESERVATION));
    }

    @Transactional
    public void delete(Long id) {
        Reservation reservation = readReservation(id);
        if (reservation.isPast(LocalDateTime.now(clock))) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_DELETE);
        }

        reservationRepository.delete(id);
    }

    public Optional<Reservation> readBySlot(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.readBySlot(date, timeId, themeId);
    }
}
