package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.request.ReservationSearchConditionRequest;
import roomescape.reservation.dto.response.MyReservationJsonResponse;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Service
@AllArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationResponse create(
        ReservationTime reservationTime,
        LocalDate date,
        Theme theme,
        Member member,
        List<ReservationTime> availableTimes
    ) {
        validateReservationTimeConflicted(availableTimes, reservationTime);

        Reservation reservation = Reservation.createFirstWaiting(
            date,
            reservationTime,
            theme,
            member
        );

        return ReservationResponse.fromReservation(reservationRepository.save(reservation));
    }

    private void validateReservationTimeConflicted(
        List<ReservationTime> availableTimes,
        ReservationTime reservationTime
    ) {
        if (!availableTimes.contains(reservationTime)) {
            throw new ConflictException("해당 시간에 이미 예약이 존재합니다.");
        }
    }

    public ReservationResponse createWaiting(
        LocalDate date,
        ReservationTime reservationTime,
        Theme theme,
        Member member
    ) {
        Reservation reservation = makeWaiting(
            date,
            reservationTime,
            theme,
            member
        );

        return ReservationResponse.fromReservation(reservationRepository.save(reservation));
    }

    private Reservation makeWaiting(
        LocalDate date,
        ReservationTime reservationTime,
        Theme theme,
        Member member
    ) {
        return reservationRepository
            .findByLowestPriorityByDateAndTimeAndTheme(date, reservationTime, theme)
            .map(reservation -> Reservation.makeNextWaiting(reservation, member))
            .orElseGet(() -> Reservation.createFirstWaiting(date, reservationTime, theme, member));
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
            .map(ReservationResponse::fromReservation)
            .toList();
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
            .map(ReservationResponse::fromReservation)
            .toList();
    }

    public void deleteById(Long id) {
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
                    getStatus(reservation)
                )
            )
            .collect(Collectors.toList());
    }

    private String getStatus(Reservation reservation) {
        LocalDateTime reservationDateTime = LocalDateTime.of(reservation.getDate(), reservation.getTime().getStartAt());
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            return "과거";
        }
        if (!reservation.isWaiting()) {
            return "예약 완료";
        }
        return reservationRepository.findWaitingOrder(reservation) + "번째 예약대기";
    }

    public void deleteWaiting(Long id, Member member) {
        Reservation reservation = findByIdOrThrow(id);

        if (!reservation.getMember().equals(member)) {
            throw new BadRequestException("예약 대기 취소 권한이 없습니다.");
        }

        reservationRepository.delete(reservation);
    }

    public List<ReservationResponse> findHighestPriorityWaitings() {
        List<Reservation> reservations = reservationRepository.findHighestPriorityWaitings();
        return reservations.stream()
            .filter(Reservation::isWaiting)
            .map(ReservationResponse::fromReservation)
            .toList();
    }

    public void approveWaiting(Long id) {
        Reservation reservation = findByIdOrThrow(id);

        validateApprovalWaiting(reservation);

        reservation.approve();
        reservationRepository.save(reservation);
    }

    private void validateApprovalWaiting(Reservation reservation) {
        if (!reservationRepository.isHighestPriorityWaiting(reservation)) {
            throw new BadRequestException("앞에 대기 중인 예약이 있습니다. 대기 순서를 확인해주세요.");
        }
    }

    public void denyWaiting(Long id) {
        Reservation reservation = findByIdOrThrow(id);

        if (!reservation.isWaiting()) {
            throw new BadRequestException("대기 중인 예약이 아닙니다.");
        }

        reservationRepository.delete(reservation);
    }

    public Reservation findByIdOrThrow(Long id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new BadRequestException("존재하지 않는 예약입니다."));
    }
}
