package roomescape.reservation.service.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class WaitingQueryUseCase {

    private final WaitingRepository waitingRepository;

    public Waiting get(Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약 대기 정보를 찾을 수 없습니다."));
    }

    public boolean existsByParams(ReservationDate date, Long timeId, Long themeId, Long memberId) {
        return waitingRepository.existsByParams(date, timeId, themeId, memberId);
    }

    public List<Waiting> getByMemberId(Long memberId) {
        return waitingRepository.findAllByMemberId(memberId);
    }
}
