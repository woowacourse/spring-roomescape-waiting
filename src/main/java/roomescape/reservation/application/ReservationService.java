package roomescape.reservation.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.ForbiddenException;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationWaitingInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.Status;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final Clock clock;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public ReservationInfo create(ReservationCreateCommand command) {
        ReservationTime time = timeRepository.getById(command.timeId());
        Theme theme = themeRepository.getById(command.themeId());

        checkDuplicateReservation(command.name(), command.date(), time, theme);

        Status status = decideReservationStatus(command.date(), time, theme);
        Reservation reservation = command.toEntity(time, theme, status, clock);

        try {
            return ReservationInfo.from(reservationRepository.save(reservation));
        } catch (DuplicateException e) {
            throw new ConflictException("이미 예약된 날짜와 시간대입니다.");
        }
    }

    @Transactional
    public void cancel(Long id, String username) {
        Reservation reservation = reservationRepository.getById(id);
        validateOwner(username, reservation);

        reservationRepository.update(reservation.cancel());

        if (reservation.isReserved()) {
            promoteToReservation(reservation.getDate(), reservation.getTime(), reservation.getTheme());
        }
    }

    @Transactional
    public ReservationInfo modify(Long id, ReservationChangeCommand command) {
        Reservation reservation = reservationRepository.getById(id);
        ReservationTime time = timeRepository.getById(command.timeId());
        Theme theme = themeRepository.getById(command.themeId());

        validateOwner(command.username(), reservation);
        checkDuplicateReservation(command.username(), command.date(), time, theme);

        Status status = decideReservationStatus(command.date(), time, theme);
        Reservation changedReservation = reservation.modify(command.date(), time, theme, status, clock);
        try {
            reservationRepository.update(changedReservation);
        } catch (DuplicateException e) {
            throw new ConflictException("이미 예약된 날짜와 시간대입니다.");
        }

        if (reservation.isReserved()) {
            promoteToReservation(reservation.getDate(), reservation.getTime(), reservation.getTheme());
        }

        return ReservationInfo.from(changedReservation);
    }

    public List<ReservationInfo> getReservations(int page, int size) {
        return reservationRepository.findAll(page, size)
                .stream()
                .map(ReservationInfo::from)
                .toList();
    }

    public List<ReservationWaitingInfo> getReservationsByName(String username) {
        List<Reservation> reservations = reservationRepository.findAllByName(username);

        return reservations.stream()
                .map(reservation -> ReservationWaitingInfo.from(reservation, findWaitingOrder(reservation)))
                .toList();
    }

    private void validateOwner(String username, Reservation reservation) {
        if (!reservation.isOwner(username)) {
            throw new ForbiddenException("해당 예약에 대한 권한이 없습니다.");
        }
    }

    private void promoteToReservation(LocalDate date, ReservationTime time, Theme theme) {
        Optional<Reservation> nextWaitingReservation = reservationRepository.findNextWaitingReservation(date,
                time.getId(), theme.getId());
        try {
            nextWaitingReservation.ifPresent(waiting -> reservationRepository.update(waiting.reserved()));
        } catch (DuplicateException e) {
            throw new ConflictException("예약 상태가 변경되어 요청을 처리할 수 없습니다.");
        }
    }

    private Long findWaitingOrder(Reservation reservation) {
        if (!reservation.isWaiting()) {
            return null;
        }
        return reservationRepository.countWaitingBefore(reservation) + 1;
    }

    private Status decideReservationStatus(LocalDate date, ReservationTime time, Theme theme) {
        if (reservationRepository.existsActiveReservationByDateTimeAndTheme(time.getId(), theme.getId(), date)) {
            return Status.WAITING;
        }
        return Status.RESERVED;
    }

    private void checkDuplicateReservation(String username, LocalDate date, ReservationTime time, Theme theme) {
        if (reservationRepository.existsByUsernameAndDateTimeAndTheme(time.getId(), theme.getId(), date, username)) {
            throw new ConflictException("해당 날짜와 해당 시간대에 해당 이름으로 예약된 예약이 존재합니다.");
        }
    }
}
