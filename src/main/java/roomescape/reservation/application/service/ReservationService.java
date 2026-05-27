package roomescape.reservation.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationQueryResult;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.exception.ReservationErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.domain.repository.dto.ReservationDetail;
import roomescape.reservationtime.application.dto.ReservationTimeQueryResult;
import roomescape.reservationtime.application.service.ReservationTimeService;
import roomescape.theme.application.dto.ThemeQueryResult;
import roomescape.theme.application.service.ThemeService;

@RequiredArgsConstructor
@Transactional
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ThemeService themeService;
    private final ReservationTimeService timeService;

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

        if(reservationRepository.existsByDateAndThemeAndTime(request.date(), request.themeId(), request.timeId())) {
            Waiting savedWaitingResult = waitingRepository.save(Waiting.of(
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

        Reservation updatedReservation = reservation.update(request.date(), request.timeId());
        Reservation savedReservation = reservationRepository.update(updatedReservation);
        return toQueryResult(savedReservation);
    }

    public int delete(Long id, String name, LocalDateTime currentDateTime) {
        ReservationDetail reservationDetail = getReservationDetail(id);
        Reservation reservation = toReservation(reservationDetail);
        validateOwner(name, reservation);
        validateReservationNotPast(reservationDetail, currentDateTime);

        Optional<Waiting> oldestWaiting = waitingRepository.findOldestByDateAndThemeIdAndTimeId(
                reservation.getDate(), reservation.getThemeId(),
                reservation.getTimeId());

        if (oldestWaiting.isPresent()) {
            Waiting waiting = oldestWaiting.get();
            reservationRepository.updateWaitingOwner(reservation.getId(), waiting.getName());
            return waitingRepository.delete(waiting.getId());
        }

        return reservationRepository.delete(id);
    }

    private ReservationDetail getReservationDetail(Long id) {
        return reservationRepository.findDetailById(id)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    private void validateDuplicateReservation(ReservationCreateCommand request) {
        Boolean existsByDateAndTime = reservationRepository.existsByDateAndThemeAndTime(request.date(),
                request.themeId(),
                request.timeId()
        );
        if (existsByDateAndTime) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
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
