package roomescape.reservation.application;

import org.springframework.stereotype.Service;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.domain.ReservationSchedule;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Service
public class ReservationScheduleService {

    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationScheduleService(
        ThemeRepository themeRepository,
        ReservationTimeRepository reservationTimeRepository
    ) {
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationSchedule createReservationSchedule(MemberReservationRequest request) {
        Theme theme = findThemeById(request.themeId());
        ReservationTime reservationTime = findReservationTimeById(request.timeId());
        return new ReservationSchedule(request.date(), theme, reservationTime);
    }

    private ReservationTime findReservationTimeById(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
            .orElseThrow(() -> new NotFoundException("선택한 예약 시간이 존재하지 않습니다."));
    }

    private Theme findThemeById(final Long themeId) {
        return themeRepository.findById(themeId)
            .orElseThrow(() -> new NotFoundException("선택한 테마가 존재하지 않습니다."));
    }
}
