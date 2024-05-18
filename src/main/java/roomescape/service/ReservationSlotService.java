package roomescape.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.slot.ReservationSlot;
import roomescape.domain.reservation.slot.ReservationTime;
import roomescape.domain.reservation.slot.ReservationTimeRepository;
import roomescape.domain.reservation.slot.Theme;
import roomescape.domain.reservation.slot.ThemeRepository;
import roomescape.exception.RoomEscapeBusinessException;
import roomescape.service.dto.ReservationSlotRequest;

@Service
public class ReservationSlotService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationSlotService(ReservationTimeRepository reservationTimeRepository,
                                  ThemeRepository themeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public ReservationSlot findSlot(ReservationSlotRequest reservationSlotRequest) {
        LocalDate date = reservationSlotRequest.date();
        ReservationTime time = findTimeById(reservationSlotRequest.timeId());
        Theme theme = findThemeById(reservationSlotRequest.themeId());

        return new ReservationSlot(date, time, theme);
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));
    }

    private ReservationTime findTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약 시간입니다."));
    }
}
