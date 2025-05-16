package roomescape.service.reservationtime;

import java.time.LocalDate;
import java.util.List;
import roomescape.dto.reservationtime.AvailableTimeResponse;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;

public interface ReservationTimeService {

    ReservationTimeResponse create(ReservationTimeRequest request);

    List<ReservationTimeResponse> getAll();

    void deleteById(Long id);

    List<AvailableTimeResponse> getAvailableTimes(LocalDate date, Long themeId);
}
