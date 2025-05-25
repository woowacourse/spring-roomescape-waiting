package roomescape.application.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.common.exception.DuplicatedException;
import roomescape.common.exception.ResourceInUseException;
import roomescape.dto.request.ReservationTimeRegisterDto;
import roomescape.dto.response.AvailableReservationTimeResponseDto;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.model.ReservationTicket;
import roomescape.model.ReservationTime;
import roomescape.persistence.repository.ReservationTicketRepository;
import roomescape.persistence.repository.ReservationTimeRepository;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationTicketRepository reservationTicketRepository;

    public List<ReservationTimeResponseDto> getAllTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream().map(ReservationTimeResponseDto::new).toList();
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
        List<ReservationTicket> reservationTickets = getReservationsBy(date, themeId);
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        Set<ReservationTime> nonDuplicatedReservationTimes = getReservationTimes(reservationTickets);

        reservationTimes.removeAll(nonDuplicatedReservationTimes);

        return getAvailableReservationTimes(reservationTimes, nonDuplicatedReservationTimes);
    }

    private List<ReservationTicket> getReservationsBy(String date, Long themeId) {
        LocalDate parsedDate = LocalDate.parse(date);
        return reservationTicketRepository.findForThemeOnDate(themeId, parsedDate);
    }

    private Set<ReservationTime> getReservationTimes(List<ReservationTicket> reservationTickets) {
        return reservationTickets.stream()
                .map(ReservationTicket::getReservationTime)
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

        if (reservationTimeRepository.isDuplicatedStartAt((parsedStartAt))) {
            throw new DuplicatedException("중복된 예약시각은 등록할 수 없습니다.");
        }
    }
}



