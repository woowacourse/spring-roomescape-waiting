package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
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
    public Reservation save(ServiceReservationCreateRequest request) {
        ReservationTime reservationTime = findReservationTime(request.timeId());
        Theme theme = findTheme(request.themeId());

        Reservation reservationWithoutId = request.toReservation(reservationTime, theme);
        return reservationRepository.save(reservationWithoutId);
    }

    private ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_RESERVATION_TIME));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_THEME));
    }

    public List<ServiceReceptionResponse> findByName(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);

        return reservations.stream()
                .map(reservation -> ServiceReceptionResponse.of(reservation, 0L, ReservationStatus.CONFIRMED.name()))
                .toList();
    }

    public List<ServiceReceptionResponse> findAll() {
        List<Reservation> reservations = reservationRepository.findAll();

        return reservations.stream()
                .map(reservation -> ServiceReceptionResponse.of(reservation, 0L, ReservationStatus.CONFIRMED.name()))
                .toList();
    }

    public Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_RESERVATION));
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.delete(id);
    }

    public Optional<Reservation> findBySlot(LocalDate date, Long timeId, Long themeId) {
        return reservationRepository.findBySlot(date, timeId, themeId);
    }
}
