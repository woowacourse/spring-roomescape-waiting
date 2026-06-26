package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.controller.dto.PaymentReservationRequest;
import roomescape.controller.dto.ReservationPatchRequest;
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.WaitingRequest;
import roomescape.domain.*;
import roomescape.payment.PaymentAmountMismatchException;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentResultUnknownException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.gateway.PaymentGateway;
import roomescape.payment.gateway.toss.TossPaymentException;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.DuplicateSessionException;
import roomescape.exception.InvalidWaitingPrerequisiteException;
import roomescape.exception.SessionNotFoundException;
import roomescape.repository.SessionRepository;
import roomescape.service.dto.AvailableTimeSlot;

import java.util.Objects;
import roomescape.service.dto.Booking;
import roomescape.service.dto.PaymentHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SessionService {

    private final SessionRepository sessionRepository;
    private final TimeSlotService timeSlotService;
    private final ThemeService themeService;
    private final ReservationService reservationService;
    private final WaitingService waitingService;
    private final PaymentGateway paymentGateway;
    private final PaymentOrderService paymentOrderService;

    public SessionService(SessionRepository sessionRepository, TimeSlotService timeSlotService,
                          ThemeService themeService, ReservationService reservationService,
                          WaitingService waitingService, PaymentGateway paymentGateway,
                          PaymentOrderService paymentOrderService) {
        this.sessionRepository = sessionRepository;
        this.timeSlotService = timeSlotService;
        this.themeService = themeService;
        this.reservationService = reservationService;
        this.waitingService = waitingService;
        this.paymentGateway = paymentGateway;
        this.paymentOrderService = paymentOrderService;
    }

    public List<Session> allSessions() {
        return sessionRepository.findAll();
    }

    public Session findSessionById(long id) {
        return sessionRepository.findById(id).orElseThrow(SessionNotFoundException::new);
    }

    @Transactional
    public Session createSession(LocalDate date, Long timeId, Long themeId) {
        TimeSlot timeSlot = timeSlotService.findTimeSlotById(timeId);
        Theme theme = themeService.findThemeById(themeId);
        if (sessionRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId).isPresent()) {
            throw new DuplicateSessionException(date, timeId, themeId);
        }
        return sessionRepository.save(Session.transientOf(date, timeSlot, theme));
    }

    @Transactional
    public List<Session> createSessionsForDate(LocalDate date) {
        List<TimeSlot> timeSlots = timeSlotService.allTimes();
        List<Theme> themes = themeService.allTheme();
        return timeSlots.stream()
                .flatMap(timeSlot -> themes.stream()
                        .map(theme -> sessionRepository.findByDateAndTimeIdAndThemeId(
                                        date, timeSlot.getId(), theme.getId())
                                .orElseGet(() -> sessionRepository.save(Session.transientOf(date, timeSlot, theme)))))
                .toList();
    }

    public List<Reservation> allReservations() {
        return reservationService.findAll();
    }

    public Reservation findReservationById(long id) {
        return reservationService.findById(id);
    }

    public List<Booking> findReservationByName(String name) {
        List<Booking> bookings = new ArrayList<>();
        reservationService.findByName(name).forEach(r -> bookings.add(Booking.fromReservation(r)));
        waitingService.findByName(name).forEach(w -> bookings.add(Booking.fromWaiting(w)));
        return bookings;
    }

    public List<AvailableTimeSlot> findAvailableTimes(long themeId, LocalDate date) {
        themeService.findThemeById(themeId);
        return timeSlotService.findAvailableTimes(themeId, date);
    }

    @Transactional
    public String preparePayment(Long amount) {
        return paymentOrderService.prepare(amount).orderId();
    }

    @Transactional
    public void cancelPreparedPayment(String orderId) {
        paymentOrderService.cancel(orderId);
    }

    public List<PaymentHistory> findPaymentHistory(String userName) {
        return paymentOrderService.findByName(userName).stream()
                .map(order -> PaymentHistory.of(order, findSessionOrNull(order.sessionId())))
                .toList();
    }

    @Transactional
    public Reservation makeReservation(PaymentReservationRequest request) {
        Session session = findSessionOrThrow(request.date(), request.timeId(), request.themeId());
        PaymentOrder order = paymentOrderService.getByOrderId(request.orderId());

        // 이미 확정된 주문이면(success 새로고침 등) 토스를 다시 부르지 않고 기존 예약을 그대로 돌려준다
        if (order.isConfirmed()) {
            return reservationService.findBySession(session)
                    .orElseThrow(() -> new IllegalStateException("확정된 주문의 예약을 찾을 수 없습니다: " + request.orderId()));
        }
        if (!order.amount().equals(request.amount())) {
            throw new PaymentAmountMismatchException(order.amount(), request.amount());
        }

        confirmWithGateway(order, session, request);
        paymentOrderService.confirm(order, request.name(), session.getId(), request.paymentKey());
        return reservationService.save(request.name(), session, request.amount(), request.paymentKey());
    }

    private void confirmWithGateway(PaymentOrder order, Session session, PaymentReservationRequest request) {
        PaymentConfirmation confirmation = new PaymentConfirmation(
                request.paymentKey(), order.orderId(), request.amount(), order.idempotencyKey());
        try {
            paymentGateway.confirm(confirmation);
        } catch (PaymentResultUnknownException e) {
            // read timeout — 승인 여부 불명확. "확인 필요"로 기록(별도 트랜잭션)하고 그대로 알린다
            paymentOrderService.recordUnknown(order.orderId(), request.name(), session.getId(), request.paymentKey());
            throw e;
        } catch (PaymentConnectionException e) {
            // 연결조차 못 함 — 토스에 닿지 않았으니 재시도 안전. PENDING 유지
            paymentOrderService.recordRetryable(order.orderId(), request.name(), session.getId());
            throw e;
        } catch (TossPaymentException e) {
            // 토스가 명시적으로 거절/오류 — "실패"로 기록
            paymentOrderService.recordFailed(order.orderId(), request.name(), session.getId());
            throw e;
        }
    }

    @Transactional
    public void cancelReservation(long reservationId, String userName) {
        Reservation reservation = reservationService.findById(reservationId);
        reservation.validateModifiable(userName, LocalDateTime.now());
        reservationService.delete(reservationId);
        promoteWaitingIfExists(reservation.getSession());
    }

    @Transactional
    public Reservation rescheduleReservation(long id, String userName, ReservationRequest request) {
        Reservation existing = reservationService.findById(id);
        existing.validateModifiable(userName, LocalDateTime.now());
        Session newSession = findSessionOrThrow(request.date(), request.timeId(), request.themeId());
        reservationService.checkDuplicateForUpdate(newSession, id);
        Reservation updated = reservationService.update(existing.reschedule(newSession, LocalDateTime.now()));
        if (!existing.getSession().getId().equals(newSession.getId())) {
            promoteWaitingIfExists(existing.getSession());
        }
        return updated;
    }

    @Transactional
    public Reservation patchReservation(long id, String userName, ReservationPatchRequest request) {
        Reservation existing = reservationService.findById(id);
        existing.validateModifiable(userName, LocalDateTime.now());
        LocalDate date = Objects.requireNonNullElse(request.date(), existing.getSession().getDate());
        Long timeId = Objects.requireNonNullElse(request.timeId(), existing.getSession().getTimeSlot().getId());
        Long themeId = Objects.requireNonNullElse(request.themeId(), existing.getSession().getTheme().getId());
        Session targetSession = findSessionOrThrow(date, timeId, themeId);
        reservationService.checkDuplicateForUpdate(targetSession, id);
        Reservation updated = reservationService.update(existing.reschedule(targetSession, LocalDateTime.now()));
        if (!existing.getSession().getId().equals(targetSession.getId())) {
            promoteWaitingIfExists(existing.getSession());
        }
        return updated;
    }

    @Transactional
    public Waiting addWaiting(WaitingRequest request) {
        Session session = sessionRepository.findByDateAndTimeIdAndThemeId(
                        request.date(), request.timeId(), request.themeId())
                .orElseThrow(InvalidWaitingPrerequisiteException::new);
        Reservation reservation = reservationService.findBySessionOrThrow(session);
        if (reservation.isReservedBy(request.name())) {
            throw new DuplicateReservationException(
                    session.getDate().toString(), session.getTimeSlot().getId(), session.getTheme().getId());
        }
        Waiting waiting = Waiting.transientOf(request.name(), session);
        waitingService.validateNotDuplicate(waiting);
        waiting.validateNotPast(LocalDateTime.now());
        return waitingService.save(waiting);
    }

    @Transactional
    public void cancelWaiting(long waitingId, String userName) {
        Waiting waiting = waitingService.findByIdOrThrow(waitingId);
        waiting.validateModifiable(userName, LocalDateTime.now());
        waitingService.deleteById(waitingId);
    }

    private Session findSessionOrThrow(LocalDate date, Long timeId, Long themeId) {
        return sessionRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(SessionNotFoundException::new);
    }

    private Session findSessionOrNull(Long sessionId) {
        if (sessionId == null) {
            return null;
        }
        return sessionRepository.findById(sessionId).orElse(null);
    }

    private void promoteWaitingIfExists(Session session) {
        if (waitingService.isExistsBySessionId(session.getId())) {
            Waiting first = waitingService.findFirstBySessionId(session.getId());
            waitingService.deleteById(first.getId());
            reservationService.save(first.getName(), session, 0L, null);
        }
    }
}
