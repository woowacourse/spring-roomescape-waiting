package roomescape.reservation.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
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

        Status status = decideReservationStatus(command.date(), time.getId(), theme.getId());
        Reservation reservation = command.toEntity(time, theme, status, clock);

        return ReservationInfo.from(reservationRepository.save(reservation));

    }

    @Transactional
    public void cancel(Long id, String username) {
        Reservation reservation = reservationRepository.getById(id);
        if (!reservation.isOwner(username)) {
            throw new ForbiddenException("예약 취소 권한이 없습니다.");
        }

        reservationRepository.update(reservation.cancel());

        if (reservation.isReserved()) {
            Optional<Reservation> nextWaitingReservation = reservationRepository.findNextWaitingReservation(
                    reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());

            nextWaitingReservation.ifPresent(waiting -> reservationRepository.update(waiting.reserved()));
        }
    }

    @Transactional
    public ReservationInfo modify(Long id, ReservationChangeCommand command) {
        Reservation reservation = reservationRepository.getById(id);
        ReservationTime time = timeRepository.getById(command.timeId());
        Theme theme = themeRepository.getById(command.themeId());

        checkDuplicateReservation(command.username(), command.date(), time, theme);

        Status status = decideReservationStatus(command.date(), time.getId(), theme.getId());

        Reservation changedReservation = reservation.modify(command.date(), time, theme, status, clock);
        reservationRepository.update(changedReservation);

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

    private Long findWaitingOrder(Reservation reservation) {
        if (!reservation.isWaiting()) {
            return null;
        }
        return reservationRepository.countWaitingBefore(reservation) + 1;
    }

    private Status decideReservationStatus(LocalDate date, Long timeId, Long themeId) {
        if (reservationRepository.existsActiveReservationByDateTimeAndTheme(timeId, themeId, date)) {
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
