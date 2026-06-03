package roomescape.reservation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationPeriod;
import roomescape.reservation.ReservationStatus;
import roomescape.reservation.application.readmodel.ReservationReadModel;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.request.ReservationUpdateRequest;
import roomescape.reservation.dto.response.MyReservationsAndWaitingsDetailResponse;
import roomescape.reservation.dto.response.ReservationSaveResponse;
import roomescape.reservation.ReservationRepository;
import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;
import roomescape.schedule.application.ScheduleService;
import roomescape.waiting.Waiting;
import roomescape.waiting.application.readmodel.WaitingReadModel;
import roomescape.waiting.WaitingRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ScheduleService scheduleService;
    private final Clock clock;

    public ReservationSaveResponse save(ReservationSaveRequest body, long memberId) {
        scheduleService.validateSchedule(body.date(), body.timeId(), body.themeId());
        long scheduleId = resolveScheduleId(body.date(), body.timeId(), body.themeId());
        validateNotReservationAlreadyExists(scheduleId);
        Reservation reservation = reservationRepository.save(body.toDomain(memberId, scheduleId));

        return ReservationSaveResponse.from(reservation);
    }

    @Transactional
    public void deleteByIdForUser(long reservationId, long memberId) {
        ReservationDetailProjection reservationDetail = reservationRepository.findDetailById(reservationId)
                .orElse(null);
        if (reservationDetail == null) {
            return;
        }

        validateReservationOwner(reservationId, reservationDetail, memberId);
        validateNotPast(reservationDetail);
        long scheduleId = resolveScheduleId(reservationDetail.date(), reservationDetail.timeId(), reservationDetail.themeId());
        deleteReservationAndPromoteWaiting(reservationId, scheduleId);
    }

    @Transactional
    public void deleteByIdForManager(long reservationId) {
        ReservationDetailProjection reservationDetail = reservationRepository.findDetailById(reservationId)
                .orElse(null);
        if (reservationDetail == null) {
            return;
        }

        validateNotPast(reservationDetail);
        long scheduleId = resolveScheduleId(reservationDetail.date(), reservationDetail.timeId(), reservationDetail.themeId());
        deleteReservationAndPromoteWaiting(reservationId, scheduleId);
    }

    @Transactional
    public ReservationSaveResponse updateForUser(ReservationUpdateRequest body, long reservationId, long memberId) {
        ReservationDetailProjection oldReservation = getOldReservationDetailOrThrow(reservationId);
        validateReservationOwner(reservationId, oldReservation, memberId);

        return updateReservationAndPromoteWaiting(body, reservationId, oldReservation);
    }

    @Transactional
    public ReservationSaveResponse updateForManager(ReservationUpdateRequest body, long reservationId) {
        ReservationDetailProjection oldReservation = getOldReservationDetailOrThrow(reservationId);

        return updateReservationAndPromoteWaiting(body, reservationId, oldReservation);
    }

    public List<MyReservationsAndWaitingsDetailResponse> findMyReservationsAndWaitingsByPeriod(long memberId, ReservationPeriod period) {
        if (period == null) {
            period = ReservationPeriod.UPCOMING;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        List<ReservationReadModel> reservations = ReservationReadModel.from(reservationRepository.findUpcomingReservationDetailsByMemberId(memberId, now));
        List<WaitingReadModel> waitings = WaitingReadModel.from(waitingRepository.findUpcomingWaitingDetailsByMemberId(memberId, now));

        if (period == ReservationPeriod.HISTORY) {
            reservations = ReservationReadModel.from(reservationRepository.findPastReservationDetailsByMemberId(memberId, now));
            waitings = WaitingReadModel.from(waitingRepository.findPastWaitingDetailsByMemberId(memberId, now));
        }

        return Stream.concat(
                        reservations.stream().map(MyReservationsAndWaitingsDetailResponse::from),
                        waitings.stream().map(MyReservationsAndWaitingsDetailResponse::from)
                ).sorted(reservationOrderComparator(period))
                .toList();
    }

    public List<MyReservationsAndWaitingsDetailResponse> findReservationDetails() {
        List<ReservationReadModel> reservationReadModels = ReservationReadModel.from(reservationRepository.findAll());

        return MyReservationsAndWaitingsDetailResponse.from(reservationReadModels);
    }

    private Comparator<MyReservationsAndWaitingsDetailResponse> reservationOrderComparator(ReservationPeriod period) {
        Comparator<MyReservationsAndWaitingsDetailResponse> comparator =
                Comparator.comparing(MyReservationsAndWaitingsDetailResponse::date)
                        .thenComparing(response -> response.time().time());

        if (period == ReservationPeriod.HISTORY) {
            comparator = Comparator.comparing(MyReservationsAndWaitingsDetailResponse::date, Comparator.reverseOrder())
                    .thenComparing((a, b) -> b.time().time().compareTo(a.time().time()));
        }

        return comparator.thenComparingInt(response ->
                response.status() == ReservationStatus.RESERVED ? 0 : 1);
    }

    private ReservationSaveResponse updateReservationAndPromoteWaiting(
            ReservationUpdateRequest body,
            long reservationId,
            ReservationDetailProjection oldReservation
    ) {
        validateUpdatable(oldReservation, body);
        long oldScheduleId = resolveScheduleId(oldReservation.date(), oldReservation.timeId(), oldReservation.themeId());
        long newScheduleId = resolveNewScheduleId(body, oldReservation, reservationId);
        validateNotSameSchedule(oldScheduleId, newScheduleId);

        Waiting firstWaiting = popFirstWaiting(oldScheduleId);
        updateReservationSchedule(reservationId, newScheduleId);
        promoteWaitingIfPresent(firstWaiting, oldScheduleId);

        return ReservationSaveResponse.from(getReservationOrThrow(reservationId));
    }

    private long resolveScheduleId(LocalDate date, long timeId, long themeId) {
        return scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(date, timeId, themeId);
    }

    private long resolveNewScheduleId(
            ReservationUpdateRequest body,
            ReservationDetailProjection oldReservation,
            long reservationId
    ) {
        LocalDate newDate = Objects.requireNonNullElse(body.date(), oldReservation.date());
        long newTimeId = Objects.requireNonNullElse(body.timeId(), oldReservation.getTimeId());
        long newScheduleId = resolveScheduleId(newDate, newTimeId, oldReservation.getThemeId());

        scheduleService.validateSchedule(newDate, newTimeId, oldReservation.getThemeId());
        validateNotDuplicatedReservation(reservationId, newScheduleId);
        return newScheduleId;
    }

    private Waiting popFirstWaiting(long scheduleId) {
        Waiting firstWaiting = waitingRepository.findFirstByScheduleId(scheduleId).orElse(null);
        if (firstWaiting != null) {
            waitingRepository.deleteById(firstWaiting.getId());
        }
        return firstWaiting;
    }

    private void updateReservationSchedule(long reservationId, long newScheduleId) {
        int affectedRow = reservationRepository.updateScheduleById(reservationId, newScheduleId);
        validateReservationUpdated(affectedRow);
    }

    private void promoteWaitingIfPresent(Waiting waiting, long oldScheduleId) {
        if (waiting != null) {
            reservationRepository.save(new Reservation(null, waiting.getMemberId(), oldScheduleId));
        }
    }

    private void deleteReservationAndPromoteWaiting(long reservationId, long scheduleId) {
        Waiting firstWaiting = waitingRepository.findFirstByScheduleIdForUpdate(scheduleId)
                .orElse(null);
        if (firstWaiting != null) {
            waitingRepository.deleteById(firstWaiting.getId());
            reservationRepository.deleteById(reservationId);
            reservationRepository.save(new Reservation(null, firstWaiting.getMemberId(), scheduleId));
            return;
        }

        reservationRepository.deleteById(reservationId);
    }

    private Reservation getReservationOrThrow(long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.RESERVATION_NOT_FOUND_AFTER_UPDATE, reservationId));
    }

    private ReservationDetailProjection getOldReservationDetailOrThrow(long reservationId) {
        return reservationRepository.findDetailById(reservationId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.RESERVATION_NOT_FOUND, reservationId));
    }

    private void validateReservationOwner(
            long reservationId,
            ReservationDetailProjection reservationDetail,
            long memberId
    ) {
        if (!Objects.equals(reservationDetail.memberId(), memberId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_NOT_OWNED_BY_MEMBER, reservationId);
        }
    }

    private void validateReservationUpdated(int affectedRow) {
        if (affectedRow != 1) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_UPDATE_FAILED);
        }
    }

    private void validateNotEmptyUpdateRequest(ReservationUpdateRequest body) {
        if (body.date() == null && body.timeId() == null) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_UPDATE_EMPTY);
        }
    }

    private void validateNotSameSchedule(long oldScheduleId, long newScheduleId) {
        if (oldScheduleId == newScheduleId) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_SAME_SCHEDULE);
        }
    }

    private void validateUpdatable(ReservationDetailProjection oldReservation, ReservationUpdateRequest body) {
        validateNotPast(oldReservation);
        validateNotEmptyUpdateRequest(body);
    }

    private void validateNotDuplicatedReservation(long reservationId, long scheduleId) {
        if (reservationRepository.existsByScheduleIdAndIdNot(scheduleId, reservationId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_ALREADY_EXIST, scheduleId);
        }
    }

    private void validateNotReservationAlreadyExists(long scheduleId) {
        if (reservationRepository.existsByScheduleId(scheduleId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_ALREADY_EXIST, scheduleId);
        }
    }

    private void validateNotPast(ReservationDetailProjection reservationDetail) {
        scheduleService.validateNotPastDate(reservationDetail.date());
        scheduleService.validateNotPastTime(reservationDetail.date(), reservationDetail.getTime());
    }
}
