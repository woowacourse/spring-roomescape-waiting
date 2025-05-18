package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.dto.request.ReservationTimeRegisterDto;
import roomescape.dto.response.AvailableReservationTimeResponseDto;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.model.Reservation;
import roomescape.model.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTimeResponseDto> getAllTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream().map(ReservationTimeResponseDto::from).toList();
    }

    public ReservationTimeResponseDto saveTime(ReservationTimeRegisterDto reservationTimeRegisterDto) {
        validateReservationTime(reservationTimeRegisterDto);

        ReservationTime reservationTime = reservationTimeRegisterDto.convertToTime();
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        return new ReservationTimeResponseDto(savedReservationTime.getId(), savedReservationTime.getStartAt());
    }

    public void deleteTime(Long id) {
        clearReservationTimeInReservations(id);
        reservationTimeRepository.deleteById(id);
    }

    public List<AvailableReservationTimeResponseDto> getAvailableTimes(String date, Long themeId) {
        List<Reservation> reservations = getReservationsBy(date, themeId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        Set<ReservationTime> nonDuplicatedReservationTimes = getReservationTimes(reservations);

        reservationTimes.removeAll(nonDuplicatedReservationTimes);

        return getAvailableReservationTimes(reservationTimes, nonDuplicatedReservationTimes);
    }

    private List<Reservation> getReservationsBy(String date, Long themeId) {
        LocalDate parsedDate = LocalDate.parse(date);
        return reservationRepository.findByThemeIdAndDate(themeId, parsedDate);
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
        availableReservationTimes.addAll(nonAvailableReservationTimes);
        return availableReservationTimes;
    }

    private List<AvailableReservationTimeResponseDto> getAvailableReservationTimes(
            List<ReservationTime> reservationTimes) {
        return new java.util.ArrayList<>(reservationTimes.stream()
                .map(reservationTime -> AvailableReservationTimeResponseDto.from(
                        reservationTime,
                        false))
                .toList());
    }

    private List<AvailableReservationTimeResponseDto> getAvailableReservationTimes(
            Set<ReservationTime> nonDuplicatedReservationTimes) {
        return nonDuplicatedReservationTimes.stream()
                .map(reservationTime -> AvailableReservationTimeResponseDto.from(
                        reservationTime,
                        true))
                .toList();
    }

    private void validateReservationTime(ReservationTimeRegisterDto reservationTimeRegisterDto) {
        LocalTime parsedStartAt = LocalTime.parse(reservationTimeRegisterDto.startAt());

        if (reservationTimeRepository.existsByStartAt((parsedStartAt))) {
            throw new DuplicatedException("중복된 예약시각은 등록할 수 없습니다.");
        }
    }

    private void clearReservationTimeInReservations(Long id) {
        List<Reservation> reservations = reservationRepository.findByReservationTimeId(id);
        for (Reservation reservation : reservations) {
            reservation.setReservationTime(null);
        }
    }
}



