package roomescape.reservation.service.usecase;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.repository.ReservationWaitRepository;
import roomescape.reservation.service.dto.ReservationWaitWithRankResponse;

@Service
@RequiredArgsConstructor
public class ReservationWaitQueryUseCase {

    private final ReservationWaitRepository reservationWaitRepository;

    public List<ReservationWaitWithRankResponse> getByMemberId(final Long memberId) {
        return reservationWaitRepository.findWithRankByInfoMemberId(memberId);
    }

    public List<ReservationWait> getAll() {
        return reservationWaitRepository.findAll();
    }

    public Optional<ReservationWait> findByParamsAt(
            final ReservationDate date,
            final Long timeId,
            final Long themeId,
            final int index
    ) {
        validateIndexPositive(index);
        return reservationWaitRepository.findByParamsAt(date, timeId, themeId, index);
    }

    private void validateIndexPositive(final int index) {
        if (index < 0) {
            throw new IllegalStateException("index는 음수일 수 없습니다.");
        }
    }
}
