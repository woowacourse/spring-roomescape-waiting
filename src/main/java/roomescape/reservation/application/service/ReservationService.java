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
import roomescape.reservationtime.application.exception.ReservationTimeErrorCode;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.theme.application.dto.ThemeQueryResult;
import roomescape.theme.application.exception.ThemeErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;

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
        ReservationTimeQueryResult timeQueryResult = findTimeById(request.timeId());
        validateReservationDateTime(request.date(), timeQueryResult.startAt(), currentDateTime);

        ThemeQueryResult themeQueryResult = findThemeById(request.themeId());

        Reservation reservation = request.toEntity(themeQueryResult.id(), timeQueryResult.id());

        if (reservationRepository.existsByDateAndThemeAndTime(request.date(), request.themeId(), request.timeId())) {
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

        ReservationTimeQueryResult timeQueryResult = findTimeById(request.timeId());
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

    private ThemeQueryResult findThemeById(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND));
        return ThemeQueryResult.from(theme);
    }

    private ReservationTimeQueryResult findTimeById(Long id) {
        ReservationTime time = timeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.TIME_NOT_FOUND));
        return ReservationTimeQueryResult.from(time);
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
        ThemeQueryResult themeQueryResult = findThemeById(reservation.getThemeId());
        ReservationTimeQueryResult timeQueryResult = findTimeById(reservation.getTimeId());
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
