package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.ReservationDto;
import roomescape.service.dto.ReservationTimeInfoDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationService(ReservationRepository reservationRepository, ReservationTimeRepository reservationTimeRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation saveReservation(ReservationDto reservationDto) {
        ReservationTime time = findReservationTime(reservationDto);

        LocalDate date = reservationDto.getDate();
        validateIsFuture(date, time.getStartAt());
        validateDuplication(date, time.getId(), reservationDto.getThemeId());

        Reservation reservation = Reservation.of(reservationDto, time, reservationDto.getThemeId(), reservationDto.getMemberId());
        return reservationRepository.save(reservation);
    }

    private ReservationTime findReservationTime(ReservationDto reservationDto) {
        long timeId = reservationDto.getTimeId();
        Optional<ReservationTime> time = reservationTimeRepository.findById(timeId);
        return time.orElseThrow(() -> new BadRequestException("[ERROR] 존재하지 않는 데이터입니다."));
    }

    public void deleteReservation(long id) {
        validateExistence(id);
        reservationRepository.deleteById(id);
    }

    public ReservationTimeInfoDto findReservationTimesInformation(LocalDate date, long themeId) {
        List<ReservationTime> bookedTimes = reservationRepository.findReservationTimeBooked(date, themeId);
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        return new ReservationTimeInfoDto(bookedTimes, allTimes);
    }

    public List<Reservation> findReservationsByConditions(long memberId, long themeId, LocalDate from, LocalDate to) {
        return reservationRepository.findByMemberIdAndThemeIdAndDate(memberId, themeId, from, to);
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

    private void validateExistence(long id) {
        boolean isNotExist = !reservationRepository.existsById(id);
        if (isNotExist) {
            throw new NotFoundException("[ERROR] 존재하지 않는 예약입니다.");
        }
    }
}
