package roomescape.reservation.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.global.exception.ResourceNotFoundException;
import roomescape.reservation.application.dto.response.MyWaitingServiceResponse;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.repository.WaitingRepository;

@RequiredArgsConstructor
@Service
public class AdminWaitingService {

    private final WaitingRepository waitingRepository;

    public List<MyWaitingServiceResponse> getAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();

        return waitings.stream()
            .map(MyWaitingServiceResponse::from)
            .toList();
    }

    public void deleteById(Long id) {
        Waiting waiting = waitingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("id에 해당하는 예약 대기가 존재하지 않습니다."));
        waitingRepository.delete(waiting);
    }
}
