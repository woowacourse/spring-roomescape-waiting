package roomescape.reservation.service.usecase;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.repository.ReservationWaitRepository;
import roomescape.reservation.service.dto.ReservationWaitWithRankResponse;

@Service
@RequiredArgsConstructor
public class ReservationWaitQueryUseCase {

    private final ReservationWaitRepository reservationWaitRepository;

    public List<ReservationWaitWithRankResponse> getByMemberId(final Long memberId) {
        return reservationWaitRepository.findWithRankByInfoMemberId(memberId);
    }
}
