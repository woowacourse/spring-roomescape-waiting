package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.RoomEscapeException;
import roomescape.payment.domain.Order;
import roomescape.payment.repository.OrderRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Slot;
import roomescape.reservation.dto.ReservationCreateResponse;
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

    private static final long RESERVATION_AMOUNT = 1000L;

    private final Clock clock;
    private final SlotRepository slotRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final OrderRepository orderRepository;

    public ReservationService(Clock clock, SlotRepository slotRepository,
                              ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository,
                              ThemeRepository themeRepository,
                              OrderRepository orderRepository) {
        this.clock = clock;
        this.slotRepository = slotRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public ReservationCreateResponse reserve(ReservationRequest request) {
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
        Reservation savedReservation = save(Reservation.create(slot, request.name(), status, now));

        if (savedReservation.isWaiting()) {
            return ReservationCreateResponse.waiting(savedReservation);
        }
        Order order = orderRepository.save(Order.create(savedReservation.getId(), RESERVATION_AMOUNT));
        return ReservationCreateResponse.pending(savedReservation, order);
    }

    private Reservation save(Reservation reservation) {
        try {
            return reservationRepository.save(reservation);
        } catch (DuplicateKeyException duplicate) {
            throw new RoomEscapeException(ReservationErrorCode.RESERVATION_DUPLICATE);
        }
    }

    private ReservationStatus determineStatus(Long slotId) {
        if (reservationRepository.existsOccupiedBySlotId(slotId)) {
            return ReservationStatus.WAITING;
        }
        return ReservationStatus.PENDING;
    }

    @Transactional
    public void confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
        reservationRepository.updateStatus(reservation.confirm());
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
        List<UserReservationResponse> reservations = reservationRepository.findReservedByName(name).stream()
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
        Long slotId = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND))
                .getSlotId();

        slotRepository.lockForUpdate(slotId);
        // 락 대기 중 승급·취소로 상태가 바뀔 수 있어, 락 안에서 다시 읽은 상태로만 판정한다
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
        reservation.validateNotPast(LocalDateTime.now(clock));

        boolean wasOccupying = reservation.isOccupying();
        int deletedCount = reservationRepository.delete(id);
        if (deletedCount == 0) {
            throw new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND);
        }

        if (wasOccupying) {
            reservationRepository.findFirstWaitingBySlotId(slotId)
                    .ifPresent(waiting ->
                            reservationRepository.updateStatus(waiting.promote())
                    );
        }
    }
}
