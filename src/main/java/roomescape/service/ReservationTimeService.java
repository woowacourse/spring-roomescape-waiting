package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.ReservationTimeCreateCommand;
import roomescape.service.dto.ReservationTimeResult;
import roomescape.service.exception.ReservationTimeConflictException;
import roomescape.service.exception.ReservationTimeInUseException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;

@Service
public class ReservationTimeService {

    private static final Logger log = LoggerFactory.getLogger(ReservationTimeService.class);

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationTimeService(
            ReservationTimeRepository reservationTimeRepository,
            ReservationRepository reservationRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationTimeResult> findAll() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResult::from)
                .toList();
    }

    public ReservationTimeResult create(ReservationTimeCreateCommand command) {
        if (reservationTimeRepository.existsByStartAt(command.startAt())) {
            throw new ReservationTimeConflictException(
                    "이미 등록된 시간입니다: " + command.startAt());
        }
        ReservationTime saved = reservationTimeRepository.save(new ReservationTime(null, command.startAt()));
        return ReservationTimeResult.from(saved);
    }

    public void delete(Long id) {
        if (!reservationTimeRepository.existsById(id)) {
            log.warn("존재하지 않는 시간 삭제 시도: timeId={}", id);
            throw new ReservationTimeNotFoundException("존재하지 않는 시간입니다: timeId=" + id);
        }
        if (reservationRepository.existsByTimeId(id)) {
            log.warn("예약이 존재하는 시간 삭제 시도: timeId={}", id);
            throw new ReservationTimeInUseException(
                    "예약이 존재하는 시간은 삭제할 수 없습니다: timeId=" + id);
        }
        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationTimeResult> findAvailable(LocalDate date, Long themeId) {
        if (!themeRepository.existsById(themeId)) {
            log.warn("존재하지 않는 테마로 가용 시간 조회 시도: themeId={}", themeId);
            throw new ThemeNotFoundException("존재하지 않는 테마입니다: themeId=" + themeId);
        }
        LocalDateTime now = LocalDateTime.now();
        return reservationTimeRepository.findAll().stream()
                .filter(time -> !LocalDateTime.of(date, time.getStartAt()).isBefore(now))
                .map(ReservationTimeResult::from)
                .toList();
    }

}
