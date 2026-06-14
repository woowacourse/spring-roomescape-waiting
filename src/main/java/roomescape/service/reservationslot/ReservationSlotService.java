package roomescape.service.reservationslot;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationslot.ReservationSlot;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.InvalidInputException;
import roomescape.repository.PersistenceConflictException;
import roomescape.repository.reservationslot.ReservationSlotRepository;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.service.theme.ThemeService;

@Service
public class ReservationSlotService {
    private final ReservationSlotRepository reservationSlotRepository;
    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;

    public ReservationSlotService(
            final ReservationSlotRepository reservationSlotRepository,
            final ThemeService themeService,
            final ReservationTimeService reservationTimeService
    ) {
        this.reservationSlotRepository = reservationSlotRepository;
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
    }

    public List<ReservationSlot> getAll() {
        return reservationSlotRepository.findAll();
    }

    public ReservationSlot open(final LocalDate date, final long themeId, final long timeId) {
        Theme theme = themeService.getById(themeId);
        ReservationTime time = reservationTimeService.getById(timeId);
        ReservationSlot slot = createSlot(date, theme, time);

        if (reservationSlotRepository.findBySlot(slot).isPresent()) {
            throw new ConflictException(ErrorCode.RESERVATION_SLOT_DUPLICATED, "이미 열린 예약 슬롯입니다.");
        }

        try {
            return reservationSlotRepository.save(slot);
        } catch (PersistenceConflictException exception) {
            throw new ConflictException(ErrorCode.RESERVATION_SLOT_DUPLICATED, "이미 열린 예약 슬롯입니다.");
        }
    }

    private ReservationSlot createSlot(final LocalDate date, final Theme theme, final ReservationTime time) {
        try {
            return new ReservationSlot(date, theme, time);
        } catch (IllegalArgumentException exception) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, exception.getMessage());
        }
    }

}
