package roomescape.time.service.usecase;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.NotFoundException;
import roomescape.time.domain.ReservationTime;
import roomescape.time.repository.ReservationTimeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationTimeQueryUseCase {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTime get(final Long id) {
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
