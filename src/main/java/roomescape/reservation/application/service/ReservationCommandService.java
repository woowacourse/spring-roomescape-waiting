package roomescape.reservation.application.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.global.exception.UniqueConstraintViolationException;
import roomescape.reservation.application.dto.PaymentFailCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationResult;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.domain.PaymentOrder;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.PaymentOrderRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.theme.application.dto.ThemeResult;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;
    private final WaitingRepository waitingRepository;
    private final PaymentOrderRepository orderRepository;
    private final PaymentService paymentService;

    public ReservationResult save(ReservationCreateCommand request) {
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        ReservationTime time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        ReservationSlot slot = request.toSlot(time.getStartAt());
        Reservation reservation = request.toReservation(slot);

        Reservation savedReservation = saveReservation(reservation);
        PaymentOrder savedOrder = paymentService.prepare(savedReservation.getId());

        return ReservationResult.paymentPending(
                savedReservation,
                ThemeResult.from(theme),
                ReservationTimeResult.from(time),
                savedOrder
        );
    }

    public ReservationResult update(Long reservationId, ReservationUpdateCommand request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));

        ReservationTime updateTime = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        Reservation updatedReservation = updateReservationSlot(request, updateTime.getStartAt(), reservation);
        promoteFirstWaitingToReservation(reservation.getSlot(), request.now());

        Theme theme = themeRepository.findById(updatedReservation.getSlot().themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        return ReservationResult.confirmed(
                updatedReservation,
                ThemeResult.from(theme),
                ReservationTimeResult.from(updateTime)
        );
    }

    public void delete(Long reservationId, LocalDateTime now) {
        ReservationSlot slot = reservationRepository.findSlotById(reservationId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));

        slot.validateDeletable(now);

        orderRepository.deleteByReservationId(reservationId);

        if (reservationRepository.delete(reservationId) == 0) {
            throw new NotFoundException("존재하지 않는 예약입니다.");
        }

        promoteFirstWaitingToReservation(slot, now);
    }

    public void cleanupPendingPaymentFailure(PaymentFailCommand command) {
        if (!command.hasOrderId()) {
            return;
        }

        orderRepository.findByOrderId(command.normalizedOrderId())
                .filter(PaymentOrder::isPending)
                .ifPresent(order -> deletePendingPaymentReservation(order, command.now()));
    }

    private void deletePendingPaymentReservation(PaymentOrder order, LocalDateTime now) {
        Reservation reservation = reservationRepository.findById(order.getReservationId())
                .filter(candidate -> candidate.getStatus() == ReservationStatus.PAYMENT_PENDING)
                .orElse(null);

        if (reservation == null) {
            return;
        }

        ReservationSlot slot = reservation.getSlot();
        slot.validateDeletable(now);

        if (orderRepository.deletePendingByOrderId(order.getOrderId().value()) == 0) {
            return;
        }

        if (reservationRepository.deleteByIdAndStatus(
                order.getReservationId(),
                ReservationStatus.PAYMENT_PENDING) == 0) {
            return;
        }

        promoteFirstWaitingToReservation(slot, now);
    }

    private Reservation updateReservationSlot(ReservationUpdateCommand request, LocalTime startAt,
                                              Reservation reservation) {
        Reservation updatedReservation = reservation.updateDateAndTime(
                request.date(),
                request.timeId(),
                startAt,
                request.now()
        );

        updateReservation(updatedReservation);

        return updatedReservation;
    }

    private void promoteFirstWaitingToReservation(ReservationSlot slot, LocalDateTime now) {
        Optional<Waiting> firstWaitingBySlot = waitingRepository.findFirstBySlot(slot);
        firstWaitingBySlot.ifPresent(waiting -> {
            waitingRepository.delete(waiting.getId());
            waitingRepository.rebalanceRank(waiting.getSlot(), waiting.getRank());

            Reservation promotedReservation = saveReservation(
                    Reservation.create(waiting.getUser(), waiting.getSlot(), now)
            );
            paymentService.prepare(promotedReservation.getId());
        });
    }

    private Reservation saveReservation(Reservation reservation) {
        try {
            return reservationRepository.save(reservation);
        } catch (UniqueConstraintViolationException e) {
            throw new ConflictException("이미 해당 날짜와 시간에 예약이 존재합니다.");
        }
    }

    private void updateReservation(Reservation reservation) {
        try {
            if (reservationRepository.update(reservation.getId(), reservation.getSlot()) == 0) {
                throw new NotFoundException("존재하지 않는 예약입니다.");
            }
        } catch (UniqueConstraintViolationException e) {
            throw new ConflictException("변경하려는 날짜와 시간에 이미 예약이 존재합니다.");
        }
    }
}
