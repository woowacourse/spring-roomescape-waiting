package roomescape.reservation.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.BadRequestException;
import roomescape.global.ConflictException;
import roomescape.global.ForbiddenException;
import roomescape.global.NotFoundException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.exception.ReservationErrorMessage;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.event.schema.WaitingSaved;
import roomescape.reservation.presentation.dto.WaitingResponse;
import roomescape.reservationtime.exception.ReservationTimeErrorMessage;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.theme.exception.ThemeErrorMessage;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@RequiredArgsConstructor
@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
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

        if (reservationRepository.existsByNameAndDateAndThemeAndTime(
                pending.getName(),
                pending.getDate(),
                pending.getThemeId(),
                pending.getTimeId())) {
            throw new ConflictException(ReservationErrorMessage.ALREADY_RESERVED_CANNOT_WAIT);
        }

        if (waitingRepository.existsByNameAndDateAndThemeIdAndTimeId(
                pending.getName(),
                pending.getDate(),
                pending.getThemeId(),
                pending.getTimeId())) {
            throw new ConflictException(ReservationErrorMessage.DUPLICATE_WAITING);
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
                .orElseThrow(() -> new NotFoundException(ReservationErrorMessage.WAITING_NOT_FOUND, id));

        if (!waiting.isOwner(name)) {
            throw new ForbiddenException(ReservationErrorMessage.FORBIDDEN_WAITING_ACCESS);
        }

        return waitingRepository.delete(id);
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ThemeErrorMessage.THEME_NOT_FOUND, id));
    }

    private ReservationTime findTimeById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ReservationTimeErrorMessage.TIME_NOT_FOUND, id));
    }

    private void validateReservationDateTime(LocalDate date, LocalTime startAt, LocalDateTime currentDateTime) {
        LocalDateTime triedDateTime = LocalDateTime.of(date, startAt);

        if (triedDateTime.isBefore(currentDateTime)) {
            throw new BadRequestException(ReservationErrorMessage.CANNOT_SELECT_PAST_DATETIME);
        }
    }
}
