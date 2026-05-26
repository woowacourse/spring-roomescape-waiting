package roomescape.reservation.application;

import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.dto.ReservationChangeCommand;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.application.exception.ReservationInUseException;
import roomescape.reservation.application.exception.ReservationNotFoundException;
import roomescape.reservation.domain.exception.DuplicatedReservationException;
import roomescape.reservation.domain.exception.IllegalStateReservationException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

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
    public List<ReservationInfo> getReservationsByName(final String username) {
        return reservationRepository.findAllByName(username)
                .stream()
                .map(ReservationInfo::from)
                .toList();
    }

    public ReservationInfo addReservation(final ReservationCreateCommand command) {
        ReservationTime time = timeRepository.getById(command.timeId());
        time.checkValidDateTime(command.date(), clock);
        Theme theme = themeRepository.getById(command.themeId());
        if (reservationRepository.existsByReservationTimeAndThemeAndDate(time.getId(), theme.getId(), command.date())) {
            throw new ReservationInUseException("이미 예약이 존재합니다.");
        }
        try {
            return ReservationInfo.from(reservationRepository.save(command.toEntity(time, theme)));
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
        if (!reservationRepository.existsByIdAndUsernameAndActive(id, username)) {
            throw new ReservationNotFoundException("해당 예약을 찾을 수 없거나 취소할 권한이 없습니다.");
        }
        Reservation canceledReservation = reservationRepository.getById(id).cancel();
        reservationRepository.cancel(canceledReservation);
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

    public ReservationInfo changeReservationWaitingStatus(final Long id, final ReservationChangeCommand command) {
        Reservation reservation = reservationRepository.getById(id);
        ReservationTime time = timeRepository.getById(command.timeId());
        Theme theme = themeRepository.getById(command.themeId());

        if (!reservationRepository.existsActiveReservationByThemeAndTime(time.getId(), theme.getId(),
                command.date())) {
            throw new IllegalStateReservationException("대기 상태로 변경할 수 없습니다.");
        }

        if (reservationRepository.existsPendingReservationByName(time.getId(), theme.getId(), command.date(),
                command.username())) {
            throw new DuplicatedReservationException("이미 예약 대기 중입니다.");
        }

        Reservation pendingReservation = reservation.pending(command.username(), command.date(), time, theme, clock);
        reservationRepository.updateById(id, pendingReservation);
        return ReservationInfo.from(pendingReservation);
    }
}
