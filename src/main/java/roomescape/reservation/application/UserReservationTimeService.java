package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.application.dto.response.ReservationTimeServiceResponse;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.repository.ReservationTimeRepository;

@Service
@RequiredArgsConstructor
public class UserReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public List<ReservationTimeServiceResponse> getAllWithStatus(Long themeId, LocalDate date) {
        List<ReservationTime> allTimes = reservationTimeRepository.getAll();
        List<ReservationTime> bookedTimes = reservationTimeRepository.getAllByThemeIdAndDate(themeId, date);

        return allTimes.stream()
                .map(time -> ReservationTimeServiceResponse.of(time, bookedTimes.contains(time)))
                .toList();
    }
}
