package roomescape.business.application_service.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.WaitingDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.repository.Reservations;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WaitingReader {

    private final Reservations reservations;

    /**
     * 대기 중인 예약들을 모두 반환합니다.
     *
     * @return 대기중인 예약 응답들
     */
    public List<WaitingDto> getAll() {
        List<Reservation> reservations = this.reservations.findAllNotReserved();
        return WaitingDto.fromEntities(reservations);
    }
}
