package roomescape.reservationtime.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.repository.JpaReservationTimeRepository;
import roomescape.theme.service.ThemeService;

@Service
public class ReservationTimeService {

    private final JpaReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeService themeService;

    public ReservationTimeService(
            final JpaReservationTimeRepository reservationTimeRepository,
            final ReservationRepository reservationRepository,
            final ThemeService themeService
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
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

        return reservationTimeRepository.save(reservationTime);
    }

    public List<ReservationTime> findAvailableTimes(final LocalDate date, final long themeId) {
        themeService.getById(themeId);
        Set<Long> reservedTimeIds = Set.copyOf(reservationRepository.findReservedTimeIdsByDateAndThemeId(date, themeId));

        return reservationTimeRepository.findAll().stream()
                .filter(reservationTime -> !reservedTimeIds.contains(reservationTime.getId()))
                .filter(reservationTime -> isAvailableOn(date, reservationTime))
                .toList();
    }

    public void deleteById(final long timeId) {
        if (reservationRepository.existsByTimeId(timeId)) {
            throw new ConflictException(ErrorCode.RESERVATION_TIME_IN_USE, "이미 예약된 시간은 삭제할 수 없습니다.");
        }
        int affectedRowCount = reservationTimeRepository.deleteById(timeId);

        if(affectedRowCount <= 0) {
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

    private boolean isAvailableOn(final LocalDate date, final ReservationTime reservationTime) {
        LocalDate today = LocalDate.now();

        if (date.isBefore(today)) {
            return false;
        }

        if (date.isAfter(today)) {
            return true;
        }

        return !reservationTime.getStartAt().isBefore(LocalTime.now());
    }
}
