package roomescape.reservationslot.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservationslot.exception.ReservationSlotAlreadyExistsException;
import roomescape.reservationslot.exception.ReservationSlotNotFoundException;
import roomescape.reservationslot.infrastructure.ReservationSlotRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Service
public class ReservationSlotDataService {

    private final ReservationSlotRepository reservationSlotRepository;

    public ReservationSlotDataService(final ReservationSlotRepository reservationSlotRepository) {
        this.reservationSlotRepository = reservationSlotRepository;
    }

    public void delete(Long id) {
        reservationSlotRepository.deleteById(id);
    }

    public void validateReservationSlotDoesNotExists(final LocalDate date, final Long timeId, final Long themeId) {
        if (reservationSlotRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new ReservationSlotAlreadyExistsException("해당 시간에 이미 예약 슬롯이 존재합니다.");
        }
    }

    public ReservationSlot getReservationSlotByDateAndTimeAndTheme(final LocalDate date, final Long timeId,
                                                                   final Long themeId) {
        return reservationSlotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId)
                .orElseThrow(() -> new ReservationSlotNotFoundException("해당 시간에 예약이 존재하지 않습니다."));
    }

    public ReservationSlot saveReservationSlotWithReservation(final Member member, final LocalDate date,
                                                              final ReservationTime time,
                                                              final Theme theme, final LocalDateTime now) {
        ReservationSlot reservationSlot = new ReservationSlot(date, time, theme);
        reservationSlot.addReservation(member, now);
        return reservationSlotRepository.save(reservationSlot);
    }

    public boolean existsByTimeId(final Long timeId) {
        return reservationSlotRepository.existsByTimeId(timeId);
    }

    public boolean existsByThemeId(final Long themeId) {
        return reservationSlotRepository.existsByThemeId(themeId);
    }

    public boolean hasOnlyOneReservation(final Long reservationSlotId) {
        return reservationSlotRepository.hasOnlyOneReservation(reservationSlotId);
    }
}
