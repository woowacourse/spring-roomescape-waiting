package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.exception.IllegalReservationDateTimeRequestException;
import roomescape.member.dao.MemberRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.dao.ReservationContentRepository;
import roomescape.reservation.dao.ReservationRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationContent;
import roomescape.reservation.domain.Reservations;
import roomescape.reservation.domain.Status;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationConditionSearchRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.theme.dao.ThemeRepository;
import roomescape.theme.domain.Theme;
import roomescape.time.dao.TimeRepository;
import roomescape.time.domain.Time;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationContentRepository reservationContentRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository,
            ReservationContentRepository reservationContentRepository, TimeRepository timeRepository,
            ThemeRepository themeRepository, MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationContentRepository = reservationContentRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public ReservationResponse addReservation(ReservationRequest reservationRequest) {
        Time time = timeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new IllegalReservationDateTimeRequestException("해당 예약 시간이 존재하지 않습니다."));
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new BadRequestException("선택하신 테마가 존재하지 않습니다."));
        validateReservationRequest(reservationRequest);
        ReservationContent reservationContent = reservationContentRepository.findByThemeAndTimeAndDate(theme, time,
                        reservationRequest.date())
                .orElseGet(() -> reservationContentRepository.save(
                        new ReservationContent(reservationRequest.date(), time, theme)));
        Reservation savedReservation = reservationRepository.save(
                new Reservation(reservationRequest.member(), reservationContent));
        return ReservationResponse.fromReservation(savedReservation);
    }

    private void validateReservationRequest(ReservationRequest reservationRequest) {
        if (reservationRequest.date()
                .isBefore(LocalDate.now())) {
            throw new IllegalReservationDateTimeRequestException("지난 날짜의 예약을 시도하였습니다.");
        }
    }

    @Transactional
    public ReservationResponse addReservation(AdminReservationRequest reservationRequest) {
        Time time = timeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new IllegalReservationDateTimeRequestException("해당 예약 시간이 존재하지 않습니다."));
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new BadRequestException("선택한 테마가 존재하지 않습니다."));
        Member member = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow();

        ReservationContent reservationContent = reservationContentRepository.findByThemeAndTimeAndDate(theme, time,
                        reservationRequest.date())
                .orElseGet(() -> reservationContentRepository.save(
                        new ReservationContent(reservationRequest.date(), time, theme)));
        Reservation savedReservation = reservationRepository.save(new Reservation(member, reservationContent));
        return ReservationResponse.fromReservation(savedReservation);
    }

    public List<ReservationResponse> findReservations() {
        List<Reservation> reservations = reservationRepository.findAllByOrderByReservationContent_Date();

        return reservations.stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> findReservationByMemberId(Long id) {
        List<Reservation> reservationsByMember = reservationRepository.findAllByMember_Id(id);
        return reservationsByMember.stream()
                .map(this::findMyReservations)
                .toList();
    }

    private MyReservationResponse findMyReservations(Reservation reservation) {
        Long bookedContentId = reservation.getReservationContentId();
        List<Reservation> reservations = reservationRepository.findAllByReservationContent_IdOrderByCreatedAtAsc(
                bookedContentId);
        Reservations reservationQueue = new Reservations(reservations);
        int reservationRank = reservationQueue.getReservationRank(reservation);
        return MyReservationResponse.of(reservation, Status.fromRank(reservationRank), reservationRank);
    }

    public List<ReservationTimeAvailabilityResponse> findTimeAvailability(long themeId, LocalDate date) {
        List<Time> allTimes = timeRepository.findAllByOrderByStartAtAsc();
        List<Time> bookedTimes = getBookedTimesOfThemeAtDate(themeId, date);

        return allTimes.stream()
                .map(time -> ReservationTimeAvailabilityResponse.fromTime(time, bookedTimes.contains(time)))
                .toList();
    }

    private List<Time> getBookedTimesOfThemeAtDate(long themeId, LocalDate date) {
        List<Reservation> reservationsOfThemeInDate = reservationRepository.findAllByReservationContent_Theme_IdAndReservationContent_Date(
                themeId, date);
        return extractReservationTimes(reservationsOfThemeInDate);
    }

    private List<Time> extractReservationTimes(List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::getTime)
                .toList();
    }

    public List<ReservationResponse> findReservationsByConditions(ReservationConditionSearchRequest request) {
        List<Reservation> reservations = reservationRepository.findAllByMember_Id(request.memberId());

        return reservations.stream()
                .filter(reservation -> reservation.isReservedDateBetween(request.dateFrom(), request.dateTo()))
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public void removeReservations(long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

}
