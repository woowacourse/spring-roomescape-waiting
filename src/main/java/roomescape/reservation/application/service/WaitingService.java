package roomescape.reservation.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.exception.BadRequestException;
import roomescape.reservation.application.exception.ConflictException;
import roomescape.reservation.application.exception.ErrorMessage;
import roomescape.reservation.application.exception.ReservationErrorCode;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.event.schema.WaitingSaved;
import roomescape.reservation.presentation.dto.WaitingResponse;
import roomescape.reservationtime.application.exception.ReservationTimeErrorCode;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.theme.application.exception.ThemeErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@RequiredArgsConstructor
@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<WaitingResponse> findAllByName(String name) {
        return waitingRepository.findByName(name).stream()
                .map(WaitingResponse::from)
                .toList();
    }

    @Transactional
    public void save(ReservationCreateCommand request, LocalDateTime requested) {
        ReservationTime time = findTimeById(request.timeId());
        validateReservationDateTime(request.date(), time.getStartAt(), requested);

        Theme theme = findThemeById(request.themeId());

        Waiting pending = Waiting.of(null, request.name(), request.date(), theme.getId(), time.getId());

        if(waitingRepository.existsByNameAndDateAndThemeIdAndTimeId(
                pending.getName(),
                pending.getDate(),
                pending.getThemeId(),
                pending.getTimeId())) {
            throw new ConflictException(ErrorMessage.DUPLICATE_WAITING);
        }

        Waiting saved = waitingRepository.save(pending);
        eventPublisher.publishEvent(new WaitingSaved(
                saved.getDate(),
                saved.getThemeId(),
                saved.getTimeId())
        );
    }

    @Transactional
    public void deleteOldestBySlot(LocalDate date, Long themeId, Long timeId) {
        waitingRepository.deleteOldestBySlot(date, themeId, timeId);
    }

    @Transactional
    public int delete(Long id, String name) {
        Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));

        if (!waiting.isOwner(name)) {
            throw new RoomEscapeException(ReservationErrorCode.FORBIDDEN_RESERVATION_ACCESS);
        }

        return waitingRepository.delete(id);
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND));
    }

    private ReservationTime findTimeById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ReservationTimeErrorCode.TIME_NOT_FOUND));
    }

    private void validateReservationDateTime(LocalDate date, LocalTime startAt, LocalDateTime currentDateTime) {
        LocalDateTime triedDateTime = LocalDateTime.of(date, startAt);

        if (triedDateTime.isBefore(currentDateTime)) {
            throw new BadRequestException(ErrorMessage.CANNOT_SELECT_PAST_DATETIME);
        }
    }

    private void validateDuplicateWaiting(ReservationCreateCommand request) {
        if (waitingRepository.existsByNameAndDateAndThemeIdAndTimeId(
                request.name(), request.date(), request.themeId(), request.timeId())) {
            throw new RoomEscapeException(ReservationErrorCode.DUPLICATE_WAITING);
        }
    }
}
