package roomescape.reservation.service.usecase;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.WaitingRepository;
import roomescape.reservation.service.dto.WaitingWithRank;

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

    public boolean existsByParams(ReservationDate date, Long timeId, Long themeId) {
        return waitingRepository.existsByParams(date, timeId, themeId);
    }

    public List<Waiting> getAll() {
        return waitingRepository.findAll();
    }

    public List<WaitingWithRank> getWaitingWithRank(Long memberId) {
        List<Waiting> waitings = waitingRepository.findAllOrderByAsc();
        List<WaitingWithRank> waitingWithRanks = new ArrayList<>();

        for (int i = 0; i < waitings.size(); i++) {
            Waiting waiting = waitings.get(i);
            WaitingWithRank waitingWithRank = new WaitingWithRank(waiting, i + 1);
            waitingWithRanks.add(waitingWithRank);
        }

        return waitingWithRanks.stream()
                .filter(waitingWithRank -> waitingWithRank.waiting().getMember().getId().equals(memberId))
                .toList();
    }

    public Waiting get(ReservationDate date, Long timeId, Long themeId) {
        return waitingRepository.findByParams(date, timeId, themeId)
                .orElseThrow(() -> new NotFoundException("예약 대기 정보를 찾을 수 없습니다."));
    }
}
