package roomescape.reservation.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationQueryResult;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.event.ReservationScheduleVacatedEvent;
import roomescape.reservation.application.exception.ReservationErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.dto.ReservationDetail;
import roomescape.reservationtime.application.dto.ReservationTimeQueryResult;
import roomescape.reservationtime.application.service.ReservationTimeService;
import roomescape.theme.application.dto.ThemeQueryResult;
import roomescape.theme.application.service.ThemeService;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingService waitingService;
    private final ThemeService themeService;
    private final ReservationTimeService timeService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<ReservationQueryResult> findAll() {
        List<ReservationDetail> result = reservationRepository.findAll();
        return result.stream()
                .map(ReservationQueryResult::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationQueryResult> findAllByName(String name) {
        return reservationRepository.findByName(name).stream()
                .map(this::toQueryResult)
                .toList();
    }

    public ReservationQueryResult save(ReservationCreateCommand request, LocalDateTime currentDateTime) {
        ReservationTimeQueryResult timeQueryResult = timeService.findById(request.timeId());
        validateReservationDateTime(request.date(), timeQueryResult.startAt(), currentDateTime);

        ThemeQueryResult themeQueryResult = themeService.findById(request.themeId());

        Reservation reservation = request.toEntity(themeQueryResult.id(), timeQueryResult.id());

        if (reservationRepository.existsByDateAndThemeAndTime(request.date(), request.themeId(), request.timeId())) {
            validateDuplicateReservationWaiting(request);
            log.info("이미 예약된 일정이므로 대기로 저장합니다. date={}, themeId={}, timeId={}",
                    request.date(), request.themeId(), request.timeId());

            Waiting savedWaitingResult = waitingService.save(Waiting.of(
                    null,
                    reservation.getName(),
                    reservation.getDate(),
                    reservation.getThemeId(),
                    reservation.getTimeId()));

            return ReservationQueryResult.from(savedWaitingResult, themeQueryResult, timeQueryResult);
        }
        return ReservationQueryResult.from(reservationRepository.save(reservation), themeQueryResult, timeQueryResult);
    }

    public ReservationQueryResult update(ReservationUpdateCommand request, LocalDateTime currentDateTime) {
        ReservationDetail reservationDetail = getReservationDetail(request.id());
        Reservation reservation = toReservation(reservationDetail);
        validateOwner(request.name(), reservation);
        validateReservationNotPast(reservationDetail, currentDateTime);

        ReservationTimeQueryResult timeQueryResult = timeService.findById(request.timeId());
        validateReservationDateTime(request.date(), timeQueryResult.startAt(), currentDateTime);
        validateDuplicateReservation(request, reservation);

        if (isSameSchedule(request, reservation)) {
            return toQueryResult(reservation);
        }

        ReservationQueryResult updatedReservation = updateReservation(request, reservation);
        publishScheduleVacated(reservation, "update");

        return updatedReservation;
    }

    public int delete(Long id, String name, LocalDateTime currentDateTime) {
        ReservationDetail reservationDetail = getReservationDetail(id);
        Reservation reservation = toReservation(reservationDetail);
        validateOwner(name, reservation);
        validateReservationNotPast(reservationDetail, currentDateTime);

        int deletedCount = reservationRepository.delete(id);

        if (deletedCount > 0) {
            publishScheduleVacated(reservation, "delete");
        }

        return deletedCount;
    }

    private void validateDuplicateReservationWaiting(ReservationCreateCommand request) {
        boolean alreadyReservedBySameName = reservationRepository.existsByNameAndDateAndThemeAndTime(
                request.name(),
                request.date(),
                request.themeId(),
                request.timeId()
        );

        if (alreadyReservedBySameName) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private ReservationQueryResult updateReservation(ReservationUpdateCommand request, Reservation reservation) {
        Reservation updatedReservation = reservation.update(request.date(), request.timeId());
        Reservation savedReservation = reservationRepository.update(updatedReservation);
        return toQueryResult(savedReservation);
    }

    private void publishScheduleVacated(Reservation reservation, String reason) {
        log.info("예약 일정이 비어 대기 승격 이벤트를 발행합니다. reason={}, reservationId={}, date={}, themeId={}, timeId={}",
                reason,
                reservation.getId(),
                reservation.getDate(),
                reservation.getThemeId(),
                reservation.getTimeId());

        eventPublisher.publishEvent(new ReservationScheduleVacatedEvent(
                reservation.getDate(),
                reservation.getThemeId(),
                reservation.getTimeId()
        ));
    }

    private boolean isSameSchedule(ReservationUpdateCommand request, Reservation reservation) {
        return reservation.getDate().equals(request.date())
                && reservation.getTimeId().equals(request.timeId());
    }

    private ReservationDetail getReservationDetail(Long id) {
        return reservationRepository.findDetailById(id)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateDuplicateReservation(ReservationUpdateCommand request, Reservation reservation) {
        Boolean existsByDateAndTime = reservationRepository.existsByDateAndThemeAndTimeExcludingId(
                request.date(),
                reservation.getThemeId(),
                request.timeId(),
                reservation.getId()
        );
        if (existsByDateAndTime) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void validateOwner(String name, Reservation reservation) {
        if (!reservation.isOwner(name)) {
            throw new RoomEscapeException(ReservationErrorCode.FORBIDDEN_RESERVATION_ACCESS);
        }
    }

    private void validateReservationDateTime(LocalDate date, LocalTime startAt, LocalDateTime currentDateTime) {
        LocalDateTime triedDateTime = LocalDateTime.of(date, startAt);

        if (triedDateTime.isBefore(currentDateTime)) {
            throw new RoomEscapeException(ReservationErrorCode.PAST_RESERVATION_TIME);
        }
    }

    private void validateReservationNotPast(ReservationDetail reservationDetail, LocalDateTime currentDateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDetail.date(), reservationDetail.startAt());

        if (reservationDateTime.isBefore(currentDateTime)) {
            throw new RoomEscapeException(ReservationErrorCode.PAST_RESERVATION_MODIFICATION);
        }
    }

    private ReservationQueryResult toQueryResult(Reservation reservation) {
        ThemeQueryResult themeQueryResult = themeService.findById(reservation.getThemeId());
        ReservationTimeQueryResult timeQueryResult = timeService.findById(reservation.getTimeId());
        return ReservationQueryResult.from(reservation, themeQueryResult, timeQueryResult);
    }

    private Reservation toReservation(ReservationDetail reservationDetail) {
        return Reservation.builder()
                .id(reservationDetail.reservationId())
                .name(reservationDetail.username())
                .date(reservationDetail.date())
                .themeId(reservationDetail.themeId())
                .timeId(reservationDetail.timeId())
                .build();
    }
}
