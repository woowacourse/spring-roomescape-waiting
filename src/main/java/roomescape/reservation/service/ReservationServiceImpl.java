package roomescape.reservation.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.holiday.service.HolidayService;
import roomescape.reservation.controller.dto.ReservationWithWaitingOrderResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.error.DataInconsistencyException;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.ReservationSaveServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.TimeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final TimeService timeService;
    private final ThemeRepository themeRepository;
    private final HolidayService holidayService;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            TimeService timeService,
            ThemeRepository themeRepository,
            HolidayService holidayService
    ) {
        this.reservationRepository = reservationRepository;
        this.timeService = timeService;
        this.themeRepository = themeRepository;
        this.holidayService = holidayService;
    }

    @Override
    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    @Override
    public List<ReservationWithWaitingOrderResponse> getAllByName(String name) {
        return reservationRepository.findAllByName(name).stream()
                .map(ReservationWithWaitingOrderResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public Reservation create(ReservationSaveServiceRequest request) {
        ReservationTime time = findTime(request.timeId());
        Long themeId = request.themeId();
        time.validateReservableSchedule();
        validateThemeId(themeId);
        validateNotHoliday(time);
        Theme theme = themeRepository.findById(themeId);
        validateNotDuplicated(request.name(), themeId, time);
        boolean hasConfirmed = reservationRepository.hasConfirmedReservation(themeId, time);
        String orderId = generateOrderId(request.amount());
        Status status = resolveStatus(orderId, hasConfirmed);
        Reservation newReservation = new Reservation(request.name(), time, theme, status, orderId, request.amount(), LocalDateTime.now());
        try {
            return reservationRepository.save(newReservation);
        } catch (DuplicateKeyException e) {
            throw new DuplicateReservationException();
        }
    }

    private String generateOrderId(Long amount) {
        if (amount == null) {
            return null;
        }
        return "order-" + UUID.randomUUID().toString().replace("-", "");
    }

    private Status resolveStatus(String orderId, boolean hasConfirmed) {
        if (hasConfirmed) {
            return Status.WAITING;
        }
        return orderId != null ? Status.PAYMENT_PENDING : Status.CONFIRMED;
    }

    private ReservationTime findTime(Long timeId) {
        if (timeId == null) {
            throw new IllegalArgumentException("예약 시간은 필수입니다.");
        }
        return timeService.findById(timeId);
    }

    private void validateThemeId(Long themeId) {
        if (themeId == null) {
            throw new IllegalArgumentException("테마는 필수입니다.");
        }
        if (!themeRepository.existsById(themeId)) {
            throw new ThemeNotFoundException(themeId);
        }
    }

    private void validateNotHoliday(ReservationTime time) {
        if (holidayService.isHoliday(time.getDate())) {
            throw new IllegalArgumentException("휴일은 예약이 불가합니다.");
        }
    }

    private void validateNotDuplicated(String name, Long themeId, ReservationTime time) {
        if (reservationRepository.isDuplicatedWithName(name, themeId, time)) {
            throw new DuplicateReservationException();
        }
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        cancelWithPromotion(reservation);
    }

    @Override
    @Transactional
    public void cancelByOrderId(String orderId) {
        reservationRepository.findByOrderId(orderId)
                .ifPresent(reservation -> cancelWithPromotion(reservation));
    }

    @Override
    @Transactional
    public void cancelForUser(Long id, String name) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        reservation.validateOwnedBy(name);
        reservation.getTime().validateNotPastForCancel();
        cancelWithPromotion(reservation);
    }

    private void cancelWithPromotion(Reservation reservation) {
        promoteNextWaiting(reservation);
        reservationRepository.deleteById(reservation.getId());
    }

    @Override
    @Transactional
    public Reservation update(Long id, Long timeId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
        reservation.getTime().validateUpdatableReservation();
        Long themeId = reservation.getTheme().getId();
        ReservationTime newTime = findTime(timeId);
        newTime.validateReservableSchedule();
        validateNotDuplicated(reservation.getName(), themeId, newTime);
        promoteNextWaiting(reservation);
        Status status = Status.from(
                reservationRepository.hasConfirmedReservation(themeId, newTime));
        boolean updated = reservationRepository.update(id, timeId, LocalDateTime.now(), status);
        if (!updated) {
            throw new IllegalStateException("예약 수정에 실패했습니다. id: " + id);
        }
        return reservation.withTimeAndStatus(newTime, status);
    }

    private void promoteNextWaiting(Reservation reservation) {
        if (reservation.isReserved()) {
            reservationRepository.findEarliestWaiting(reservation.getTime().getId(), reservation.getTheme().getId())
                    .ifPresent(waitingId -> {
                        if (!reservationRepository.promoteToReserved(waitingId)) {
                            throw new DataInconsistencyException("대기 예약 승격에 실패했습니다. id: " + waitingId);
                        }
                    });
        }
    }
}
