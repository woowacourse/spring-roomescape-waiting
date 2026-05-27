package roomescape.reservation.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationPendingInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.application.exception.ReservationInUseException;
import roomescape.reservation.application.exception.ReservationNotFoundException;
import roomescape.reservation.domain.Status;
import roomescape.reservation.domain.dto.ReservationQueryResult;
import roomescape.reservation.domain.exception.DuplicatedReservationException;
import roomescape.reservation.domain.exception.IllegalStateReservationException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReservationService {

    private static final int DELETE_ROW_COUNTS = 0;

    private final Clock clock;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;

    @Transactional(readOnly = true)
    public List<ReservationInfo> getReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationInfo::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationPendingInfo> getReservationsByName(final String username) {
        List<ReservationQueryResult> results =
                reservationRepository.findAllByName(username);

        return results.stream()
                .map(ReservationPendingInfo::from)
                .toList();
    }

    public ReservationInfo addReservation(final ReservationCreateCommand command) {
        ReservationTime time = timeRepository.getById(command.timeId());
        time.checkValidDateTime(command.date(), clock);
        Theme theme = themeRepository.getById(command.themeId());
        Reservation reservation = command.toEntity(time, theme, clock);
        if (reservationRepository.existsByReservationTimeAndThemeAndDate(time.getId(), theme.getId(), command.date())) {
            checkDuplicatePendingReservation(command.date(), command.name(), time, theme);
            reservation = reservation.pending(
                    command.name(),
                    command.date(),
                    time,
                    theme,
                    clock
            );
        }
        try {
            return ReservationInfo.from(reservationRepository.save(reservation));
        } catch (DataIntegrityViolationException e) {
            throw new ReservationInUseException("이미 예약이 존재합니다.");
        }
    }

    public void hardDeleteReservation(final Long id) {
        if (reservationRepository.deleteById(id) == DELETE_ROW_COUNTS) {
            throw new ReservationNotFoundException("존재하지 않는 예약ID 입니다.");
        }
    }

    public void cancelReservation(final Long id, final String username) {
        if (!reservationRepository.existsByIdAndUsernameAndActiveOrPending(id, username)) {
            throw new ReservationNotFoundException("해당 예약을 찾을 수 없거나 취소할 권한이 없습니다.");
        }
        Reservation reservation = reservationRepository.getById(id);
        Reservation canceledReservation = reservation.cancel();
        reservationRepository.cancel(canceledReservation);
        if (reservation.getStatus().equals(Status.ACTIVE)) {
            Optional<Reservation> nextPendingReservation = reservationRepository.findNextPendingReservation(
                    reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
            nextPendingReservation.ifPresent(pending -> {
                Reservation activeReservation = pending.active();
                reservationRepository.updateById(activeReservation.getId(), activeReservation);
            });
        }
    }

    public ReservationInfo changeReservation(final Long id, final ReservationChangeCommand command) {
        Reservation reservation = reservationRepository.getById(id);
        ReservationTime time = timeRepository.getById(command.timeId());
        Theme theme = themeRepository.getById(command.themeId());
        if (reservationRepository.existsByReservationTimeAndThemeAndDateAndIdNot(id, time.getId(), theme.getId(),
                command.date())) {
            throw new ReservationInUseException("이미 다른 예약이 존재합니다.");
        }
        Reservation changedReservation = reservation.changeTime(command.username(), command.date(), time, theme, clock);
        reservationRepository.updateById(id, changedReservation);
        return ReservationInfo.from(changedReservation);
    }

    public ReservationInfo changeReservationStatusToPending(final Long id, final ReservationChangeCommand command) {
        Reservation reservation = reservationRepository.getById(id);
        ReservationTime time = timeRepository.getById(command.timeId());
        Theme theme = themeRepository.getById(command.themeId());
        if (!reservationRepository.existsActiveReservationByThemeAndTime(time.getId(), theme.getId(),
                command.date())) {
            throw new IllegalStateReservationException("대기 상태로 변경할 수 없습니다.");
        }
        checkDuplicatePendingReservation(command.date(), command.username(), time, theme);
        Reservation pendingReservation = reservation.pending(command.username(), command.date(), time, theme, clock);
        reservationRepository.updateById(id, pendingReservation);
        return ReservationInfo.from(pendingReservation);
    }

    private void checkDuplicatePendingReservation(final LocalDate date, final String username, final ReservationTime time, final Theme theme) {
        if (reservationRepository.existsPendingReservationByName(time.getId(), theme.getId(), date, username)) {
            throw new DuplicatedReservationException("이미 예약 대기 중입니다.");
        }
    }
}
