package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationSearchConditionRequest;
import roomescape.reservation.dto.response.MyReservationJsonResponse;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

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
        ReservationTime reservationTime,
        Theme theme,
        Member member,
        List<ReservationTime> availableTimes,
        ReservationCreateRequest reservationCreateRequest
    ) {
        validateNotPast(LocalDateTime.of(reservationCreateRequest.date(), reservationTime.getStartAt()));
        validateReservationTimeConflict(availableTimes, reservationTime);

        Reservation reservation = new Reservation(
            reservationCreateRequest.date(),
            reservationTime,
            theme,
            member
        );

        reservationRepository.save(reservation);

        return ReservationResponse.from(reservation);
    }

    private void validateReservationTimeConflict(List<ReservationTime> availableTimes,
                                                 ReservationTime reservationTime) {
        if (!availableTimes.contains(reservationTime)) {
            throw new ConflictException("이미 해당 시간과 테마에 예약이 존재하여 예약할 수 없습니다.");
        }
    }

    public ReservationResponse createWaiting(
        ReservationTime reservationTime,
        Theme theme,
        Member member,
        ReservationCreateRequest reservationCreateRequest
    ) {
        validateNotPast(LocalDateTime.of(reservationCreateRequest.date(), reservationTime.getStartAt()));

        Reservation reservation = makeWaiting(
            reservationCreateRequest.date(),
            reservationTime,
            theme,
            member
        );

        reservationRepository.save(reservation);

        return ReservationResponse.from(reservation);
    }

    private Reservation makeWaiting(
        LocalDate date,
        ReservationTime reservationTime,
        Theme theme,
        Member member
    ) {
        return reservationRepository
            .findByLastPriorityByDateAndTimeAndTheme(date, reservationTime, theme)
            .map(value -> Reservation.makeWaiting(value, member))
            .orElseGet(() -> Reservation.first(date, reservationTime, theme, member));
    }


    private void validateNotPast(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("과거 시점의 예약을 할 수 없습니다.");
        }
    }

    public List<ReservationResponse> findAll() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream().map(ReservationResponse::from).toList();
    }

    public List<ReservationResponse> findByCondition(
        ReservationSearchConditionRequest reservationSearchConditionRequest
    ) {
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndVisitDateBetween(
            reservationSearchConditionRequest.themeId(),
            reservationSearchConditionRequest.memberId(),
            reservationSearchConditionRequest.dateFrom(),
            reservationSearchConditionRequest.dateTo()
        );

        return reservations.stream()
            .map(ReservationResponse::from)
            .toList();
    }

    public void deleteReservationById(Long id) {
        reservationRepository.deleteById(id);
    }

    public void validateReservationNonExistenceByTimeId(Long reservationTimeId) {
        if (reservationRepository.existsByTimeId(reservationTimeId)) {
            throw new ConflictException("해당 예약 시간을 사용하는 예약이 존재합니다.");
        }
    }

    public List<MyReservationResponse> findAllByMember(Member member) {
        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        return reservations.stream()
            .map(reservation ->
                MyReservationJsonResponse.fromReservationAndStatus(
                    reservation,
                    getReservationStatus(reservation)
                )
            )
            .collect(Collectors.toList());
    }

    private String getReservationStatus(Reservation reservation) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservation.getDate(), reservation.getTime().getStartAt());
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            return "과거";
        }
        long order = reservationRepository.findOrder(reservation);
        if (order == 0) {
            return "예약";
        }
        return order + "번째 예약대기";
    }

    public void deleteWaiting(Long id, Member member) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("존재하지 않는 예약입니다."));

        if (!reservation.getMember().equals(member)) {
            throw new BadRequestException("예약 대기 취소 권한이 없습니다.");
        }

        reservationRepository.delete(reservation);
    }

    public List<ReservationResponse> findHighestPriorityWaitings() {
        List<Reservation> reservations = reservationRepository.findHighestPriorityWaitings();
        return reservations.stream()
            .filter(Reservation::isWaiting)
            .map(ReservationResponse::from)
            .toList();
    }

    public void approveWaiting(Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("존재하지 않는 예약입니다."));

        validateApprovalWaiting(reservation);

        reservation.approve();
        reservationRepository.save(reservation);
    }

    private void validateApprovalWaiting(Reservation reservation) {
        if (!reservationRepository.isHighestPriorityWaiting(reservation)) {
            throw new BadRequestException("앞에 대기 중인 예약이 있습니다. 대기 순서를 확인해주세요.");
        }
    }
}
