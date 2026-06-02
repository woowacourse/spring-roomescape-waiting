package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.exception.client.BusinessRuleViolationException;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.application.dto.command.ReservationTimeCreateCommand;
import roomescape.application.dto.result.ReservationTimeResult;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResult> findAll() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResult::from)
                .toList();
    }

    public ReservationTimeResult create(ReservationTimeCreateCommand command) {
        ReservationTime time = ReservationTime.create(command.getStartAt());
        ReservationTime saved = reservationTimeRepository.save(time);
        return ReservationTimeResult.from(saved);
    }

    public void delete(Long id) {
        validateNotInUse(id);
        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationTimeResult> findAvailable(LocalDate date, Long themeId) {
        return reservationTimeRepository.findAvailable(date, themeId).stream()
                .map(ReservationTimeResult::from)
                .toList();
    }

    private void validateNotInUse(Long timeId) {
        if (reservationRepository.existsByTimeId(timeId)) {
            throw new BusinessRuleViolationException(
                    "예약이 존재하는 시간은 삭제할 수 없습니다."
            );
        }
    }

}
