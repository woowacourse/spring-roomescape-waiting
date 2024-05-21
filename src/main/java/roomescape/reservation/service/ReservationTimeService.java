package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import roomescape.reservation.controller.dto.request.ReservationTimeSaveRequest;
import roomescape.reservation.controller.dto.response.ReservationTimeDeleteResponse;
import roomescape.reservation.controller.dto.response.ReservationTimeResponse;
import roomescape.reservation.controller.dto.response.SelectableTimeResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationMapping;
import roomescape.reservation.domain.ReservationTime;
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

    public ReservationTimeResponse save(final ReservationTimeSaveRequest reservationTimeSaveRequest) {
        ReservationTime reservationTime = reservationTimeSaveRequest.toEntity();
        return ReservationTimeResponse.from(reservationTimeRepository.save(reservationTime));
    }

    public List<ReservationTimeResponse> getAll() {
        return StreamSupport.stream(reservationTimeRepository.findAll().spliterator(), false)
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTime getById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 잘못된 예약 가능 시간 번호를 입력하였습니다."));
    }

    public List<SelectableTimeResponse> findSelectableTimes(final LocalDate date, final long themeId) {
        List<ReservationMapping> usedTimeIds = reservationRepository.findByDateAndThemeId(date, themeId);
        List<ReservationTime> reservationTimes =
                StreamSupport.stream(reservationTimeRepository.findAll().spliterator(), false).toList();

        return reservationTimes.stream()
                .map(time -> new SelectableTimeResponse(
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

    public ReservationTimeDeleteResponse delete(final long id) {
        validateDoesNotExists(id);
        validateAlreadyHasReservationByTimeId(id);
        return new ReservationTimeDeleteResponse(reservationTimeRepository.deleteById(id));
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
