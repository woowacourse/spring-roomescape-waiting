package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.ResourceInUseException;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.dto.request.ReservationTimeRegisterDto;
import roomescape.dto.response.AvailableReservationTimeResponseDto;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@RequiredArgsConstructor
@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public List<ReservationTimeResponseDto> getAllTimes() {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream().map(ReservationTimeResponseDto::from).toList();
    }

    @Transactional
    public ReservationTimeResponseDto saveTime(final ReservationTimeRegisterDto reservationTimeRegisterDto) {
        validateReservationTime(reservationTimeRegisterDto);

        final ReservationTime reservationTime = reservationTimeRegisterDto.convertToTime();
        final ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        return new ReservationTimeResponseDto(savedReservationTime.getId(), savedReservationTime.getStartAt());
    }

    @Transactional
    public void deleteTime(final Long id) {
        try {
            reservationTimeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceInUseException("삭제하고자 하는 시각에 예약된 정보가 있습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<AvailableReservationTimeResponseDto> getAvailableTimes(final String date, final Long themeId) {
        final List<Reservation> reservations = getReservationsBy(date, themeId);
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final Set<ReservationTime> nonDuplicatedReservationTimes = getReservationTimes(reservations);

        reservationTimes.removeAll(nonDuplicatedReservationTimes);

        return getAvailableReservationTimes(reservationTimes, nonDuplicatedReservationTimes);
    }

    private List<Reservation> getReservationsBy(final String date, final Long themeId) {
        final LocalDate parsedDate = LocalDate.parse(date);
        return reservationRepository.findByThemeIdAndDate(themeId, parsedDate);
    }

    private Set<ReservationTime> getReservationTimes(final List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::getReservationTime)
                .collect(Collectors.toSet());
    }

    private List<AvailableReservationTimeResponseDto> getAvailableReservationTimes(
            final List<ReservationTime> reservationTimes,
            final Set<ReservationTime> nonDuplicatedReservationTimes) {

        final List<AvailableReservationTimeResponseDto> availableReservationTimes = getAvailableReservationTimes(
                reservationTimes);
        final List<AvailableReservationTimeResponseDto> nonAvailableReservationTimes = getAvailableReservationTimes(
                nonDuplicatedReservationTimes);

        availableReservationTimes.addAll(nonAvailableReservationTimes);
        return availableReservationTimes;
    }

    private List<AvailableReservationTimeResponseDto> getAvailableReservationTimes(
            final List<ReservationTime> reservationTimes) {

        return new java.util.ArrayList<>(reservationTimes.stream()
                .map(reservationTime -> AvailableReservationTimeResponseDto.from(
                        reservationTime,
                        false))
                .toList());
    }

    private List<AvailableReservationTimeResponseDto> getAvailableReservationTimes(
            final Set<ReservationTime> nonDuplicatedReservationTimes) {

        return nonDuplicatedReservationTimes.stream()
                .map(reservationTime -> AvailableReservationTimeResponseDto.from(
                        reservationTime,
                        true))
                .toList();
    }

    private void validateReservationTime(final ReservationTimeRegisterDto reservationTimeRegisterDto) {
        final LocalTime parsedStartAt = LocalTime.parse(reservationTimeRegisterDto.startAt());

        if (reservationTimeRepository.existsByStartAt((parsedStartAt))) {
            throw new DuplicatedException("중복된 예약시각은 등록할 수 없습니다.");
        }
    }
}



