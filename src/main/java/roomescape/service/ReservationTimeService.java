package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.ResourceInUseException;
import roomescape.dto.request.ReservationTimeRegisterDto;
import roomescape.dto.response.AvailableReservationTimeResponseDto;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.model.AvailableReservationTime;
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
        try {
            reservationTimeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("삭제하고자 하는 시각에 예약된 정보가 있습니다.");
        }
    }

    public List<AvailableReservationTimeResponseDto> getAvailableTimes(String date, Long themeId) {
        List<Reservation> reservations = getReservationsBy(date, themeId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        Set<ReservationTime> nonDuplicatedReservationTimes = getReservationTimes(reservations);

        reservationTimes.removeAll(nonDuplicatedReservationTimes);

        List<AvailableReservationTime> availableReservationTimes = getAvailableReservationTimes(
                reservationTimes, nonDuplicatedReservationTimes);

        return availableReservationTimes.stream()
                .map(
                        availableReservationTime -> new AvailableReservationTimeResponseDto(
                                availableReservationTime.getId(),
                                availableReservationTime.getStartAt(),
                                availableReservationTime.getAlreadyBooked()))
                .toList();
    }

    private List<Reservation> getReservationsBy(String date, Long themeId) {
        LocalDate parsedDate = LocalDate.parse(date);
        return reservationRepository.findByTheme_IdAndDate(themeId, parsedDate);
    }

    private Set<ReservationTime> getReservationTimes(List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::getReservationTime)
                .collect(Collectors.toSet());
    }

    private List<AvailableReservationTime> getAvailableReservationTimes(List<ReservationTime> reservationTimes,
                                                                        Set<ReservationTime> nonDuplicatedReservationTimes) {
        List<AvailableReservationTime> availableReservationTimes = getAvailableReservationTimes(reservationTimes);
        List<AvailableReservationTime> nonAvailableReservationTimes = getAvailableReservationTimes(
                nonDuplicatedReservationTimes);
        availableReservationTimes.addAll(nonAvailableReservationTimes);
        return availableReservationTimes;
    }

    private List<AvailableReservationTime> getAvailableReservationTimes(List<ReservationTime> reservationTimes) {
        return new java.util.ArrayList<>(reservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTime(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        false))
                .toList());
    }

    private List<AvailableReservationTime> getAvailableReservationTimes(
            Set<ReservationTime> nonDuplicatedReservationTimes) {
        return nonDuplicatedReservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTime(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        true))
                .toList();
    }

    private void validateReservationTime(ReservationTimeRegisterDto reservationTimeRegisterDto) {
        LocalTime parsedStartAt = LocalTime.parse(reservationTimeRegisterDto.startAt());

        if (reservationTimeRepository.existsByStartAt((parsedStartAt))) {
            throw new DuplicatedException("중복된 예약시각은 등록할 수 없습니다.");
        }
    }
}



