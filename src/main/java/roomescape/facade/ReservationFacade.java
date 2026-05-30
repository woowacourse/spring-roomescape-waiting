package roomescape.facade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationUpdateRequest;
import roomescape.dto.ReservationWaitingRequest;
import roomescape.dto.TimeWithStatusResponse;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.service.ReservationWaitingService;
import roomescape.service.ThemeService;

@Component
public class ReservationFacade {

    private static final String CANNOT_DELETE_TIME_IN_USE = "ID %d번 시간을 사용 중인 예약이 존재하여 시간을 삭제할 수 없습니다.";
    private static final String CANNOT_DELETE_THEME_IN_USE = "ID %d번 테마를 사용 중인 예약이 존재하여 테마를 삭제할 수 없습니다.";
    private static final String PAST_RESERVATION_REJECTED = "지난 시각에는 예약할 수 없습니다.";
    private static final String PAST_RESERVATION_WAITING_REJECTED = "지난 시각에는 대기할 수 없습니다.";
    private static final String OWNER_CANNOT_WAIT = "본인이 예약한 슬롯에는 대기를 신청할 수 없습니다.";
    private static final String ALREADY_WAITING = "이미 대기를 신청한 예약입니다.";

    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ReservationWaitingService reservationWaitingService;
    private final ThemeService themeService;

    public ReservationFacade(
            ReservationService reservationService,
            ReservationTimeService reservationTimeService,
            ReservationWaitingService reservationWaitingService,
            ThemeService themeService
    ) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.reservationWaitingService = reservationWaitingService;
        this.themeService = themeService;
    }

    // TODO: delete cascade 불가는 db제약으로 풀어내기
    @Transactional
    public void deleteTime(Long id) {
        if (reservationService.hasReservationsByTimeId(id)) {
            throw new BusinessRuleViolationException(String.format(CANNOT_DELETE_TIME_IN_USE, id));
        }
        reservationTimeService.deleteTime(id);
    }

    @Transactional
    public void deleteTheme(Long id) {
        if (reservationService.hasReservationsByThemeId(id)) {
            throw new BusinessRuleViolationException(String.format(CANNOT_DELETE_THEME_IN_USE, id));
        }
        themeService.deleteTheme(id);
    }

    @Transactional
    public Reservation addReservation(ReservationRequest request) {
        ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());

        Reservation reservation = new Reservation(
                request.name(),
                request.date(),
                reservationTime,
                theme
        );

        if (reservation.isPast(LocalDateTime.now())) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_REJECTED);
        }
        reservationService.validateConflict(request.date(), reservationTime.getId(), theme.getId());

        return reservationService.addReservation(reservation);
    }

    @Transactional
    public Reservation updateMyReservation(Long id, String name, ReservationUpdateRequest request) {
        Reservation existing = reservationService.getById(id);
        ReservationTime newTime = reservationTimeService.findById(request.timeId());

        LocalDateTime now = LocalDateTime.now();
        Reservation updated = existing.updateWith(
                name,
                request.date(),
                newTime,
                now
        );

        if (!existing.isSameSlot(request.date(), newTime)) {
            reservationService.validateConflict(request.date(), newTime.getId(), existing.getTheme().getId());
        }

        return reservationService.updateReservation(updated);
    }

    @Transactional
    public ReservationWaiting addWaiting(ReservationWaitingRequest request) {
        Reservation reservation = reservationService.getById(request.reservationId());
        if (reservation.isOwnedBy(request.name())) {
            throw new BusinessRuleViolationException(OWNER_CANNOT_WAIT);
        }
        if (reservation.isPast(LocalDateTime.now())) {
            throw new BusinessRuleViolationException(PAST_RESERVATION_WAITING_REJECTED);
        }
        if (reservationWaitingService.existBy(request.name(), reservation.getId())) {
            throw new BusinessRuleViolationException(ALREADY_WAITING);
        }

        ReservationWaiting reservationWaiting = new ReservationWaiting(
                request.name(),
                LocalDateTime.now(),
                reservation
        );

        return reservationWaitingService.addWaiting(reservationWaiting);
    }

    public List<TimeWithStatusResponse> getTimesWithAvailability(LocalDate date, Long themeId) {
        List<ReservationTime> times = reservationTimeService.getReservationTimes();
        Set<Long> reservedTimeIds = reservationService.getReservedTimeIds(date, themeId);

        return times.stream()
                .map(time -> TimeWithStatusResponse.from(time, reservedTimeIds.contains(time.getId())))
                .toList();
    }
}
