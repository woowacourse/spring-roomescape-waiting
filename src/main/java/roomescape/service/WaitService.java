package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Wait;
import roomescape.exception.custom.AlreadyWaitingException;
import roomescape.exception.custom.CannotDeleteReservationTimeInUseException;
import roomescape.exception.custom.CannotDeleteThemeInUseException;
import roomescape.exception.custom.WaitIsFullException;
import roomescape.exception.custom.WaitNotExistsException;
import roomescape.repository.WaitRepository;
import roomescape.repository.dto.WaitDetailDto;
import roomescape.service.dto.WaitInfo;

@Service
@Transactional(readOnly = true)
public class WaitService {

    public static final int MAX_WAITING_COUNT = 3;

    private final WaitRepository waitRepository;

    public WaitService(WaitRepository waitRepository) {
        this.waitRepository = waitRepository;
    }

    @Transactional
    public WaitInfo save(Wait waitWithoutId) {
        List<WaitDetailDto> waits = waitRepository.findBySlot(
                waitWithoutId.getReservationDate(),
                waitWithoutId.getTime().getId(),
                waitWithoutId.getTheme().getId());

        for (WaitDetailDto waitDetailDto : waits) {
            if (waitWithoutId.isSameUser(waitDetailDto.name())) {
                throw new AlreadyWaitingException();
            }
        }

        if (waits.size() >= MAX_WAITING_COUNT) {
            throw new WaitIsFullException();
        }

        Wait newWait = waitRepository.save(waitWithoutId);
        Long order = calculateOrder(newWait);
        return WaitInfo.of(newWait, order);
    }

    public List<WaitInfo> findByName(String name) {
        return waitRepository.findByName(name).stream()
                .map(WaitInfo::from)
                .toList();
    }

    public List<WaitInfo> findAll() {
        return waitRepository.findAll().stream()
                .map(WaitInfo::from)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        waitRepository.deleteById(id);
    }

    public List<WaitInfo> findBySlot(LocalDate reservationDate, Long timeId, Long themeId) {
        return waitRepository.findBySlot(reservationDate, timeId, themeId).stream()
                .map(WaitInfo::from)
                .toList();
    }

    public WaitInfo findWait(Long waitId) {
        WaitDetailDto waitDetailDto = waitRepository.findById(waitId)
                .orElseThrow(WaitNotExistsException::new);
        return WaitInfo.from(waitDetailDto);
    }

    public Long calculateOrder(Wait wait) {
        return waitRepository.findOrderByWait(wait);
    }

    public void validateReferencedTime(Long timeId) {
        if (waitRepository.existsByTimeId(timeId)) {
            throw new CannotDeleteReservationTimeInUseException();
        }
    }

    public void validateReferencedTheme(Long themeId) {
        if (waitRepository.existsByThemeId(themeId)) {
            throw new CannotDeleteThemeInUseException();
        }
    }
}
