package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.dao.dto.WaitingQueryResult;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.UserName;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.time.ReservationTime;
import roomescape.service.dto.command.ReservationCommand;
import roomescape.service.dto.result.ReservationDetailResult;
import roomescape.service.dto.result.ReservationDetailResults;
import roomescape.service.dto.result.ReservationResult;

@Service
public class ReservationService {
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final WaitingDao waitingDao;
    private final Clock clock;

    public ReservationService(
            ReservationDao reservationDao,
            ReservationTimeDao reservationTimeDao,
            ThemeDao themeDao,
            WaitingDao waitingDao,
            Clock clock
    ) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.waitingDao = waitingDao;
        this.clock = clock;
    }

    public List<ReservationResult> findReservations() {
        List<Reservation> reservations = reservationDao.findAll();

        return reservations.stream()
                .map(ReservationResult::from)
                .toList();
    }

    public ReservationDetailResults findReservationDetailsByUserName(String userName) {
        List<Reservation> reservations = reservationDao.findAllByUserName(userName);
        List<WaitingQueryResult> waitings = waitingDao.findAllByUserName(userName);

        List<ReservationDetailResult> details = Stream.concat(
                reservations.stream().map(ReservationDetailResult::fromReservation),
                waitings.stream().map(ReservationDetailResult::fromWaiting)
        ).toList();

        return new ReservationDetailResults(details);
    }

    public ReservationResult reserve(ReservationCommand command) {
        Reservation reservation = convertToReservation(null, command);
        validateNoWaiting(command);
        Reservation reserved = reservationDao.save(reservation);
        return ReservationResult.from(reserved);
    }

    public ReservationResult changeReservationSlot(Long id, ReservationCommand command) {
        Reservation origin = getReservationOrThrow(id);
        origin.validateOwner(command.name());
        validatePastTime(origin.getDate(), origin.getTime());
        Reservation modified = convertToReservation(id, command);
        validateNoWaiting(command);

        boolean isSuccessful = reservationDao.update(modified);

        if (!isSuccessful) {
            throw new ConflictException("다른 사용자가 예약했습니다. 다시 시도해주세요.");
        }

        return ReservationResult.from(modified);
    }

    public void removeReservation(Long id) {
        reservationDao.delete(id);
    }

    public void cancelReservation(Long id, String userName) {
        Reservation origin = getReservationOrThrow(id);
        origin.validateOwner(userName);
        validatePastTime(origin.getDate(), origin.getTime());
        reservationDao.delete(id);
    }

    private Reservation convertToReservation(Long id, ReservationCommand command) {
        ReservationTime time = getReservationTimeOrThrow(command);
        Theme theme = getThemeOrThrow(command);
        validateAvailability(command.date(), time, theme);

        return new Reservation(
                id,
                UserName.parse(command.name()),
                command.date(),
                time,
                theme
        );
    }

    private void validateAvailability(LocalDate date, ReservationTime time, Theme theme) {
        validatePastTime(date, time);
        validateDuplicate(date, time, theme);
    }

    private void validatePastTime(LocalDate date, ReservationTime time) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime requestDateTime = LocalDateTime.of(date, time.getStartAt());
        if (requestDateTime.isBefore(now)) {
            throw new UnprocessableEntityException("이미 지난 시간입니다.");
        }
    }

    private void validateDuplicate(LocalDate date, ReservationTime time, Theme theme) {
        if (reservationDao.existsBy(date, theme, time)) {
            throw new ConflictException("이미 존재하는 예약 건입니다.");
        }
    }

    private void validateNoWaiting(ReservationCommand command) {
        if (waitingDao.existsBySlot(command.date(), command.timeId(), command.themeId())) {
            throw new ConflictException("이미 예약 대기자가 있는 시간입니다, 예약 대기로 신청해주세요.");
        }
    }

    private Reservation getReservationOrThrow(Long id) {
        return reservationDao.findById(id)
                .orElseThrow(() -> new NotFoundException("삭제하려는 예약이 존재하지 않습니다."));
    }

    private Theme getThemeOrThrow(ReservationCommand command) {
        return themeDao.findThemeById(command.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
    }

    private ReservationTime getReservationTimeOrThrow(ReservationCommand command) {
        return reservationTimeDao.findTimeById(command.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));
    }
}