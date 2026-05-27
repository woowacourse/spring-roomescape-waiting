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
        User user = getOrCreateUser(request);

        ReservationTime reservationTime = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new NotFoundException(ReservationTimeErrors.RESERVATION_TIME_NOT_EXIST));
        ReservationDate reservationDate = reservationDateRepository.findById(request.dateId())
            .orElseThrow(() -> new NotFoundException(ReservationDateErrors.RESERVATION_DATE_NOT_EXIST));
        validateReservationScheduleToCreate(reservationDate, reservationTime);
        Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(() -> new NotFoundException(ThemeErrors.THEME_NOT_EXIST));

        ReservationSlot reservationSlot = getOrCreateReservation(reservationDate, reservationTime, theme);
        validateUserReservationNotDuplicated(user, reservationSlot);
        Long reservationCount = reservationRepository.countByReservationId(reservationSlot.getId());
        WaitingStatus waitingStatus = decideWaitingStatus(reservationCount);
        Reservation reservation = Reservation.createWithoutId(
            reservationSlot,
            user,
            reservationCount,
            waitingStatus,
            clock
        );
        Reservation savedReservation = reservationRepository.save(reservation);

        return CreateReservationResponse.from(savedReservation);
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationSlotRepository.findAll().stream()
            .map(ReservationResponse::from)
            .toList();
    }

    public UserReservationsResponse getUserReservations(String username) {
        List<Reservation> reservations = reservationRepository.findReservations(username);
        return UserReservationsResponse.of(username, reservations);
    }

    @Transactional
    public void cancelReservationByAdmin(Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ReservationErrors.RESERVATION_NOT_FOUND));
        reservationRepository.deleteById(id);
        reorderWaitingNumbers(reservation.getReservationSlot());
    }

    @Transactional
    public void cancelUserReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ReservationErrors.RESERVATION_NOT_FOUND));
        validateUserCanDeleteReservation(reservation);
        reservationRepository.deleteById(id);
        reorderWaitingNumbers(reservation.getReservationSlot());
    }

    @Transactional
    public void updateReservation(Long id, UpdateReservationRequest request) {
        Reservation userReservation = reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ReservationErrors.USER_RESERVATION_NOT_FOUND));

        ReservationSlot reservationSlot = userReservation.getReservationSlot();

        ReservationTime reservationTime = reservationSlot.getTime();
        ReservationDate reservationDate = reservationSlot.getDate();
        reservationTime = getReservationTime(request, reservationTime);
        reservationDate = getReservationDate(request, reservationDate);
        validateReservationScheduleToCreate(reservationDate, reservationTime);
        Theme theme = reservationSlot.getTheme();
        User user = userReservation.getUser();

        ReservationSlot updatedReservation = getOrCreateReservation(reservationDate, reservationTime, theme);
        validateUserReservationNotDuplicated(user, updatedReservation);

        Long reservationCount = reservationRepository.countByReservationId(reservationSlot.getId());
        WaitingStatus waitingStatus = decideWaitingStatus(reservationCount);

        Reservation updatedUserReservation = userReservation.update(
            updatedReservation,
            reservationCount,
            waitingStatus,
            clock
        );

        reservationRepository.update(userReservation.getId(), updatedUserReservation)
            .orElseThrow(() -> new NotFoundException(ReservationErrors.USER_RESERVATION_NOT_FOUND));

        reorderWaitingNumbers(reservationSlot);
    }

    private void reorderWaitingNumbers(ReservationSlot reservationSlot) {
        List<Reservation> userReservations = reservationRepository.findAllByReservationIdOrder(reservationSlot.getId());
        if (userReservations.isEmpty()) {
            return;
        }
        reservationRepository.updateWaitingNumbers(userReservations);
        reservationRepository.updateStatus(userReservations.getFirst().getId(), WaitingStatus.CONFIRMED);
        reservationRepository.updateAllStatus(userReservations.subList(1, userReservations.size()));
    }

    private void validateReservationScheduleToCreate(
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

    private void validateUserCanDeleteReservation(Reservation reservation) {
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

    private ReservationDate getReservationDate(UpdateReservationRequest request, ReservationDate reservationDate) {
        if (request.startWhen() != null) {
            reservationDate = reservationDateRepository.findByDate(request.startWhen())
                .orElseThrow(() -> new NotFoundException(ReservationDateErrors.RESERVATION_DATE_NOT_EXIST));
        }
        return reservationDate;
    }

    private ReservationTime getReservationTime(UpdateReservationRequest request, ReservationTime reservationTime) {
        if (request.startAt() != null) {
            reservationTime = reservationTimeRepository.findByStartAt(request.startAt())
                .orElseThrow(() -> new NotFoundException(ReservationTimeErrors.RESERVATION_TIME_NOT_EXIST));
        }
        return reservationTime;
    }

    private void validateUserReservationNotDuplicated(User user, ReservationSlot reservation) {
        if (reservationRepository.existsActiveByUserIdAndReservationId(user.getId(), reservation.getId())) {
            throw new BadRequestException(ReservationSlotErrors.DUPLICATED_RESERVATION);
        }
    }

    private WaitingStatus decideWaitingStatus(Long reservationCount) {
        WaitingStatus waitingStatus = WaitingStatus.WAITING;
        if (reservationCount == 0) {
            waitingStatus = WaitingStatus.CONFIRMED;
        }
        return waitingStatus;
    }

    private User getOrCreateUser(CreateReservationRequest request) {
        return userRepository.findByName(request.name())
            .orElseGet(() -> userRepository.save(User.createWithoutId(request.name())));
    }

    private ReservationSlot getOrCreateReservation(
        ReservationDate reservationDate,
        ReservationTime reservationTime,
        Theme theme
    ) {
        if (!reservationSlotRepository.existsReservation(reservationTime.getId(), reservationDate.getId(),
            theme.getId())) {
            return reservationSlotRepository.save(
                ReservationSlot.createWithoutId(reservationDate, reservationTime, theme));
        }
        return reservationSlotRepository.findBySchedule(reservationTime.getId(), reservationDate.getId(), theme.getId())
            .orElseThrow(() -> new NotFoundException(ReservationSlotErrors.RESERVATION_NOT_FOUND));
    }
}
