package roomescape.domain.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.reservation.admin.dto.ReservationResponse;
import roomescape.domain.reservation.dto.CreateReservationRequest;
import roomescape.domain.reservation.dto.CreateReservationResponse;
import roomescape.domain.reservation.dto.UpdateReservationRequest;
import roomescape.domain.reservation.dto.UserReservationsResponse;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationslot.ReservationSlotRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.NotFoundException;
import roomescape.support.exception.errors.ReservationDateErrors;
import roomescape.support.exception.errors.ReservationErrors;
import roomescape.support.exception.errors.ReservationSlotErrors;
import roomescape.support.exception.errors.ReservationTimeErrors;
import roomescape.support.exception.errors.ThemeErrors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationSlotRepository reservationSlotRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationDateRepository reservationDateRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    private final Clock clock;

    @Transactional
    public CreateReservationResponse createReservation(CreateReservationRequest request) {
        User reservationUser = findOrCreateUser(request);

        ReservationSlot targetReservationSlot = resolveReservationSlot(request);
        validateNoDuplicateReservation(reservationUser, targetReservationSlot);

        Reservation savedReservation = saveReservationOrThrow(targetReservationSlot, reservationUser);

        return CreateReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public UserReservationsResponse getUserReservations(String username) {
        List<Reservation> userReservations = reservationRepository.findReservations(username);
        return UserReservationsResponse.of(username, userReservations);
    }

    @Transactional
    public void cancelReservationByAdmin(Long id) {
        Reservation reservation = findReservationByIdOrThrow(id);
        reservationRepository.deleteById(id);
        reorderReservationWaitingNumber(reservation.getReservationSlot());
    }

    @Transactional
    public void cancelUserReservation(Long id) {
        Reservation reservation = findReservationByIdOrThrow(id);
        validateReservationDeletionAllowed(reservation);
        reservationRepository.deleteById(id);
        reorderReservationWaitingNumber(reservation.getReservationSlot());
    }

    @Transactional
    public void updateReservation(Long id, UpdateReservationRequest request) {
        Reservation reservation = findReservationByIdOrThrow(id);
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
        ReservationTime requestedReservationTime = findTimeByIdOrThrow(request.timeId());
        ReservationDate requestedReservationDate = findDateByIdOrThrow(request.dateId());
        validateReservationSchedule(requestedReservationDate, requestedReservationTime);

        Theme requestedTheme = findThemeByIdOrThrow(request.themeId());
        return findOrCreateReservationSlot(requestedReservationDate, requestedReservationTime, requestedTheme);
    }

    private ReservationSlot resolveUpdatedReservationSlot(
            ReservationSlot currentSlot,
            UpdateReservationRequest request
    ) {
        ReservationTime updatedTime = resolveReservationTime(request, currentSlot.getTime());
        ReservationDate updatedDate = resolveReservationDate(request, currentSlot.getDate());
        validateReservationSchedule(updatedDate, updatedTime);
        return findOrCreateReservationSlot(updatedDate, updatedTime, currentSlot.getTheme());
    }

    private ReservationDate resolveReservationDate(UpdateReservationRequest request, ReservationDate reservationDate) {
        if (request.dateId() != null) {
            return findDateByIdOrThrow(request.dateId());
        }
        return reservationDate;
    }

    private ReservationTime resolveReservationTime(UpdateReservationRequest request, ReservationTime reservationTime) {
        if (request.timeId() != null) {
            return findTimeByIdOrThrow(request.timeId());
        }
        return reservationTime;
    }

    private void reorderReservationWaitingNumber(ReservationSlot slot) {
        List<Reservation> reservations = reservationRepository.findAllBySlotIdOrderByWaitingNumber(slot.getId());

        if (reservations.isEmpty()) {
            reservationSlotRepository.deleteById(slot.getId());
            return;
        }

        List<Reservation> updatedReservations = assignWaitingNumbersAndStatuses(reservations);
        reservationRepository.batchUpdate(updatedReservations);
    }

    private List<Reservation> assignWaitingNumbersAndStatuses(List<Reservation> reservations) {
        List<Reservation> updatedReservations = new ArrayList<>();

        for (int index = 0; index < reservations.size(); index++) {
            Reservation reservation = reservations.get(index);
            ReservationStatus status = determineStatusByOrder(index);

            updatedReservations.add(reservation.update(index, status, clock));
        }

        return updatedReservations;
    }

    private ReservationStatus determineStatusByOrder(int index) {
        if (index == 0) {
            return ReservationStatus.CONFIRMED;
        }

        return ReservationStatus.WAITING;
    }

    private boolean hasSameReservationSlot(ReservationSlot oldSlot, ReservationSlot newSlot) {
        return oldSlot.getId().equals(newSlot.getId());
    }

    private void updateReservationWhenSameSlot(Reservation reservation, ReservationSlot currentReservationSlot) {
        updateReservationOrThrow(reservation.getId(), reservation.update(clock));
        reorderReservationWaitingNumber(currentReservationSlot);
    }

    private void updateReservationWhenMovingSlot(
            Reservation reservation,
            ReservationSlot currentSlot,
            ReservationSlot updatedSlot
    ) {
        Integer currentReservationCount = reservationRepository.countByReservationSlotId(updatedSlot.getId());
        ReservationStatus updatedStatus = decideWaitingStatus(currentReservationCount);

        Reservation reservationToSave = reservation.update(
                updatedSlot,
                currentReservationCount,
                updatedStatus,
                clock
        );

        updateReservationOrThrow(reservation.getId(), reservationToSave);

        reorderReservationWaitingNumber(currentSlot);
        reorderReservationWaitingNumber(updatedSlot);
    }

    private Reservation buildReservation(ReservationSlot reservationSlot, User user) {
        Integer currentReservationCount = reservationRepository.countByReservationSlotId(reservationSlot.getId());
        ReservationStatus newReservationStatus = decideWaitingStatus(currentReservationCount);
        return Reservation.createWithoutId(
                reservationSlot,
                user,
                currentReservationCount,
                newReservationStatus,
                clock
        );
    }

    private void validateNoDuplicateReservation(User user, ReservationSlot reservationSlot) {
        if (reservationRepository.existsActiveByUserIdAndReservationId(user.getId(), reservationSlot.getId())) {
            throw new BadRequestException(ReservationSlotErrors.DUPLICATED_RESERVATION);
        }
    }

    private ReservationStatus decideWaitingStatus(Integer reservationCount) {
        ReservationStatus status = ReservationStatus.WAITING;
        if (reservationCount == 0) {
            status = ReservationStatus.CONFIRMED;
        }
        return status;
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

    private Reservation findReservationByIdOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ReservationErrors.USER_RESERVATION_NOT_FOUND));
    }

    private User findOrCreateUser(CreateReservationRequest request) {
        return userRepository.findByName(request.name())
                .orElseGet(() -> userRepository.save(User.createWithoutId(request.name())));
    }

    private ReservationSlot findOrCreateReservationSlot(
            ReservationDate reservationDate,
            ReservationTime reservationTime,
            Theme theme
    ) {
        return reservationSlotRepository.findByScheduleForUpdate(
                reservationTime.getId(),
                reservationDate.getId(),
                theme.getId()
        ).orElseGet(() -> saveReservationSlot(reservationDate, reservationTime, theme));
    }

    private Theme findThemeByIdOrThrow(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException(ThemeErrors.THEME_NOT_EXIST));
    }

    private ReservationDate findDateByIdOrThrow(Long dateId) {
        return reservationDateRepository.findById(dateId)
                .orElseThrow(() -> new NotFoundException(ReservationDateErrors.RESERVATION_DATE_NOT_EXIST));
    }

    private ReservationTime findTimeByIdOrThrow(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException(ReservationTimeErrors.RESERVATION_TIME_NOT_EXIST));
    }

    private ReservationSlot saveReservationSlot(
            ReservationDate reservationDate,
            ReservationTime reservationTime,
            Theme theme
    ) {
        try {
            return reservationSlotRepository.save(
                    ReservationSlot.createWithoutId(reservationDate, reservationTime, theme));
        } catch (DuplicateKeyException e) {
            return reservationSlotRepository.findByScheduleForUpdate(
                    reservationTime.getId(),
                    reservationDate.getId(),
                    theme.getId()
            ).orElseThrow(() -> e);
        }
    }

    private Reservation saveReservationOrThrow(ReservationSlot targetReservationSlot, User reservationUser) {
        try {
            return reservationRepository.save(buildReservation(targetReservationSlot, reservationUser));
        } catch (DuplicateKeyException e) {
            throw new BadRequestException(ReservationSlotErrors.DUPLICATED_RESERVATION);
        }
    }

    private void updateReservationOrThrow(Long reservationId, Reservation reservationToSave) {
        try {
            reservationRepository.update(reservationId, reservationToSave);
        } catch (DuplicateKeyException e) {
            throw new BadRequestException(ReservationSlotErrors.DUPLICATED_RESERVATION);
        }
    }
}
