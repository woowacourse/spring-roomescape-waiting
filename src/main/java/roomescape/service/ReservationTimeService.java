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

    public ReservationTimeService(final ReservationTimeRepository reservationTimeRepository,
                                  final ReservationRepository reservationRepository) {
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

    public void deleteTime(final Long id) {
        try {
            reservationTimeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("삭제하고자 하는 시각에 예약된 정보가 있습니다.");
        }
    }

    public List<AvailableReservationTimeResponseDto> getAvailableTimes(String date, Long themeId) {
        LocalDate parsedDate = LocalDate.parse(date);

        List<Reservation> reservations = reservationRepository.findByTheme_IdAndDate(themeId, parsedDate);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        Set<ReservationTime> nonDuplicatedReservationTimes = reservations.stream()
                .map(Reservation::getReservationTime)
                .collect(Collectors.toSet());

        reservationTimes.removeAll(nonDuplicatedReservationTimes);

        List<AvailableReservationTime> nonAvailableReservationTimes = nonDuplicatedReservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTime(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        true))
                .toList();

        List<AvailableReservationTime> availableReservationTimes = new java.util.ArrayList<>(reservationTimes.stream()
                .map(reservationTime -> new AvailableReservationTime(
                        reservationTime.getId(),
                        reservationTime.getStartAt(),
                        false))
                .toList());

        availableReservationTimes.addAll(nonAvailableReservationTimes);

        return availableReservationTimes.stream()
                .map(
                        availableReservationTime -> new AvailableReservationTimeResponseDto(
                                availableReservationTime.getId(),
                                availableReservationTime.getStartAt(),
                                availableReservationTime.getAlreadyBooked()))
                .toList();
    }

    private void validateReservationTime(ReservationTimeRegisterDto reservationTimeRegisterDto) {
        LocalTime parsedStartAt = LocalTime.parse(reservationTimeRegisterDto.startAt());
        boolean duplicatedStartAtExisted = reservationTimeRepository.existsByStartAt((parsedStartAt));
        if (duplicatedStartAtExisted) {
            throw new DuplicatedException("중복된 예약시각은 등록할 수 없습니다.");
        }
    }
}



