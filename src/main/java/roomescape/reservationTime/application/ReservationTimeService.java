package roomescape.reservationTime.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ReservationTimeErrorCode;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservationTime.application.dto.ReservationTimeCreateCommand;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationTimeReference reservationReference;

    @Transactional
    public ReservationTime saveTime(ReservationTimeCreateCommand createCommand) {
        ReservationTime reservationTime = ReservationTime.create(createCommand.startAt());
        return reservationTimeRepository.save(reservationTime);
    }

    public List<ReservationTime> getTimes() {
        return reservationTimeRepository.findAll();
    }

    public Set<Long> getBookedTimes(LocalDate date, Long themeId) {
        if (date == null || themeId == null) {
            return Set.of();
        }
        List<ReservationTime> bookedTimes = reservationReference.getBookedTimes(date, themeId);
        return bookedTimes.stream()
                .map(ReservationTime::getId)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void deleteTime(Long id) {
        reservationTimeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND, id));
        reservationReference.validateReservationTimeNotReferenced(id);
        reservationTimeRepository.deleteById(id);
    }
}
