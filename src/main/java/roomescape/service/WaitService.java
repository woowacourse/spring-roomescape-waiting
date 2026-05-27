package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Wait;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.WaitRepository;
import roomescape.service.dto.response.ServiceReceptionResponse;

@Component
@Transactional(readOnly = true)
public class WaitService {

    private final WaitRepository waitRepository;

    public WaitService(WaitRepository waitRepository) {
        this.waitRepository = waitRepository;
    }

    @Transactional
    public ServiceReceptionResponse save(Wait waitWithoutId) {
        List<Wait> waits = waitRepository.findBySlot(
                waitWithoutId.getReservationDate(),
                waitWithoutId.getTime().getId(),
                waitWithoutId.getTheme().getId());

        for (Wait wait : waits) {
            if (wait.getName().equals(waitWithoutId.getName())) {
                throw new CustomInvalidRequestException(ErrorCode.DUPLICATED_WAIT);
            }
        }

        if (waits.size() >= 3) {
            throw new CustomInvalidRequestException(ErrorCode.WAIT_IS_FULL);
        }

        Wait wait = waitRepository.save(waitWithoutId);

        return ServiceReceptionResponse.of(wait, calculateOrder(wait), ReservationStatus.WAITING.name());
    }

    public List<ServiceReceptionResponse> findByName(String name) {
        return waitRepository.findByName(name).stream()
                .map(wait -> ServiceReceptionResponse.of(wait, calculateOrder(wait), ReservationStatus.WAITING.name()))
                .toList();
    }

    public List<ServiceReceptionResponse> findAll() {
        return waitRepository.findAll().stream()
                .map(wait -> ServiceReceptionResponse.of(wait, calculateOrder(wait), ReservationStatus.WAITING.name()))
                .toList();
    }

    public void delete(Long id) {
        waitRepository.delete(id);
    }

    public List<Wait> findByReservation(Reservation reservation) {
        return waitRepository.findBySlot(reservation.getDate(), reservation.getTime().getId(),
                reservation.getTheme().getId());
    }

    public Wait findWait(Long waitId) {
        return waitRepository.findById(waitId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_WAIT));
    }

    private Long calculateOrder(Wait wait) {
        return waitRepository.findOrderByWait(wait);
    }
}
