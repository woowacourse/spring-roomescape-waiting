package roomescape.business.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.MyReservationDto;
import roomescape.business.dto.ReservationDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.vo.Id;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationReader {

    private final Reservations reservations;
    private final ReservationSlotReader slotReader;

    public List<ReservationDto> getAll(final String themeIdValue, final String userIdValue, final LocalDate dateFrom, final LocalDate dateTo) {
        List<Reservation> reservations = this.reservations.findAllWithFilter(Id.create(themeIdValue), Id.create(userIdValue), dateFrom, dateTo);
        return ReservationDto.fromEntities(reservations);
    }

    public List<MyReservationDto> getMyReservations(final String userIdValue) {
        List<ReservationSlot> slots = slotReader.getAllSlotsContainsReserverOf(userIdValue);
        Id userId = Id.create(userIdValue);
        Map<Reservation, Integer> reservationsWithWaitingNumber = slots.stream()
                .collect(Collectors.toMap(
                        slot -> slot.reservationOf(userId),
                        slot -> slot.waitingNumberOf(userId)
                ));
        return MyReservationDto.fromMap(reservationsWithWaitingNumber);
    }
}
