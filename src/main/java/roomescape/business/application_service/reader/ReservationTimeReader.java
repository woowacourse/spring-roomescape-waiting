package roomescape.business.application_service.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.ReservableReservationTimeDto;
import roomescape.business.dto.ReservationTimeDto;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.repository.ReservationTimes;
import roomescape.business.model.vo.Id;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeReader {

    private final ReservationTimes reservationTimes;

    public List<ReservationTimeDto> getAll() {
        List<ReservationTime> reservationTimes = this.reservationTimes.findAll();
        return ReservationTimeDto.fromEntities(reservationTimes);
    }

    public List<ReservableReservationTimeDto> getAllBy(final LocalDate date, final String themeIdValue) {
        Id themeId = Id.create(themeIdValue);
        List<ReservationTime> available = reservationTimes.findAvailableByDateAndThemeId(date, themeId);
        List<ReservationTime> notAvailable = reservationTimes.findNotAvailableByDateAndThemeId(date, themeId);

        return ReservableReservationTimeDto.fromEntities(available, notAvailable);
    }
}
