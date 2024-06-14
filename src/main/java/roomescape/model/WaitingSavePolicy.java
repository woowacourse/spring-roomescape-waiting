package roomescape.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.ReservationDto;

@Component
public class WaitingSavePolicy {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public WaitingSavePolicy(WaitingRepository waitingRepository, ReservationRepository reservationRepository,
                             ReservationTimeRepository reservationTimeRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public void validate(ReservationDto reservationDto) {
        ReservationTime time = findReservationTime(reservationDto);

        validateIsFuture(reservationDto.getDate(), time.getStartAt());
        validateDuplication(reservationDto.getDate(), reservationDto.getTimeId(), reservationDto.getThemeId(),
                reservationDto.getMemberId());
        validateExistReservation(reservationDto.getDate(), reservationDto.getTimeId(), reservationDto.getThemeId(),
                reservationDto.getMemberId());
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
            throw new BadRequestException("[ERROR] 현재 이전 대기는 할 수 없습니다.");
        }
    }

    private void validateDuplication(LocalDate date, long timeId, long themeId, long memberId) {
        boolean isExistWaiting = waitingRepository
                .existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId);
        if (isExistWaiting) {
            throw new DuplicatedException("[ERROR] 이미 예약 대기 이력이 존재합니다.");
        }

        boolean isExistReservation = reservationRepository
                .existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId);
        if (isExistReservation) {
            throw new DuplicatedException("[ERROR] 본인의 예약에는 대기를 할 수 없습니다.");
        }
    }

    private void validateExistReservation(LocalDate date, long timeId, long themeId, long memberId) {
        List<Reservation> reservations = reservationRepository
                .findByTimeIdAndThemeIdAndDate(timeId, themeId, date);
        if (reservations.isEmpty()) {
            throw new BadRequestException("[ERROR] 예약이 존재하지 않아 예약 대기가 불가합니다.");
        }
    }
}
