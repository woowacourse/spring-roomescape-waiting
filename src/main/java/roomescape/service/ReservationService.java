package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.*;
import roomescape.dto.request.ReservationCreateRequest;
import roomescape.dto.command.ReservationModifyCommand;
import roomescape.dto.response.AvailableDateResult;
import roomescape.dto.response.ReservationResult;
import roomescape.dto.response.ReservationTimeStatusResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    private final ThemeRepository themeRepository;
    private final WaitingListRepository waitingListRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ReservationResult create(final ReservationCreateRequest command) {
        final Theme findTheme = findThemeOrThrow(command.themeId());
        final ReservationTime findReservationTime = findReservationTimeOrThrow(command.timeId());

        final Reservation reservation = Reservation.create(command.name(), command.date(), findReservationTime, findTheme);

        reservation.validateNotPast();

        validateNotDuplicated(reservation);

        final Reservation savedReservation = reservationRepository.save(reservation);

        final Order order = Order.create(command.amount(), savedReservation.getId());
        final Order savedOrder = orderRepository.save(order);

        return ReservationResult.from(savedReservation, savedOrder);
    }

    public AvailableDateResult getReservationOptions() {
        return new AvailableDateResult(ReservationPolicy.getReservableDates());
    }

    public List<ReservationResult> getReservationsByName(final String name) {
        final List<Reservation> reservations = reservationRepository.findByName(name);
        return reservations.stream()
                .map(r -> {
                    Order findOrder = orderRepository.findByReservationId(r.getId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
                    return ReservationResult.from(r, findOrder);
                })
                .toList();
    }

    public List<ReservationResult> getReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(r -> {
                    Order findOrder = orderRepository.findByReservationId(r.getId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
                    return ReservationResult.from(r, findOrder);
                })
                .toList();
    }

    public List<ReservationTimeStatusResult> getReservationTimeStatuses(final LocalDate date, final Long themeId) {
        return reservationRepository.findReservationTimeStatusesByDateAndThemeId(date, themeId);
    }

    @Transactional
    public ReservationResult modify(final ReservationModifyCommand command) {
        final Reservation originalReservation = findReservationOrThrow(command.reservationId());

        originalReservation.validateOwner(command.name());

        final ReservationTime findReservationTime = findReservationTimeOrThrow(command.timeId());
        final Theme findTheme = findThemeOrThrow(command.themeId());

        final Reservation modifiedReservation = originalReservation.modify(command.date(), findReservationTime, findTheme);

        validateNotDuplicated(modifiedReservation);

        reservationRepository.update(modifiedReservation);

        if (!originalReservation.isSameSlot(modifiedReservation)) {
            promoteFirstWaitingList(originalReservation);
        }

        Order findOrder = orderRepository.findByReservationId(modifiedReservation.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        return ReservationResult.from(modifiedReservation, findOrder);
    }

    @Transactional
    public void deleteWithValidation(final Long reservationId, final String name) {
        final Reservation reservation = findReservationOrThrow(reservationId);

        reservation.validateOwner(name);
        reservation.validateNotPast();

        deleteInternal(reservation);
    }

    @Transactional
    public void deleteAsAdmin(final Long reservationId) {
        deleteInternal(findReservationOrThrow(reservationId));
    }

    private void deleteInternal(final Reservation reservation) {
        reservationRepository.deleteById(reservation.getId());
        promoteFirstWaitingList(reservation);
    }

    private void promoteFirstWaitingList(Reservation reservation) {
        final Optional<WaitingList> findFirstWaitingList = waitingListRepository.findFirstByDateAndTimeAndThemeOrderByCreatedAtAsc(reservation.getReservationDate().getDate(), reservation.getTime(), reservation.getTheme());

        if (findFirstWaitingList.isPresent()) {
            final WaitingList waitingList = findFirstWaitingList.get();

            final Reservation promoted = Reservation.create(waitingList.getName(), waitingList.getReservationDate().getDate(), waitingList.getReservationTime(), waitingList.getTheme());
            reservationRepository.save(promoted);

            waitingListRepository.deleteById(waitingList.getId());
        }
    }

    private void validateNotDuplicated(final Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                reservation.getReservationDate().getDate(), reservation.getTime().getId(), reservation.getTheme().getId())) {
            throw new BusinessException(ErrorCode.TIME_ALREADY_RESERVED);
        }
    }

    private Reservation findReservationOrThrow(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    private ReservationTime findReservationTimeOrThrow(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TIME_NOT_FOUND));
    }

    private Theme findThemeOrThrow(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));
    }
}
