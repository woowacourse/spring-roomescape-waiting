package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import roomescape.reservation.controller.dto.request.ReservationTimeSaveRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationMapping;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.SelectableTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;


    public ReservationTimeService(final ReservationTimeRepository reservationTimeRepository,
                                  final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTime save(final ReservationTimeSaveRequest reservationTimeSaveRequest) {
        ReservationTime reservationTime = reservationTimeSaveRequest.toEntity();
        return reservationTimeRepository.save(reservationTime);
    }

    public List<ReservationTime> getAll() {
        return StreamSupport.stream(reservationTimeRepository.findAll().spliterator(), false).toList();
    }

    public ReservationTime getById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 잘못된 예약 가능 시간 번호를 입력하였습니다."));
    }

    public List<SelectableTime> findSelectableTimes(final LocalDate date, final long themeId) {
        List<ReservationMapping> usedTimeIds = reservationRepository.findByDateAndThemeId(date, themeId);
        List<ReservationTime> reservationTimes = getAll();

        return reservationTimes.stream()
                .map(time -> new SelectableTime(
                        time.getId(),
                        time.getStartAt(),
                        isAlreadyBooked(time, usedTimeIds)
                ))
                .toList();
    }

    private boolean isAlreadyBooked(final ReservationTime reservationTime, final List<ReservationMapping> usedTimeIds) {
        return usedTimeIds.stream()
                .anyMatch(reservationMapping -> reservationMapping.getTimeId() == reservationTime.getId());
    }

    public int delete(final long id) {
        validateDoesNotExists(id);
        validateAlreadyHasReservationByTimeId(id);
        return reservationTimeRepository.deleteById(id);
    }

    private void validateAlreadyHasReservationByTimeId(final long id) {
        List<Reservation> reservations = reservationRepository.findByTimeId(id);
        if (!reservations.isEmpty()) {
            throw new IllegalArgumentException("[ERROR] 해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }
    }

    private void validateDoesNotExists(final long id) {
        if (reservationTimeRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException("[ERROR] (themeId : " + id + ") 에 대한 예약 시간이 존재하지 않습니다.");
        }
    }
}
