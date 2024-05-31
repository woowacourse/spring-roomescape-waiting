package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.exception.BadRequestException;
import roomescape.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationConditionSearchRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.repository.ReservationDetailRepository;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.time.domain.Time;
import roomescape.time.repository.TimeRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationDetailRepository detailRepository;
    private final MemberRepository memberRepository;
    private final TimeRepository timeRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationDetailRepository detailRepository,
                              MemberRepository memberRepository,
                              TimeRepository timeRepository) {
        this.reservationRepository = reservationRepository;
        this.detailRepository = detailRepository;
        this.memberRepository = memberRepository;
        this.timeRepository = timeRepository;
    }

    public List<ReservationResponse> findReservations() {
        List<Reservation> reservations = reservationRepository.findAllByOrderByDetailDateAsc();

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationRequest findReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("해당 예약 정보가 존재하지 않습니다."));

        return ReservationRequest.from(reservation);
    }

    public List<ReservationResponse> findReservationsByConditions(ReservationConditionSearchRequest request) {
        List<Reservation> reservations = reservationRepository.findAllByMember_Id(request.memberId());

        return reservations.stream()
                .filter(reservation -> reservation.isReservedAtPeriod(request.dateFrom(), request.dateTo()))
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findReservationByMemberId(Long id) {
        List<Reservation> reservationsByMember = reservationRepository.findAllByMember_IdOrderByDetailDateAsc(id);
        return reservationsByMember.stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    public void checkExistsReservation(Long detailId) {
        if (existsByDetailId(detailId)) {
            throw new BadRequestException("예약이 존재하지 않으므로 예약 대기할 수 없습니다.");
        }
    }

    private boolean existsByDetailId(Long detailId) {
        return reservationRepository.existsByDetail_Id(detailId);
    }

    public List<ReservationTimeAvailabilityResponse> findTimeAvailability(Long themeId, LocalDate date) {
        List<Time> allTimes = timeRepository.findAllByOrderByStartAtAsc();
        List<Time> bookedTimes = getBookedTimesOfThemeAtDate(themeId, date);

        return allTimes.stream()
                .map(time -> ReservationTimeAvailabilityResponse.from(time, bookedTimes.contains(time)))
                .toList();
    }

    private List<Time> getBookedTimesOfThemeAtDate(long themeId, LocalDate date) {
        List<Reservation> reservations = reservationRepository.findAllByDetail_Theme_IdAndDetail_Date(themeId, date);
        return reservations.stream()
                .map(Reservation::getTime)
                .toList();
    }

    public ReservationResponse addReservation(ReservationRequest reservationRequest) {
        Member member = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new BadRequestException("해당 멤버 정보가 존재하지 않습니다."));
        ReservationDetail detail = detailRepository.findById(reservationRequest.detailId())
                .orElseThrow(() -> new BadRequestException("해당 예약 정보가 존재하지 않습니다."));
        validateReservationDetail(detail);

        Reservation reservation = reservationRequest.createReservation(member, detail);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validateReservationDetail(ReservationDetail detail) {
        reservationRepository.findByDetail_Id(detail.getId())
                .ifPresent(reservation -> {
                    throw new ConflictException(
                            "해당 테마(%s)의 해당 시간(%s)에는 이미 예약이 존재합니다."
                                    .formatted(
                                            reservation.getTheme().getName(),
                                            reservation.getTime().getStartAt()));
                });
    }

    public void deleteReservation(long reservationId) {
        reservationRepository.deleteById(reservationId);
    }
}
