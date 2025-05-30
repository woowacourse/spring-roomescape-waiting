package roomescape.application.reservation;

import java.time.LocalDate;
import org.springframework.stereotype.Component;
import roomescape.application.support.exception.NotFoundEntityException;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@Component
public class ReservationSlotAssembler {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationSlotAssembler(ReservationTimeRepository reservationTimeRepository,
                                    ThemeRepository themeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationSlot assemble(LocalDate date, Long timeId, Long themeId) {
        return new ReservationSlot(date, getReservationTimeById(timeId), getThemeById(themeId));
    }

    private ReservationTime getReservationTimeById(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundEntityException(timeId + "에 해당하는 reservation_time 튜플이 없습니다."));
    }

    private Theme getThemeById(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundEntityException(themeId + "에 해당하는 theme 튜플이 없습니다."));
    }
}

