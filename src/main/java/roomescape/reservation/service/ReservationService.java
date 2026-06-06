package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Slot;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.UserReservationResponse;
import roomescape.reservation.dto.UserReservationsResponse;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.SlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.ReservedTimeResponse;
import roomescape.reservationtime.exception.ReservationTimeErrorCode;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeErrorCode;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ReservationService {

    private final Clock clock;
    private final SlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(Clock clock, SlotRepository slotRepository,
                              ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository) {
        this.clock = clock;
        this.slotRepository = slotRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public ReservationResponse reserve(ReservationRequest request) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND));

        Slot slot = slotRepository.findOrCreate(request.date(), time, theme);
        LocalDateTime now = LocalDateTime.now(clock);
        if (slot.isPast(now)) {
            throw new RoomEscapeException(ReservationErrorCode.RESERVATION_PAST_TIME);
        }

        slotRepository.lockForUpdate(slot.getId());
        ReservationStatus status = determineStatus(slot.getId());
        try {
            Reservation newReservation = Reservation.create(slot, request.name(), status, now);
            Reservation savedReservation = reservationRepository.save(newReservation);
            return ReservationResponse.from(savedReservation);
        } catch (DuplicateKeyException duplicate) {
            throw new RoomEscapeException(ReservationErrorCode.RESERVATION_DUPLICATE);
        }
    }

    private ReservationStatus determineStatus(Long slotId) {
        if (reservationRepository.existsConfirmedBySlotId(slotId)) {
            return ReservationStatus.WAITING;
        }
        return ReservationStatus.CONFIRMED;
    }

    @Transactional(readOnly = true)
    public ReservationResponse readById(Long id) {
        return ReservationResponse.from(reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> readAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserReservationsResponse findUserReservations(String name) {
        List<UserReservationResponse> reservations = reservationRepository.findConfirmedByName(name).stream()
                .map(UserReservationResponse::confirmed)
                .toList();
        List<UserReservationResponse> waitings = reservationRepository.findWaitingRanksByName(name).stream()
                .map(UserReservationResponse::waiting)
                .toList();
        return UserReservationsResponse.of(reservations, waitings);
    }

    @Transactional(readOnly = true)
    public List<ReservedTimeResponse> findReservedTimes(LocalDate targetDate, Long targetThemeId) {
        return reservationTimeRepository.findReservedTimes(targetDate, targetThemeId).stream()
                .map(ReservedTimeResponse::from)
                .toList();
    }

    @Transactional
    public void cancel(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
        reservation.validateNotPast(LocalDateTime.now(clock));

        slotRepository.lockForUpdate(reservation.getSlotId());
        boolean wasConfirmed = reservation.isConfirmed();
        reservationRepository.delete(id);

        if (wasConfirmed) {
            reservationRepository.findFirstWaitingBySlotId(reservation.getSlotId())
                    .ifPresent(waiting ->
                            reservationRepository.updateStatus(waiting.confirm())
                    );
        }
    }
}
