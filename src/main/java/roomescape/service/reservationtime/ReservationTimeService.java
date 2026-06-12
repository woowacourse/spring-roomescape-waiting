package roomescape.service.reservationtime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.PersistenceConflictException;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.repository.reservationslot.ReservationSlotRepository;
import roomescape.repository.reservationtime.ReservationTimeRepository;
import roomescape.service.theme.ThemeService;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final ThemeService themeService;

    public ReservationTimeService(
            final ReservationTimeRepository reservationTimeRepository,
            final ReservationRepository reservationRepository,
            final ReservationSlotRepository reservationSlotRepository,
            final ThemeService themeService
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.reservationSlotRepository = reservationSlotRepository;
        this.themeService = themeService;
    }

    public ReservationTime save(final LocalTime startAt) {
        ReservationTime reservationTime;
        try {
            reservationTime = ReservationTime.createNew(startAt);
        } catch (IllegalArgumentException exception) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, exception.getMessage());
        }

        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new ConflictException(ErrorCode.RESERVATION_TIME_DUPLICATED, "같은 시간을 추가할 수 없습니다.");
        }

        try {
            return reservationTimeRepository.save(reservationTime);
        } catch (PersistenceConflictException exception) {
            throw new ConflictException(ErrorCode.RESERVATION_TIME_DUPLICATED, "같은 시간을 추가할 수 없습니다.");
        }
    }

    public List<ReservationTime> findAvailableTimes(
            final LocalDate date,
            final long themeId,
            final LocalDateTime requestedAt
    ) {
        Theme theme = themeService.getById(themeId);
        Set<Long> reservedSlotIds = reservationRepository.findByDateAndTheme(date, theme)
                .stream()
                .map(Reservation::getSlot)
                .map(ReservationSlot::getId)
                .collect(Collectors.toSet());

        return reservationSlotRepository.findByDateAndTheme(date, theme)
                .stream()
                .filter(slot -> !reservedSlotIds.contains(slot.getId()))
                .filter(slot -> !slot.isPast(requestedAt))
                .map(ReservationSlot::getTime)
                .toList();
    }

    public void deleteById(final long timeId) {
        ReservationTime reservationTime = getById(timeId);
        if (reservationRepository.existsByTime(reservationTime)) {
            throw new ConflictException(ErrorCode.RESERVATION_TIME_IN_USE, "이미 예약된 시간은 삭제할 수 없습니다.");
        }

        int affectedRowCount;
        try {
            affectedRowCount = reservationTimeRepository.deleteById(timeId);
        } catch (PersistenceConflictException exception) {
            throw new ConflictException(ErrorCode.RESERVATION_TIME_IN_USE, "이미 예약된 시간은 삭제할 수 없습니다.");
        }

        if (affectedRowCount <= 0) {
            throw new ResourceNotFoundException(ErrorCode.RESERVATION_TIME_NOT_FOUND, "삭제된 시간 데이터가 없습니다.");
        }
    }

    public List<ReservationTime> getAll() {
        return reservationTimeRepository.findAll();
    }

    public ReservationTime getById(final long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_TIME_NOT_FOUND, "찾는 시간이 없습니다"));
    }

}
