package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Wait;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;
import roomescape.repository.WaitRepository;
import roomescape.repository.dto.WaitDetailDto;

@Service
@Transactional(readOnly = true)
public class WaitService {

    public static final int MAX_WAITING_COUNT = 3;

    private final WaitRepository waitRepository;

    public WaitService(WaitRepository waitRepository) {
        this.waitRepository = waitRepository;
    }

    @Transactional
    public WaitDetailDto save(Wait waitWithoutId) {
        List<WaitDetailDto> waits = waitRepository.findBySlot(
                waitWithoutId.getReservationDate(),
                waitWithoutId.getTime().getId(),
                waitWithoutId.getTheme().getId());

        for (WaitDetailDto waitDetailDto : waits) {
            if (waitWithoutId.isSameUser(waitDetailDto.name())) {
                throw new CustomInvalidRequestException(ErrorCode.DUPLICATED_WAIT);
            }
        }

        if (waits.size() >= MAX_WAITING_COUNT) {
            throw new CustomInvalidRequestException(ErrorCode.WAIT_IS_FULL);
        }

        Wait newWait = waitRepository.save(waitWithoutId);
        return WaitDetailDto.from(newWait, calculateOrder(newWait));
    }

    public List<WaitDetailDto> findByName(String name) {
        return waitRepository.findByName(name);
    }

    public List<WaitDetailDto> findAll() {
        return waitRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        waitRepository.deleteById(id);
    }

    public List<WaitDetailDto> findBySlot(LocalDate reservationDate, Long timeId, Long themeId) {
        return waitRepository.findBySlot(reservationDate, timeId, themeId);
    }

    public WaitDetailDto findWait(Long waitId) {
        return waitRepository.findById(waitId)
                .orElseThrow(() -> new CustomInvalidRequestException(ErrorCode.NOT_FOUND_WAIT));
    }

    public Long calculateOrder(Wait wait) {
        return waitRepository.findOrderByWait(wait);
    }
}
