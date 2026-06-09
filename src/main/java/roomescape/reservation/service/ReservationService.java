package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.CustomerEmail;
import roomescape.reservation.domain.CustomerName;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.exception.ReservationAlreadyExistsException;
import roomescape.reservation.domain.exception.ReservationNotFoundException;
import roomescape.reservation.domain.exception.ReservationOptionChangedException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservation.controller.dto.request.ReservationCreateRequest;
import roomescape.reservation.controller.dto.request.ReservationUpdateRequest;
import roomescape.reservation.controller.dto.response.ReservationOptionResponse;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.controller.dto.response.ReservationsAndWaitingsResponse;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.exception.ReservationTimeNotFoundException;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.repository.ReservationSlotRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.controller.dto.response.ThemeResponse;
import roomescape.wating.domain.exception.WaitingNotFoundException;
import roomescape.wating.repository.WaitingRepository;
import roomescape.wating.controller.dto.response.WaitingResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final int RESERVABLE_DAYS_RANGE = 14;

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final WaitingRepository waitingRepository;

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationsAndWaitingsResponse getReservationsByCustomer(final String customerName, final String customerEmail) {
        final LocalDateTime now = LocalDateTime.now();
        final CustomerName validCustomerName = new CustomerName(customerName);
        final CustomerEmail validCustomerEmail = new CustomerEmail(customerEmail);

        final List<Reservation> reservations = reservationRepository.findAllByCustomerNameAndCustomerEmailAndReservationDateTimeAfter(
                validCustomerName.name(),
                validCustomerEmail.email(),
                now
        );
        final List<WaitingResponse> waitingsWithRank = waitingRepository.findAllWithRankByCustomerNameAndCustomerEmailAndReservationDateTimeAfter(
                        validCustomerName.name(),
                        validCustomerEmail.email(),
                        now
                )
                .stream()
                .map(WaitingResponse::from)
                .toList();

        return ReservationsAndWaitingsResponse.from(reservations, waitingsWithRank);
    }

    public List<ReservationTimesWithStatus> getReservationTimeStatuses(final LocalDate date, final Long themeId) {
        return reservationRepository.findReservationTimeStatusesByDateAndThemeId(date, themeId);
    }

    public ReservationResponse create(final ReservationCreateRequest data) {
        final ReservationTime reservationTime = getReservationTime(data.timeId());
        final Theme theme = getTheme(data.themeId());
        final ReservationSlot slot = findOrCreateSlot(data.date(), reservationTime, theme);

        final Reservation reservation = Reservation.create(
                data.name(),
                data.email(),
                slot,
                LocalDateTime.now()
        );

        final Reservation savedReservation = saveReservation(reservation);

        return ReservationResponse.from(savedReservation);
    }

    public ReservationResponse updateByAdmin(final Long reservationId, final ReservationUpdateRequest data) {
        final Reservation originReservation = getReservation(reservationId);

        return updateSchedule(data, originReservation);
    }

    private ReservationResponse updateSchedule(final ReservationUpdateRequest data, final Reservation originReservation) {
        final ReservationTime newReservationTime = getReservationTime(data.timeId());
        final ReservationSlot slot = findOrCreateSlot(
                data.date(),
                newReservationTime,
                originReservation.getTheme()
        );

        final Reservation updatedReservation = originReservation.changeSchedule(
                slot,
                LocalDateTime.now()
        );
        final Reservation reservation = updateReservation(updatedReservation);

        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void cancelByCustomer(final Long reservationId, final String customerName, final String customerEmail) {
        final Reservation reservation = getReservation(reservationId);
        validateOwnedByCustomer(reservation, customerName, customerEmail);
        reservation.validateCancelableByCustomer(LocalDate.now());

        deleteReservationAndPromoteWaiting(reservation);
    }

    @Transactional
    public void cancelByAdmin(final Long reservationId) {
        final Reservation reservation = getReservation(reservationId);

        deleteReservationAndPromoteWaiting(reservation);
    }

    public ReservationOptionResponse getReservationOptions() {
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = today.datesUntil(today.plusDays(RESERVABLE_DAYS_RANGE)).toList();

        List<ThemeResponse> themes = themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();

        return new ReservationOptionResponse(dates, themes);
    }

    private Reservation saveReservation(final Reservation reservation) {
        try {
            return reservationRepository.save(reservation);
        } catch (DuplicateKeyException exception) {
            throw new ReservationAlreadyExistsException(exception);
        } catch (DataIntegrityViolationException exception) {
            throw new ReservationOptionChangedException(exception);
        }
    }

    private Reservation updateReservation(final Reservation reservation) {
        try {
            final boolean updated = reservationRepository.update(reservation);

            if (!updated) {
                throw new ReservationNotFoundException();
            }

            return reservation;
        } catch (DuplicateKeyException exception) {
            throw new ReservationAlreadyExistsException(exception);
        } catch (DataIntegrityViolationException exception) {
            throw new ReservationOptionChangedException(exception);
        }
    }

    private ReservationSlot findOrCreateSlot(
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme
    ) {
        try {
            return reservationSlotRepository.findOrCreate(date, reservationTime, theme);
        } catch (DataIntegrityViolationException exception) {
            throw new ReservationOptionChangedException(exception);
        }
    }

    private void deleteReservationAndPromoteWaiting(final Reservation reservation) {
        final ReservationSlot slot = reservationSlotRepository.findByIdForUpdate(reservation.getSlotId())
                .orElseThrow(ReservationNotFoundException::new);

        if (!reservationRepository.deleteByIdAndSlotId(reservation.getId(), slot.getId())) {
            throw new ReservationNotFoundException();
        }

        waitingRepository.findEarliestBySlotId(slot.getId())
                .ifPresent(waiting -> {
                    final Reservation promotedReservation = Reservation.of(
                            null,
                            waiting.getCustomerName().name(),
                            waiting.getCustomerEmail(),
                            waiting.getSlot()
                    );

                    saveReservation(promotedReservation);
                    if (!waitingRepository.deleteById(waiting.getId())) {
                        throw new WaitingNotFoundException();
                    }
                });
    }

    private Reservation getReservation(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(ReservationNotFoundException::new);
    }

    private void validateOwnedByCustomer(
            final Reservation reservation,
            final String customerName,
            final String customerEmail
    ) {
        if (!reservation.isOwnedBy(customerName, customerEmail)) {
            throw new ReservationNotFoundException();
        }
    }

    private ReservationTime getReservationTime(final Long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(ReservationTimeNotFoundException::new);
    }

    private Theme getTheme(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(ThemeNotFoundException::new);
    }
}
