package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.request.ServiceReservationTimeCreateRequest;

@Component
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final Clock clock;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, Clock clock) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.clock = clock;
    }

    @Transactional
    public ReservationTime save(ServiceReservationTimeCreateRequest request) {
        validateDuplicatedReservationTime(request.startAt());
        return reservationTimeRepository.save(request.toEntity());
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public List<Long> findReservedTimeIdsByDateAndTheme(LocalDate date, Long themeId) {
        return reservationTimeRepository.findReservedTimeIdByDateAndTheme(date, themeId);
    }

    public void validateNotPastDate(LocalDate date) {
        if (date.isBefore(LocalDate.now(clock))) {
            throw new CustomInvalidRequestException(ErrorCode.PAST_RESERVATION_TIME_READ);
        }
    }

    public void validateNotPastSlotForCreate(LocalDate date, ReservationTime time) {
        if (isPastSlot(date, time)) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_CREATE);
        }
    }

    public void validateNotPastSlotForDelete(LocalDate date, ReservationTime time) {
        if (isPastSlot(date, time)) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_PAST_TIME_RESERVATION_DELETE);
        }
    }

    @Transactional
    public void delete(Long id) {
        reservationTimeRepository.delete(id);
    }

    public ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_RESERVATION_TIME));
    }

    private boolean isPastSlot(LocalDate date, ReservationTime time) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        if (date.isBefore(nowDate)) {
            return true;
        }
        if (date.isAfter(nowDate)) {
            return false;
        }
        return time.isPast(nowTime);
    }

    private void validateDuplicatedReservationTime(LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new CustomInvalidRequestException(ErrorCode.DUPLICATED_RESERVATION_TIME);
        }
    }
}
