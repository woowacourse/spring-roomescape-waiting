package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.time.ReservationTime;
import roomescape.dto.reservationtime.AvailableTimeResponse;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;

    public ReservationTimeService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository timeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
    }

    public Long addReservationTime(ReservationTimeRequest reservationTimeRequest) {
        validateTimeDuplicate(reservationTimeRequest.startAt());
        ReservationTime reservationTime = reservationTimeRequest.toEntity();
        return timeRepository.save(reservationTime).getId();
    }

    private void validateTimeDuplicate(LocalTime time) {
        if (timeRepository.existsByStartAt(time)) {
            throw new IllegalArgumentException(
                    "[ERROR] 이미 등록된 시간은 등록할 수 없습니다.",
                    new Throwable("등록 시간 : " + time)
            );
        }
    }

    public void deleteReservationTime(Long id) {
        ReservationTime reservationTime = timeRepository.getById(id);
        validateDeletable(reservationTime);
        reservationRepository.deleteById(reservationTime.getId());
    }

    private void validateDeletable(ReservationTime reservationTime) {
        if (reservationRepository.existsByTimeId(reservationTime.getId())) {
            throw new IllegalArgumentException(
                    "[ERROR] 해당 시간에 예약이 존재해서 삭제할 수 없습니다.",
                    new Throwable("예약 시간 : " + reservationTime.getStartAt())
            );
        }
    }

    public List<ReservationTimeResponse> getAllReservationTimes() {
        List<ReservationTime> reservationTimes = timeRepository.findAll();
        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse getReservationTime(Long id) {
        ReservationTime reservationTime = timeRepository.getById(id);
        return ReservationTimeResponse.from(reservationTime);
    }

    public List<AvailableTimeResponse> getAvailableTimes(LocalDate date, Long themeId) {
        List<ReservationTime> bookedTimes = findBookedTimes(date, themeId);
        List<AvailableTimeResponse> alreadyBookedTimeResponses = bookedTimes.stream()
                .map(AvailableTimeResponse::createAlreadyBookedTime)
                .toList();

        List<AvailableTimeResponse> availableTimeResponses = findAvailableTimes(bookedTimes).stream()
                .map(AvailableTimeResponse::createAvailableTime)
                .toList();

        return concatTimeResponses(alreadyBookedTimeResponses, availableTimeResponses);
    }

    private List<ReservationTime> findBookedTimes(LocalDate date, Long themeId) {
        List<Reservation> bookedReservations = reservationRepository.findByDateAndThemeId(date, themeId);

        return bookedReservations.stream()
                .map(Reservation::getTime)
                .toList();
    }

    private List<ReservationTime> findAvailableTimes(List<ReservationTime> alreadyBookedTimes) {
        List<Long> bookedTimeIds = alreadyBookedTimes.stream()
                .map(ReservationTime::getId)
                .toList();

        if (bookedTimeIds.isEmpty()) {
            return timeRepository.findAll();
        }

        return timeRepository.findByIdNotIn(bookedTimeIds);
    }

    private List<AvailableTimeResponse> concatTimeResponses(
            List<AvailableTimeResponse> alreadyBookedTimeResponses,
            List<AvailableTimeResponse> availableTimeResponses
    ) {
        List<AvailableTimeResponse> responses = new ArrayList<>();
        responses.addAll(availableTimeResponses);
        responses.addAll(alreadyBookedTimeResponses);
        responses.sort(Comparator.comparingLong(AvailableTimeResponse::id));

        return responses;
    }
}
