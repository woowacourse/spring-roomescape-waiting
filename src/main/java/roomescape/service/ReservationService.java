package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.ReservationRequestDTO;
import roomescape.dto.ReservationResponseDTO;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.dto.ReservedTimeResponseDTO;
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
        LocalDate date = reservationRequestDTO.date();
        ReservationTime time = reservationTimeRepository.findById(reservationRequestDTO.timeId())
                .orElseThrow(() -> new RoomEscapeException(
                        ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND));
        Theme theme = themeRepository.findById(reservationRequestDTO.themeId())
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND));

        ReservationSlot slot = ReservationSlot.of(date, time, theme);
        validateDuplicateReservation(slot);

        Reservation reservation = Reservation.create(reservationRequestDTO.name(), slot);
        slot.validateNotPastTime(LocalDateTime.now(clock));

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponseDTO.from(savedReservation);
    }

    private void validateDuplicateReservation(ReservationSlot slot) {
        if (reservationRepository.existsBySlot(slot)) {
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
    public List<ReservationResponseDTO> readAllReservation() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservedTimeResponseDTO> findReservedTimes(
            LocalDate targetDate,
            Long targetThemeId
    ) {
        return reservationTimeRepository.findReservedTimes(targetDate, targetThemeId);
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

        reservationRepository.findBySlotWithLock(reservation.getReservationSlot());

        ReservationSlot slotForUpdate = ReservationSlot.of(
                request.date(),
                time,
                reservation.getReservationSlot().getTheme()
        );
        slotForUpdate.validateNotPastTime(now);
        validateDuplicateReservation(slotForUpdate);

        Reservation updatedReservation = reservationRepository.update(id, slotForUpdate);

        promoteWaitingToReservationBySlot(reservation.getReservationSlot());

        return ReservationResponseDTO.from(updatedReservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(
                        ReservationErrorCode.RESERVATION_NOT_FOUND)
                );

        ReservationSlot slot = reservation.getReservationSlot();
        reservation.validateNotPastTime(LocalDateTime.now(clock));
        reservationRepository.findBySlotWithLock(slot);

        reservationRepository.delete(id);

        promoteWaitingToReservationBySlot(slot);
    }

    private void promoteWaitingToReservationBySlot(ReservationSlot slot) {
        Optional<Waiting> promotableWaiting = waitingRepository.findPromotableWaitingBySlotWithLock(slot);
        promotableWaiting.ifPresent(waiting -> {
            Reservation promotedReservation = Reservation.create(
                    waiting.getName(),
                    waiting.getReservationSlot()
            );
            reservationRepository.save(promotedReservation);
            waitingRepository.delete(waiting.getId());
        });
    }
}
