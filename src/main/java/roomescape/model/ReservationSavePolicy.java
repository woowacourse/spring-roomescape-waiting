package roomescape.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.stereotype.Component;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.ReservationDto;

@Component
public class ReservationSavePolicy {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationSavePolicy(ReservationTimeRepository reservationTimeRepository,
                                 ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public void validate(ReservationDto reservationDto) {
        ReservationTime time = findReservationTime(reservationDto);

        LocalDate date = reservationDto.getDate();
        validateIsFuture(date, time.getStartAt());
        validateDuplication(date, time.getId(), reservationDto.getThemeId());
    }

    private ReservationTime findReservationTime(ReservationDto reservationDto) {
        long timeId = reservationDto.getTimeId();
        Optional<ReservationTime> time = reservationTimeRepository.findById(timeId);
        return time.orElseThrow(() -> new NotFoundException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    private void validateIsFuture(LocalDate date, LocalTime time) {
        LocalDateTime timeToBook = LocalDateTime.of(date, time).truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        if (timeToBook.isBefore(now)) {
            throw new BadRequestException("[ERROR] 현재 이전 예약은 할 수 없습니다.");
        }
    }

    private void validateDuplication(LocalDate date, long timeId, long themeId) {
        boolean isExist = reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (isExist) {
            throw new DuplicatedException("[ERROR] 중복되는 예약은 추가할 수 없습니다.");
        }
    }
}
