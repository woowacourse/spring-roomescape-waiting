package roomescape.time.service.usecase;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.NotFoundException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.time.repository.ReservationTimeRepository;

@Service
@RequiredArgsConstructor
public class ReservationTimeQueryUseCase {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTime get(final ReservationTimeId id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("조회할 시간을 찾을 수 없습니다."));
    }

    public List<ReservationTime> getAll() {
        return reservationTimeRepository.findAll();
    }

    public boolean existsByStartAt(final LocalTime startAt) {
        return reservationTimeRepository.existsByStartAt(startAt);
    }

}
