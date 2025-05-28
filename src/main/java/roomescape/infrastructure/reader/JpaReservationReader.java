package roomescape.infrastructure.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.MyReservationDto;
import roomescape.business.dto.ReservationDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.vo.Id;
import roomescape.business.service.reader.ReservationReader;
import roomescape.infrastructure.repository.dao.JpaReservationSlotDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaReservationReader implements ReservationReader {

    private final JpaReservationSlotDao slotDao;

    @Override
    public List<ReservationDto> getAll(final String themeIdValue, final String userIdValue, final LocalDate dateFrom, final LocalDate dateTo) {
        List<ReservationSlot> slots = slotDao.findAllBy(Id.create(themeIdValue), Id.create(userIdValue), dateFrom, dateTo);
        return slots.stream()
                .map(ReservationSlot::getReservedReservation)
                .filter(reservation -> {
                    if (userIdValue == null) {
                        return true;
                    } else {
                        return reservation.isSameReserver(userIdValue);
                    }
                })
                .map(ReservationDto::fromEntity)
                .toList();
    }

    @Override
    public List<MyReservationDto> getMyReservations(final String userIdValue) {
        List<ReservationSlot> slots = slotDao.findAllBy(null, Id.create(userIdValue), null, null);
        Map<Reservation, Integer> waitingNumberAndReservation = ReservationSlot.toWaitingNumberAndReservation(slots, Id.create(userIdValue));
        return MyReservationDto.fromMap(waitingNumberAndReservation);
    }
}
