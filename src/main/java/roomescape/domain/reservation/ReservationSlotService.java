package roomescape.domain.reservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.admin.dto.ReservationSlotResponse;
import roomescape.domain.reservation.dto.CreateReservationSlotRequest;
import roomescape.domain.reservation.dto.CreateReservationSlotResponse;
import roomescape.domain.reservation.dto.UpdateReservationSlotRequest;
import roomescape.domain.reservation.dto.UserReservationResponse;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.user.User;
import roomescape.domain.user.UserRepository;
import roomescape.domain.userreservation.UserReservation;
import roomescape.domain.userreservation.UserReservationRepository;
import roomescape.domain.userreservation.WaitingStatus;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.NotFoundException;
import roomescape.support.exception.errors.ReservationDateErrors;
import roomescape.support.exception.errors.ReservationSlotErrors;
import roomescape.support.exception.errors.ReservationTimeErrors;
import roomescape.support.exception.errors.ThemeErrors;
import roomescape.support.exception.errors.UserReservationErrors;

@Service
@RequiredArgsConstructor
public class ReservationSlotService {

    private final ReservationSlotRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationDateRepository reservationDateRepository;
    private final UserReservationRepository userReservationRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;

    private final Clock clock;

    @Transactional
    public CreateReservationSlotResponse createReservation(CreateReservationSlotRequest request) {
        User user = getOrCreateUser(request);

        ReservationTime reservationTime = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new NotFoundException(ReservationTimeErrors.RESERVATION_TIME_NOT_EXIST));
        ReservationDate reservationDate = reservationDateRepository.findById(request.dateId())
            .orElseThrow(() -> new NotFoundException(ReservationDateErrors.RESERVATION_DATE_NOT_EXIST));
        validateReservationScheduleToCreate(reservationDate, reservationTime);
        Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(() -> new NotFoundException(ThemeErrors.THEME_NOT_EXIST));

        ReservationSlot reservation = getOrCreateReservation(reservationDate, reservationTime, theme);
        validateUserReservationNotDuplicated(user, reservation);
        Long reservationCount = userReservationRepository.countByReservationId(reservation.getId());
        WaitingStatus waitingStatus = decideWaitingStatus(reservationCount);
        LocalDateTime now = LocalDateTime.now(clock);
        UserReservation userReservation = UserReservation.createWithoutId(
            reservation,
            user,
            reservationCount,
            waitingStatus,
            now,
            now
        );
        userReservationRepository.save(userReservation);

        return CreateReservationSlotResponse.from(reservation);
    }

    private void validateUserReservationNotDuplicated(User user, ReservationSlot reservation) {
        if (userReservationRepository.existsActiveByUserIdAndReservationId(user.getId(), reservation.getId())) {
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

    private User getOrCreateUser(CreateReservationSlotRequest request) {
        return userRepository.findByName(request.name())
            .orElseGet(() -> userRepository.save(User.createWithoutId(request.name())));
    }

    private ReservationSlot getOrCreateReservation(
        ReservationDate reservationDate,
        ReservationTime reservationTime,
        Theme theme
    ) {
        if (!reservationRepository.existsReservation(reservationTime.getId(), reservationDate.getId(), theme.getId())) {
            return reservationRepository.save(ReservationSlot.createWithoutId(reservationDate, reservationTime, theme));
        }
        return reservationRepository.findBySchedule(reservationTime.getId(), reservationDate.getId(), theme.getId())
            .orElseThrow(() -> new NotFoundException(ReservationSlotErrors.RESERVATION_NOT_FOUND));
    }

    public List<ReservationSlotResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
            .map(ReservationSlotResponse::from)
            .toList();
    }

    public UserReservationResponse getUserReservations(String name) {
        User user = userRepository.findByName(name)
            .orElseThrow(() -> new NotFoundException(ReservationSlotErrors.RESERVATION_NOT_FOUND));
        List<ReservationSlot> reservations = userReservationRepository.findByUserId(user.getId()).stream()
            .map(UserReservation::getReservation)
            .toList();
        return UserReservationResponse.of(name, reservations);
    }

    public void cancelReservationByAdmin(Long id) {
        reservationRepository.deleteById(id);
    }

    public void cancelUserReservation(Long id) {
        ReservationSlot reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ReservationSlotErrors.RESERVATION_NOT_FOUND));
        validateUserCanDeleteReservation(reservation);
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void updateReservation(Long id, UpdateReservationSlotRequest request) {
        UserReservation userReservation = userReservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(UserReservationErrors.USER_RESERVATION_NOT_FOUND));

        ReservationSlot reservation = userReservation.getReservation();

        ReservationTime reservationTime = reservation.getTime();
        ReservationDate reservationDate = reservation.getDate();
        reservationTime = getReservationTime(request, reservationTime);
        reservationDate = getReservationDate(request, reservationDate);
        Theme theme = reservation.getTheme();
        User user = userReservation.getUser();

        ReservationSlot updatedReservation = getOrCreateReservation(reservationDate, reservationTime, theme);
        validateUserReservationNotDuplicated(user, updatedReservation);

        Long reservationCount = userReservationRepository.countByReservationId(reservation.getId());
        WaitingStatus waitingStatus = decideWaitingStatus(reservationCount);

        UserReservation updatedUserReservation = UserReservation.createWithoutId(
                updatedReservation,
                user,
                reservationCount,
                waitingStatus,
                userReservation.getCreatedAt(),
                LocalDateTime.now(clock)
        );

        userReservationRepository.update(userReservation.getId(), updatedUserReservation)
                .orElseThrow(() -> new NotFoundException(UserReservationErrors.USER_RESERVATION_NOT_FOUND));

        reorderWaitingNumbers(reservation);
    }

    private void reorderWaitingNumbers(ReservationSlot reservation) {
        List<UserReservation> userReservations = userReservationRepository.findAllByReservationIdOrder(reservation.getId());
        userReservationRepository.updateWaitingNumbers(userReservations);
        userReservationRepository.updateStatus(userReservations.getFirst().getId(), WaitingStatus.CONFIRMED);
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

    private void validateDuplicatedWithOther(
        Long id,
        ReservationTime reservationTime,
        ReservationDate reservationDate,
        Theme theme
    ) {
        if (reservationRepository.existsOtherReservation(
            id,
            reservationTime.getId(),
            reservationDate.getId(),
            theme.getId()
        )) {
            throw new BadRequestException(ReservationSlotErrors.DUPLICATED_RESERVATION);
        }
    }

    private void validateUserCanDeleteReservation(ReservationSlot reservation) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();
        if (isPastDate(reservation.getDate(), today)) {
            throw new BadRequestException(ReservationDateErrors.PAST_RESERVATION_DATE_CANNOT_BE_DELETED, today);
        }
        if (isPastTimeToday(reservation.getDate(), reservation.getTime(), today, currentTime)) {
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

    private ReservationDate getReservationDate(UpdateReservationSlotRequest request, ReservationDate reservationDate) {
        if (request.startWhen() != null) {
            reservationDate = reservationDateRepository.findByDate(request.startWhen())
                .orElseThrow(() -> new NotFoundException(ReservationDateErrors.RESERVATION_DATE_NOT_EXIST));
        }
        return reservationDate;
    }

    private ReservationTime getReservationTime(UpdateReservationSlotRequest request, ReservationTime reservationTime) {
        if (request.startAt() != null) {
            reservationTime = reservationTimeRepository.findByStartAt(request.startAt())
                .orElseThrow(() -> new NotFoundException(ReservationTimeErrors.RESERVATION_TIME_NOT_EXIST));
        }
        return reservationTime;
    }
}
