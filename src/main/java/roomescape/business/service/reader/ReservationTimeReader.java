package roomescape.business.service.reader;

import roomescape.business.dto.ReservableReservationTimeDto;
import roomescape.business.dto.ReservationTimeDto;

import java.time.LocalDate;
import java.util.List;

public interface ReservationTimeReader {

    List<ReservationTimeDto> getAll();

    List<ReservableReservationTimeDto> getAllWithAvailableBy(LocalDate date, String themeIdValue);
}
