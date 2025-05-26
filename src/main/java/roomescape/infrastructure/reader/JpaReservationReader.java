package roomescape.infrastructure.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.application_service.reader.ReservationReader;
import roomescape.business.dto.MyReservationDto;
import roomescape.business.dto.ReservationDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.vo.Id;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaReservationReader implements ReservationReader {

    private final Reservations reservations;

    @Override
    public List<ReservationDto> getAll(final String themeIdValue, final String userIdValue, final LocalDate dateFrom, final LocalDate dateTo) {
        List<Reservation> reservations = this.reservations.findAllReservedWithFilter(Id.create(themeIdValue), Id.create(userIdValue), dateFrom, dateTo);
        return ReservationDto.fromEntities(reservations);
    }

    @Override
    public List<MyReservationDto> getMyReservations(final String userIdValue) {
        Map<Reservation, Integer> reservationsWithWaitingNumber = this.reservations.findAllWithWaitingNumberByUserId(Id.create(userIdValue));
        return MyReservationDto.fromMap(reservationsWithWaitingNumber);
    }
}
