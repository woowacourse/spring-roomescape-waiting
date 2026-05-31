package roomescape.time.application;

import java.time.Clock;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.application.dto.AvailableReservationTimeFindCommand;
import roomescape.time.application.dto.AvailableReservationTimeInfo;
import roomescape.time.application.dto.ReservationTimeCommand;
import roomescape.time.application.dto.ReservationTimeInfo;
import roomescape.time.application.exception.DuplicateReservationTimeException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.application.exception.ReservationTimeInUseException;
import roomescape.time.domain.ReservationTimeRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationTimeService {

    private final Clock clock;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    @Transactional(readOnly = true)
    public List<ReservationTimeInfo> getReservationTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeInfo::from)
                .toList();
    }

    public ReservationTimeInfo addReservationTime(ReservationTimeCommand timeCommand) {
        if (reservationTimeRepository.existsByStartAt(timeCommand.startAt())) {
            throw new DuplicateReservationTimeException("이미 존재하는 시간입니다.");
        }
        ReservationTime time = reservationTimeRepository.save(timeCommand.toEntity());
        return ReservationTimeInfo.from(time);
    }

    public void deleteReservationTime(Long id) {
        if (reservationRepository.existsByReservationTime(id)) {
            throw new ReservationTimeInUseException("해당 시간에 예약이 존재합니다.");
        }
        ReservationTime time = reservationTimeRepository.getById(id)
                .delete(clock);
        reservationTimeRepository.delete(time);
    }

    @Transactional(readOnly = true)
    public AvailableReservationTimeInfo getAvailableReservationTime(AvailableReservationTimeFindCommand command) {
        Theme theme = themeRepository.getById(command.themeId());
        List<Reservation> reservations = reservationRepository.findByThemeAndDate(
                command.themeId(), command.date());
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        Set<ReservationTime> reservedTimes = reservations.stream()
                .map(Reservation::getTime)
                .collect(Collectors.toSet());
        List<ReservationTime> availableTime = allTimes.stream()
                .filter(time -> !reservedTimes.contains(time))
                .toList();
        return AvailableReservationTimeInfo.from(theme, availableTime);
    }
}
