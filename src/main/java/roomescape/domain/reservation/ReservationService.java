package roomescape.domain.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.admin.dto.ReservationResponse;
import roomescape.domain.reservation.dto.CreateReservationRequest;
import roomescape.domain.reservation.dto.CreateReservationResponse;
import roomescape.domain.reservation.dto.ReservationWithWaitingNumber;
import roomescape.domain.reservation.dto.UpdateReservationRequest;
import roomescape.domain.reservation.dto.UserReservationsResponse;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateService;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationslot.ReservationSlotService;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeService;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeService;
import roomescape.domain.user.User;
import roomescape.domain.user.UserService;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.NotFoundException;
import roomescape.support.exception.errors.ReservationDateErrors;
import roomescape.support.exception.errors.ReservationErrors;
import roomescape.support.exception.errors.ReservationSlotErrors;
import roomescape.support.exception.errors.ReservationTimeErrors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final JpaReservationRepository reservationRepository;
    private final UserService userService;
    private final ReservationSlotService reservationSlotService;
    private final ThemeService themeService;
    private final ReservationDateService reservationDateService;
    private final ReservationTimeService reservationTimeService;

    private final Clock clock;

    @Transactional
    public CreateReservationResponse createReservation(CreateReservationRequest request) {
        User reservationUser = userService.findOrCreateUser(request.name());

        ReservationSlot targetReservationSlot = resolveReservationSlot(request);
        validateNoDuplicateReservation(reservationUser, targetReservationSlot);

        Reservation savedReservation = reservationRepository.save(
            buildReservation(targetReservationSlot, reservationUser));

        return CreateReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findReservationsForAdmin(ReservationStatus.WAITING).stream()
            .map(ReservationResponse::from)
            .toList();
    }

    public List<ReservationResponse> getWaitingReservations() {
        return reservationRepository.findWaitingReservationsForAdmin(ReservationStatus.WAITING).stream()
            .map(ReservationResponse::from)
            .toList();
    }

    public UserReservationsResponse getUserReservations(String username) {
        List<ReservationWithWaitingNumber> userReservations = reservationRepository.findUserReservations(
            username,
            ReservationStatus.WAITING
        );
        return UserReservationsResponse.of(username, userReservations);
    }

    @Transactional
    public void cancelReservationByAdmin(Long id) {
        Reservation reservation = findActiveReservationByIdOrThrow(id);
        ReservationStatus previousStatus = reservation.getStatus();
        reservationRepository.save(reservation.update(ReservationStatus.CANCELED, clock));
        if (previousStatus == ReservationStatus.CONFIRMED) {
            promoteFirstWaitingReservation(reservation.getReservationSlot());
        }
    }

    @Transactional
    public void cancelUserReservation(Long id) {
        Reservation reservation = findActiveReservationByIdOrThrow(id);
        validateReservationDeletionAllowed(reservation);
        ReservationStatus previousStatus = reservation.getStatus();
        reservationRepository.save(reservation.update(ReservationStatus.CANCELED, clock));
        if (previousStatus == ReservationStatus.CONFIRMED) {
            promoteFirstWaitingReservation(reservation.getReservationSlot());
        }
    }

    @Transactional
    public void updateReservation(Long id, UpdateReservationRequest request) {
        Reservation reservation = findActiveReservationByIdOrThrow(id);
        ReservationSlot currentReservationSlot = reservation.getReservationSlot();
        ReservationSlot updatedReservationSlot = resolveUpdatedReservationSlot(currentReservationSlot, request);

        if (hasSameReservationSlot(currentReservationSlot, updatedReservationSlot)) {
            updateReservationWhenSameSlot(reservation, currentReservationSlot);
            return;
        }

        validateNoDuplicateReservation(reservation.getUser(), updatedReservationSlot);
        updateReservationWhenMovingSlot(reservation, currentReservationSlot, updatedReservationSlot);
    }

    private ReservationSlot resolveReservationSlot(CreateReservationRequest request) {
        ReservationTime requestedReservationTime = reservationTimeService.findTimeByIdOrThrow(request.timeId());
        ReservationDate requestedReservationDate = reservationDateService.findDateByIdOrThrow(request.dateId());
        validateReservationSchedule(requestedReservationDate, requestedReservationTime);

        Theme requestedTheme = themeService.findThemeByIdOrThrow(request.themeId());
        return reservationSlotService
            .findOrCreateReservationSlot(requestedReservationDate, requestedReservationTime, requestedTheme);
    }

    private ReservationSlot resolveUpdatedReservationSlot(
        ReservationSlot currentSlot,
        UpdateReservationRequest request
    ) {
        ReservationTime updatedTime = resolveReservationTime(request, currentSlot.getTime());
        ReservationDate updatedDate = resolveReservationDate(request, currentSlot.getDate());
        validateReservationSchedule(updatedDate, updatedTime);
        return reservationSlotService.findOrCreateReservationSlot(updatedDate, updatedTime, currentSlot.getTheme());
    }

    private ReservationDate resolveReservationDate(UpdateReservationRequest request, ReservationDate reservationDate) {
        if (request.dateId() != null) {
            return reservationDateService.findDateByIdOrThrow(request.dateId());
        }
        return reservationDate;
    }

    private ReservationTime resolveReservationTime(UpdateReservationRequest request, ReservationTime reservationTime) {
        if (request.timeId() != null) {
            return reservationTimeService.findTimeByIdOrThrow(request.timeId());
        }
        return reservationTime;
    }

    private boolean hasSameReservationSlot(ReservationSlot oldSlot, ReservationSlot newSlot) {
        return oldSlot.getId().equals(newSlot.getId());
    }

    private void updateReservationWhenSameSlot(Reservation reservation, ReservationSlot currentReservationSlot) {
        Long currentReservationCount = reservationRepository.countActiveReservationsInSlot(
            currentReservationSlot.getId(),
            ReservationStatus.CANCELED
        );
        ReservationStatus updatedStatus = decideWaitingStatus(currentReservationCount - 1);
        ReservationStatus previousStatus = reservation.getStatus();
        reservationRepository.save(reservation.update(currentReservationSlot, updatedStatus, clock));
        if (shouldPromoteNextWaitingReservation(previousStatus, updatedStatus)) {
            promoteFirstWaitingReservation(currentReservationSlot);
        }
    }

    private boolean shouldPromoteNextWaitingReservation(
        ReservationStatus previousStatus,
        ReservationStatus updatedStatus
    ) {
        return previousStatus == ReservationStatus.CONFIRMED && updatedStatus == ReservationStatus.WAITING;
    }

    private void updateReservationWhenMovingSlot(
        Reservation reservation,
        ReservationSlot currentSlot,
        ReservationSlot updatedSlot
    ) {
        Long currentReservationCount = reservationRepository.countActiveReservationsInSlot(
            updatedSlot.getId(),
            ReservationStatus.CANCELED
        );
        ReservationStatus updatedStatus = decideWaitingStatus(currentReservationCount);

        Reservation reservationToSave = reservation.update(
            updatedSlot,
            updatedStatus,
            clock
        );

        ReservationStatus previousStatus = reservation.getStatus();
        reservationRepository.save(reservationToSave);
        if (previousStatus == ReservationStatus.CONFIRMED) {
            promoteFirstWaitingReservation(currentSlot);
        }
    }

    private void promoteFirstWaitingReservation(ReservationSlot reservationSlot) {
        reservationRepository.findWaitingReservationsForPromotion(
                reservationSlot.getId(),
                ReservationStatus.WAITING
            ).stream()
            .findFirst()
            .ifPresent(reservation -> reservationRepository.save(
                reservation.update(ReservationStatus.CONFIRMED, clock)
            ));
    }

    private Reservation buildReservation(ReservationSlot reservationSlot, User user) {
        Long currentReservationCount = reservationRepository.countActiveReservationsInSlot(
            reservationSlot.getId(),
            ReservationStatus.CANCELED
        );
        ReservationStatus newReservationStatus = decideWaitingStatus(currentReservationCount);
        return Reservation.createWithoutId(
            reservationSlot,
            user,
            newReservationStatus,
            clock
        );
    }

    private void validateNoDuplicateReservation(User user, ReservationSlot reservationSlot) {
        if (reservationRepository.existsByUserIdAndReservationSlotIdAndStatusNot(
            user.getId(),
            reservationSlot.getId(),
            ReservationStatus.CANCELED
        )) {
            throw new BadRequestException(ReservationSlotErrors.DUPLICATED_RESERVATION);
        }
    }

    private ReservationStatus decideWaitingStatus(Long reservationCount) {
        ReservationStatus waitingStatus = ReservationStatus.WAITING;
        if (reservationCount == 0) {
            waitingStatus = ReservationStatus.CONFIRMED;
        }
        return waitingStatus;
    }

    private void validateReservationSchedule(
        ReservationDate reservationDate,
        ReservationTime reservationTime
    ) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        if (isPastDate(reservationDate, today)) {
            throw new BadRequestException(ReservationDateErrors.RESERVATION_DATE_MUST_BE_TODAY_OR_LATER, today);
        }
        if (isPastTimeToday(reservationDate, reservationTime, today, currentTime)) {
            throw new BadRequestException(ReservationTimeErrors.RESERVATION_TIME_SHOULD_BE_NOW_OR_LATER, currentTime);
        }
    }

    private void validateReservationDeletionAllowed(Reservation reservation) {
        ReservationSlot reservationSlot = reservation.getReservationSlot();
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        if (isPastDate(reservationSlot.getDate(), today)) {
            throw new BadRequestException(ReservationDateErrors.PAST_RESERVATION_DATE_CANNOT_BE_DELETED, today);
        }
        if (isPastTimeToday(reservationSlot.getDate(), reservationSlot.getTime(), today, currentTime)) {
            throw new BadRequestException(ReservationTimeErrors.PAST_RESERVATION_TiME_CANNOT_BE_DELETED, currentTime);
        }
    }

    private boolean isPastDate(ReservationDate reservationDate, LocalDate today) {
        return reservationDate.isBefore(today);
    }

    private boolean isPastTimeToday(
        ReservationDate reservationDate,
        ReservationTime reservationTime,
        LocalDate today,
        LocalTime now
    ) {
        return reservationDate.isSame(today) && reservationTime.isBefore(now);
    }

    private Reservation findActiveReservationByIdOrThrow(Long id) {
        return reservationRepository.findByIdAndStatusNot(id, ReservationStatus.CANCELED)
            .orElseThrow(() -> new NotFoundException(ReservationErrors.USER_RESERVATION_NOT_FOUND));
    }
}
