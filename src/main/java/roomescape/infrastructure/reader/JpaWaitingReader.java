package roomescape.infrastructure.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.application_service.reader.WaitingReader;
import roomescape.business.dto.WaitingDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.repository.Reservations;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaWaitingReader implements WaitingReader {

    private final Reservations reservations;

    @Override
    public List<WaitingDto> getAll() {
        List<Reservation> reservations = this.reservations.findAllNotReserved();
        return WaitingDto.fromEntities(reservations);
    }
}
