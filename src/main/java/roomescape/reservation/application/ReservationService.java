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

        if(reservationRepository.existsByReservationTimeAndThemeAndDate(time.getId(), theme.getId(), command.date())) {
            return savePendingReservation(command, time, theme);
        }
        try {
            Reservation reservation = command.toEntity(time, theme, clock);
            return ReservationInfo.from(reservationRepository.save(reservation));
        } catch (DataIntegrityViolationException e) {
            return savePendingReservation(command, time, theme);
        }
    }

    public void hardDeleteReservation(final Long id) {
        if (reservationRepository.deleteById(id) == DELETE_ROW_COUNTS) {
            throw new ReservationNotFoundException("존재하지 않는 예약ID 입니다.");
        }
    }

    public void cancelReservation(final Long id, final String username) {
        Reservation reservation = reservationRepository.getById(id);
        Reservation canceledReservation = reservation.cancel(username, clock);
        reservationRepository.cancel(canceledReservation);
        if (reservation.getStatus().equals(Status.ACTIVE)) {
            promoteNextPendingReservation(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        }
    }

    public ReservationInfo changeReservation(final Long id, final ReservationChangeCommand command) {
        Reservation reservation = reservationRepository.getById(id);
        ReservationTime time = timeRepository.getById(command.timeId());
        Theme theme = themeRepository.getById(command.themeId());

        boolean isSlotChanged = !reservation.getDate().equals(command.date())
                || !reservation.getTime().getId().equals(command.timeId())
                || !reservation.getTheme().getId().equals(command.themeId());

        ReservationInfo reservationInfo = attemptToChangeReservation(id, command, time, theme, reservation);

        if (reservation.getStatus().equals(Status.ACTIVE) && isSlotChanged) {
            promoteNextPendingReservation(reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId());
        }
        return reservationInfo;
    }

    private ReservationInfo attemptToChangeReservation(Long id, ReservationChangeCommand command, ReservationTime time,
                                               Theme theme, Reservation reservation) {
        if (reservationRepository.existsByReservationTimeAndThemeAndDateAndIdNot(id, time.getId(), theme.getId(),
                command.date())) {
            return changePendingReservation(reservation, command, time, theme);
        }
        try {
            Reservation changedReservation = reservation.changeTime(command.name(), command.date(), time, theme, Status.ACTIVE, clock);
            reservationRepository.updateDetails(changedReservation.getId(), changedReservation);
            return ReservationInfo.from(changedReservation);
        } catch (DataIntegrityViolationException e) {
            return changePendingReservation(reservation, command, time, theme);
        }
    }

    private ReservationInfo changePendingReservation(final Reservation reservation, final ReservationChangeCommand command, final ReservationTime time, Theme theme) {
        checkDuplicatePendingReservation(command.date(), command.name(), time, theme);
        Reservation changedReservation = reservation.changeTime(command.name(), command.date(), time, theme, Status.PENDING,
                clock);
        reservationRepository.updateDetails(reservation.getId(), changedReservation);
        return ReservationInfo.from(changedReservation);
    }

    private ReservationInfo savePendingReservation(ReservationCreateCommand command, ReservationTime time, Theme theme) {
        checkDuplicatePendingReservation(command.date(), command.name(), time, theme);
        Reservation pendingReservation = command.toEntity(time, theme, clock)
                .pending(command.name(), clock);
        try {
            return ReservationInfo.from(reservationRepository.save(pendingReservation));
        } catch (DataIntegrityViolationException e) {
            throw new ReservationInUseException("예약 처리 중 일시적인 문제가 발생했습니다. 다시 시도해주세요.");
        }
    }

    private void checkDuplicatePendingReservation(final LocalDate date, final String name, final ReservationTime time, final Theme theme) {
        if (reservationRepository.existsPendingReservationByName(time.getId(), theme.getId(), date, name)) {
            throw new DuplicatedReservationException("이미 예약 대기 중입니다.");
        }
    }

    private void promoteNextPendingReservation(final LocalDate date, final Long timeId, final Long themeId) {
        Optional<Reservation> nextPendingReservation = reservationRepository.findNextPendingReservation(date, timeId, themeId);
        nextPendingReservation.ifPresent(pending -> {
                Reservation activeReservation = pending.active();
                reservationRepository.promoteToActive(activeReservation.getId());
        });
    }
}
