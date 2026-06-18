package roomescape.application.service;

import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.service.command.ReservationTimeCommand;
import roomescape.application.service.result.ReservationTimeResult;
import roomescape.domain.ReservationTime;
import roomescape.exception.DuplicateEntityException;
import roomescape.persistence.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    @Transactional
    public ReservationTimeResult register(ReservationTimeCommand command) {
        validateAlreadyTime(command.startAt());
        ReservationTime reservationTime = new ReservationTime(command.startAt());
        ReservationTime saved = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResult.from(saved);
    }

    @Transactional
    public void deactivate(long id) {
        reservationTimeRepository.findById(id)
                .ifPresent(time -> {
                    time.deactivate();
                    reservationTimeRepository.update(time);
                });
    }

    @Transactional
    public void activate(long id) {
        reservationTimeRepository.findById(id)
                .ifPresent(time -> {
                    time.activate();
                    reservationTimeRepository.update(time);
                });
    }

    private void validateAlreadyTime(LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new DuplicateEntityException("이미 등록된 예약 시간 입니다. %s", startAt);
        }
    }
}
