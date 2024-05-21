package roomescape.service.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationDetail;
import roomescape.domain.reservation.ReservationDetailRepository;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.schedule.ReservationDate;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.schedule.ReservationTimeRepository;
import roomescape.exception.InvalidReservationException;
import roomescape.service.schedule.dto.AvailableReservationTimeResponse;
import roomescape.service.schedule.dto.ReservationTimeCreateRequest;
import roomescape.service.schedule.dto.ReservationTimeReadRequest;
import roomescape.service.schedule.dto.ReservationTimeResponse;

import java.util.List;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationDetailRepository reservationDetailRepository;

    @Autowired
    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository, ReservationDetailRepository reservationDetailRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.reservationDetailRepository = reservationDetailRepository;
    }

    public ReservationTimeResponse create(ReservationTimeCreateRequest reservationTimeCreateRequest) {
        validateDuplicated(reservationTimeCreateRequest);
        ReservationTime reservationTime = reservationTimeRepository.save(
                reservationTimeCreateRequest.toReservationTime());
        return new ReservationTimeResponse(reservationTime);
    }

    private void validateDuplicated(ReservationTimeCreateRequest reservationTimeCreateRequest) {
        if (reservationTimeRepository.existsByStartAt(reservationTimeCreateRequest.startAt())) {
            throw new InvalidReservationException("이미 같은 시간이 존재합니다.");
        }
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::new)
                .toList();
    }

    public void deleteById(long id) {
        validateByReservation(id);
        reservationTimeRepository.deleteById(id);
    }

    private void validateByReservation(long id) {
        //TODO: detail이 존재하더라도, reservation은 없을 수 있음
        if (reservationDetailRepository.existsByScheduleTimeId(id)) {
            throw new InvalidReservationException("해당 시간에 예약이 존재해서 삭제할 수 없습니다.");
        }
    }

    public List<AvailableReservationTimeResponse> findAvailableTimes(
            ReservationTimeReadRequest reservationTimeReadRequest) {
        List<ReservationDetail> reservationDetails = reservationDetailRepository.findByScheduleDateAndThemeId(
                ReservationDate.of(reservationTimeReadRequest.date()), reservationTimeReadRequest.themeId());
        return reservationTimeRepository.findAll().stream()
                .map(time -> new AvailableReservationTimeResponse(time.getId(), time.getStartAt(),
                        isBooked(reservationDetails, time)))
                .toList();
    }

    private boolean isBooked(List<ReservationDetail> reservationDetails, ReservationTime time) {
        return reservationDetails.stream()
                .map(ReservationDetail::getReservationTime)
                .anyMatch(time::isSame);
    }
}
