package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequestDTO;
import roomescape.dto.ReservationResponseDTO;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.dto.ReservedTimeResponseDTO;
import roomescape.dto.UserBookingResponseDTO;
import roomescape.dto.WaitingResponseDTO;
import roomescape.exception.ReservationErrorCode;
import roomescape.exception.ReservationTimeErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.exception.ThemeErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@Service
public class ReservationService {

    private final Clock clock;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(Clock clock, ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              WaitingRepository waitingRepository) {
        this.clock = clock;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public ReservationResponseDTO addReservation(ReservationRequestDTO reservationRequestDTO) {
        ReservationTime time = reservationTimeRepository.findById(reservationRequestDTO.timeId())
                .orElseThrow(() -> new RoomEscapeException(
                        ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND));
        Theme theme = themeRepository.findById(reservationRequestDTO.themeId())
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND));

        LocalDate date = reservationRequestDTO.date();

        validateDuplicateReservation(date, time, theme);

        Reservation reservation = Reservation.create(reservationRequestDTO.name(),
                date, time, theme);
        reservation.validateNotPastTime(LocalDateTime.now(clock));

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponseDTO.from(savedReservation);
    }

    private void validateDuplicateReservation(LocalDate date, ReservationTime time, Theme theme) {
        if (reservationRepository.existsByDateAndTimeAndTheme(date, time, theme)) {
            throw new RoomEscapeException(ReservationErrorCode.RESERVATION_DUPLICATE);
        }
    }

    @Transactional(readOnly = true)
    public ReservationResponseDTO readReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(
                        () -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
        return ReservationResponseDTO.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> findReservationsByName(String name) {
        return reservationRepository.findByName(name)
                .stream()
                .map(ReservationResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserBookingResponseDTO findReservationsAndWaitingByName(String name) {
        List<ReservationResponseDTO> reservatoins = reservationRepository.findByName(name).stream()
                .map(ReservationResponseDTO::from)
                .toList();
        List<WaitingResponseDTO> waitings = waitingRepository.findByName(name).stream()
                .map(WaitingResponseDTO::from)
                .toList();

        return UserBookingResponseDTO.of(reservatoins, waitings);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponseDTO> readAllReservation() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservedTimeResponseDTO> findReservedTimes(LocalDate targetDate,
                                                           Long targetThemeId) {
        return reservationTimeRepository.findReservedTimes(targetDate, targetThemeId)
                .stream()
                .map(ReservedTimeResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponseDTO updateReservation(Long id, ReservationUpdateRequest request) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(
                () -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND)
        );
        LocalDateTime now = LocalDateTime.now(clock);
        reservation.validateNotPastTime(now);

        ReservationTime time = reservationTimeRepository.findById(request.timeId()).orElseThrow(
                () -> new RoomEscapeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND)
        );

        validateUpdateAvailableTime(request.date(), time, now);
        validateDuplicateReservation(request.date(), time, reservation.getTheme());

        return ReservationResponseDTO.from(reservationRepository.update(id, request.date(), time));
    }

    private void validateUpdateAvailableTime(LocalDate date, ReservationTime time,
                                             LocalDateTime now) {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(now)) {
            throw new RoomEscapeException(ReservationErrorCode.RESERVATION_PAST_TIME);
        }
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(
                        ReservationErrorCode.RESERVATION_NOT_FOUND)
                );
        reservation.validateNotPastTime(LocalDateTime.now(clock));
        // TODO: 내 예약이 아니면 예외(관리자 모드 제외)
        reservationRepository.delete(id);
    }
}
