package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ServiceReservationCreateRequest;
import roomescape.service.dto.request.ServiceReservationUpdateRequest;
import roomescape.service.dto.response.ServiceReservationResponse;

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
    public ServiceReservationResponse create(ServiceReservationCreateRequest request) {
        ReservationTime reservationTime = readReservationTime(request.timeId());
        Theme theme = readTheme(request.themeId());

        Reservation reservationWithoutId = request.toEntity(reservationTime, theme);
        validateCreateReservation(reservationWithoutId);

        Reservation reservation = reservationRepository.create(reservationWithoutId);

        return ServiceReservationResponse.from(reservation);
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
        validateDuplicatedReservation(reservation);
    }

    private void validatePastReservation(Reservation reservation, ErrorCode errorCode) {
        if (reservation.isPast(LocalDateTime.now(clock))) {
            throw new CustomInvalidRequestException(errorCode);
        }
    }

    private void validateDuplicatedReservation(Reservation reservation) {
        if (reservationRepository.existByDateAndTimeIdAndThemeId(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId())) {
            throw new CustomInvalidRequestException(ErrorCode.DUPLICATED_RESERVATION);
        }
    }

    public List<ServiceReservationResponse> readByName(String name) {
        List<Reservation> reservations = reservationRepository.readByName(name);

        return reservations.stream()
                .map(ServiceReservationResponse::from)
                .toList();
    }

    public List<ServiceReservationResponse> readAll() {
        List<Reservation> reservations = reservationRepository.readAll();

        return reservations.stream()
                .map(ServiceReservationResponse::from)
                .toList();
    }

    @Transactional
    public ServiceReservationResponse update(Long id, ServiceReservationUpdateRequest request) {
        Reservation beforeReservation = readReservation(id);
        validatePastReservation(beforeReservation, ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_UPDATE);

        ReservationTime reservationTime = readReservationTime(request.timeId());

        Reservation newReservation = request.toEntity(beforeReservation, reservationTime);

        boolean sameWithBefore = beforeReservation.getDate() == request.date()
                && beforeReservation.getTime().getId().equals(request.timeId());
        if (!sameWithBefore) {
            validateDuplicatedReservation(newReservation);
        }
        validatePastReservation(newReservation, ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_CREATE);

        reservationRepository.update(id, request.date(), request.timeId());

        return ServiceReservationResponse.from(newReservation);
    }

    private Reservation readReservation(Long reservationId) {
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
}
