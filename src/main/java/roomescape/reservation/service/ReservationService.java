package roomescape.reservation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchConditionRequest;
import roomescape.reservation.dto.response.MyReservationAndWaitingResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.schedule.domain.Schedule;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationService(
            ReservationRepository reservationRepository
    ) {
        this.reservationRepository = reservationRepository;
    }

    public ReservationResponse createReservation(
            Member member,
            List<ReservationTime> availableTimes,
            Schedule schedule,
            ReservationCreateRequest reservationCreateRequest
    ) {
        validateNotPast(schedule.getDateTime());
        validateReservationTimeConflict(availableTimes, schedule.getTime());
        Reservation reservation = reservationRepository.save(
                reservationCreateRequest.toReservation(schedule, member));
        return ReservationResponse.from(reservation);
    }

    private void validateNotPast(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("과거 시점의 예약을 할 수 없습니다.");
        }
    }

    private void validateReservationTimeConflict(List<ReservationTime> availableTimes,
                                                 ReservationTime reservationTime) {
        if (!availableTimes.contains(reservationTime)) {
            throw new ConflictException("이미 해당 시간과 테마에 예약이 존재하여 예약할 수 없습니다.");
        }
    }

    public List<ReservationResponse> findAll() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream().map(ReservationResponse::from).toList();
    }

    public List<ReservationResponse> findByCondition(
            ReservationSearchConditionRequest reservationSearchConditionRequest) {
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndVisitDateBetween(
                reservationSearchConditionRequest.getThemeId(),
                reservationSearchConditionRequest.getMemberId(),
                reservationSearchConditionRequest.getDateFrom(),
                reservationSearchConditionRequest.getDateTo()
        );

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteReservationById(Long id) {
        reservationRepository.deleteById(id);
    }

    public boolean existsByScheduleTimeId(Long id) {
        return reservationRepository.existsByScheduleTimeId(id);
    }

    public void validateReservationNonExistenceByTimeId(Long reservationTimeId) {
        if (existsByScheduleTimeId(reservationTimeId)) {
            throw new ConflictException("해당 예약 시간을 사용하는 예약이 존재합니다.");
        }
    }

    public List<MyReservationAndWaitingResponse> findAllByMember(Member member) {
        return reservationRepository.findAllByMember(member).stream()
                .map(MyReservationAndWaitingResponse::fromReservationAndStatus)
                .toList();
    }

    public boolean existsByMemberAndSchedule(Member member, Schedule schedule) {
        return reservationRepository.existsByMemberAndSchedule(member, schedule);
    }

    public boolean existsBySchedule(Schedule schedule) {
        return reservationRepository.existsBySchedule(schedule);
    }
}
