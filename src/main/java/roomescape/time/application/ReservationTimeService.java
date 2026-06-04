package roomescape.time.application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.DuplicateException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.time.application.dto.AvailableReservationTimeFindCommand;
import roomescape.time.application.dto.AvailableReservationTimeInfo;
import roomescape.time.application.dto.ReservationTimeCommand;
import roomescape.time.application.dto.ReservationTimeInfo;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    @Transactional
    public ReservationTimeInfo create(ReservationTimeCommand command) {
        if (reservationTimeRepository.existsActiveByStartAt(command.startAt())) {
            throw new DuplicateException("이미 존재하는 시간입니다.");
        }

        ReservationTime time = reservationTimeRepository.save(command.toEntity());
        return ReservationTimeInfo.from(time);
    }

    @Transactional
    public void deactivate(Long id) {
        ReservationTime time = reservationTimeRepository.getById(id);

        if (reservationRepository.existsByReservationTime(id)) {
            throw new ConflictException("예약이 존재하는 시간대는 비활성화할 수 없습니다.");
        }

        reservationTimeRepository.update(time.deactivate());
    }

    public List<ReservationTimeInfo> getReservationTimes(int page, int size) {
        return reservationTimeRepository.findAll(page, size)
                .stream()
                .map(ReservationTimeInfo::from)
                .toList();
    }

    public AvailableReservationTimeInfo getAvailableReservationTime(AvailableReservationTimeFindCommand command) {
        Theme theme = themeRepository.getById(command.themeId());

        List<Reservation> reservations = reservationRepository.findByThemeAndDate(theme.getId(), command.date());
        List<ReservationTime> allTimes = reservationTimeRepository.findAllActive();

        Set<ReservationTime> reservedTimes = reservations.stream()
                .map(Reservation::getTime)
                .collect(Collectors.toSet());

        List<ReservationTime> availableTime = allTimes.stream()
                .filter(time -> !reservedTimes.contains(time))
                .toList();

        return AvailableReservationTimeInfo.from(theme, availableTime);
    }
}
