package roomescape.application.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.ResourceInUseException;
import roomescape.dto.request.ReservationTimeRegisterDto;
import roomescape.dto.response.AvailableReservationTimeResponseDto;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.infrastructure.db.ReservationJpaRepository;
import roomescape.infrastructure.db.ReservationTimeJpaRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeJpaRepository reservationTimeJpaRepository;
    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationTimeService(ReservationTimeJpaRepository reservationTimeJpaRepository,
                                  ReservationJpaRepository reservationJpaRepository) {
        this.reservationTimeJpaRepository = reservationTimeJpaRepository;
        this.reservationJpaRepository = reservationJpaRepository;
    }

    public List<ReservationTimeResponseDto> getAllTimes() {
        List<ReservationTime> reservationTimes = reservationTimeJpaRepository.findAll();

        return reservationTimes.stream().map(ReservationTimeResponseDto::new).toList();
    }

    public ReservationTimeResponseDto saveTime(ReservationTimeRegisterDto reservationTimeRegisterDto) {
        validateReservationTime(reservationTimeRegisterDto);

        ReservationTime reservationTime = reservationTimeRegisterDto.convertToTime();
        ReservationTime savedReservationTime = reservationTimeJpaRepository.save(reservationTime);

        return new ReservationTimeResponseDto(savedReservationTime.getId(), savedReservationTime.getStartAt());
    }

    public void deleteTime(Long id) {
        try {
            reservationTimeJpaRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("삭제하고자 하는 시각에 예약된 정보가 있습니다.");
        }
    }

    public List<AvailableReservationTimeResponseDto> getAvailableTimes(String date, Long themeId) {
        List<Reservation> reservations = getReservationsBy(date, themeId);
        List<ReservationTime> reservationTimes = reservationTimeJpaRepository.findAll();
        Set<ReservationTime> nonDuplicatedReservationTimes = getReservationTimes(reservations);

        reservationTimes.removeAll(nonDuplicatedReservationTimes);

        return getAvailableReservationTimes(reservationTimes, nonDuplicatedReservationTimes);
    }

    private List<Reservation> getReservationsBy(String date, Long themeId) {
        LocalDate parsedDate = LocalDate.parse(date);
        return reservationJpaRepository.findByThemeIdAndDate(themeId, parsedDate);
    }

    private Set<ReservationTime> getReservationTimes(List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::getReservationTime)
                .collect(Collectors.toSet());
    }

    private List<AvailableReservationTimeResponseDto> getAvailableReservationTimes(
            List<ReservationTime> reservationTimes,
            Set<ReservationTime> nonDuplicatedReservationTimes) {
        List<AvailableReservationTimeResponseDto> availableReservationTimes = getAvailableReservationTimes(
                reservationTimes);
        List<AvailableReservationTimeResponseDto> nonAvailableReservationTimes = getAvailableReservationTimes(
                nonDuplicatedReservationTimes);

        return Stream.of(availableReservationTimes, nonAvailableReservationTimes)
                .flatMap(Collection::stream)
                .toList();
    }

    private List<AvailableReservationTimeResponseDto> getAvailableReservationTimes(
            List<ReservationTime> reservationTimes) {
        return reservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTimeResponseDto(
                        reservationTime,
                        false))
                .toList();
    }

    private List<AvailableReservationTimeResponseDto> getAvailableReservationTimes(
            Set<ReservationTime> nonDuplicatedReservationTimes) {
        return nonDuplicatedReservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTimeResponseDto(
                        reservationTime,
                        true))
                .toList();
    }

    private void validateReservationTime(ReservationTimeRegisterDto reservationTimeRegisterDto) {
        LocalTime parsedStartAt = LocalTime.parse(reservationTimeRegisterDto.startAt());

        if (reservationTimeJpaRepository.existsByStartAt((parsedStartAt))) {
            throw new DuplicatedException("중복된 예약시각은 등록할 수 없습니다.");
        }
    }
}



