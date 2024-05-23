package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.admin.domain.FilterInfo;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.admin.dto.ReservationFilterRequest;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.member.exception.model.MemberNotFoundException;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Date;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.dto.ReservationWaitingResponse;
import roomescape.reservation.exception.ReservationExceptionCode;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.model.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.Time;
import roomescape.time.exception.model.TimeNotFoundException;
import roomescape.time.repository.TimeRepository;
import roomescape.waiting.domain.Waiting;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository, TimeRepository timeRepository,
                              ThemeRepository themeRepository, MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse addReservation(ReservationRequest reservationRequest, long memberId) {
        Reservation saveReservation = makeReservation(reservationRequest, memberId, ReservationStatus.RESERVED);

        return ReservationResponse.fromReservation(reservationRepository.save(saveReservation));
    }

    public ReservationResponse addWaitingReservation(ReservationRequest reservationRequest, long memberId) {
        validateDuplicateReservation(reservationRequest, memberId);
        Reservation saveReservation = makeReservation(reservationRequest, memberId, ReservationStatus.WAITING);

        return ReservationResponse.fromReservation(reservationRepository.save(saveReservation));
    }

    public void addAdminReservation(AdminReservationRequest adminReservationRequest) {
        Time time = timeRepository.findById(adminReservationRequest.timeId())
                .orElseThrow(TimeNotFoundException::new);
        Theme theme = themeRepository.findById(adminReservationRequest.themeId())
                .orElseThrow(ThemeNotFoundException::new);
        Member member = memberRepository.findMemberById(adminReservationRequest.memberId())
                .orElseThrow(MemberNotFoundException::new);

        Reservation saveReservation = Reservation.of(adminReservationRequest.date(), time, theme, member,
                ReservationStatus.RESERVED);

        ReservationResponse.fromReservation(reservationRepository.save(saveReservation));
    }


    public List<ReservationResponse> findReservations() {
        List<Reservation> reservations = reservationRepository.findAllByOrderByDateAscTimeAsc();

        return reservations.stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public List<ReservationTimeAvailabilityResponse> findTimeAvailability(long themeId, LocalDate date) {
        List<Time> allTimes = timeRepository.findAllByOrderByStartAt();
        Date findDate = Date.dateFrom(date);
        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndDate(themeId, findDate);
        List<Time> bookedTimes = extractReservationTimes(reservations);

        return allTimes.stream()
                .map(time -> ReservationTimeAvailabilityResponse.fromTime(time, isTimeBooked(time, bookedTimes)))
                .toList();
    }

    public List<ReservationResponse> findFilteredReservations(ReservationFilterRequest reservationFilterRequest) {
        FilterInfo filterInfo = reservationFilterRequest.toFilterInfo();

        return reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(filterInfo.getMemberId(),
                        filterInfo.getThemeId(), filterInfo.getFromDate(), filterInfo.getToDate()).stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public List<ReservationWaitingResponse> findMemberReservations(long id) {
        List<Reservation> reservations = reservationRepository.findAllByMemberIdAndReservationStatus(id,
                ReservationStatus.RESERVED);

        List<Reservation> waitingReservations = reservationRepository.findAllByMemberIdAndReservationStatus(id,
                ReservationStatus.WAITING);
        List<Waiting> waitingRanks = findWaitingWithRank(waitingReservations);

        List<ReservationWaitingResponse> reservationWaitingResponses = makeResponse(
                reservations, waitingRanks);

        return reservationWaitingResponses.stream()
                .sorted(Comparator.comparing(ReservationWaitingResponse::date))
                .toList();
    }

    private static List<ReservationWaitingResponse> makeResponse(List<Reservation> reservations,
                                                                 List<Waiting> waitingRanks) {
        List<ReservationWaitingResponse> reservationWaitingResponses = new ArrayList<>();

        reservations.forEach(reservation -> reservationWaitingResponses
                .add(ReservationWaitingResponse.from(reservation)));

        waitingRanks.forEach(waiting -> reservationWaitingResponses
                .add(ReservationWaitingResponse.from(waiting)));

        return reservationWaitingResponses;
    }

    public void removeReservations(long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    public void removeWaitingReservations(long waitingId) {
        reservationRepository.deleteById(waitingId);
    }

    public List<Waiting> findWaitingWithRank(List<Reservation> reservations) {
        List<Waiting> waitings = new ArrayList<>();

        for (Reservation reservation : reservations) {
            int rank = reservationRepository.countByThemeAndDateAndTimeAndIdLessThan(reservation.getTheme(),
                    Date.dateFrom(reservation.getDate()), reservation.getReservationTime(), reservation.getId());

            waitings.add(new Waiting(reservation, rank));
        }
        return waitings;
    }

    private Reservation makeReservation(ReservationRequest reservationRequest, long memberId,
                                        ReservationStatus reservationStatus) {
        Time time = timeRepository.findById(reservationRequest.timeId())
                .orElseThrow(TimeNotFoundException::new);
        Theme theme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(ThemeNotFoundException::new);
        Member member = memberRepository.findMemberById(memberId)
                .orElseThrow(MemberNotFoundException::new);

        return Reservation.of(reservationRequest.date(), time, theme, member, reservationStatus);
    }

    private void validateDuplicateReservation(ReservationRequest reservationRequest, long memberId) {
        Optional<Reservation> duplicateReservation = reservationRepository.findByDateAndMemberIdAndThemeIdAndTimeId(
                Date.saveFrom(reservationRequest.date()), memberId, reservationRequest.themeId(),
                reservationRequest.timeId());

        if (duplicateReservation.isPresent()) {
            throw new RoomEscapeException(ReservationExceptionCode.DUPLICATE_RESERVATION);
        }
    }

    private List<Time> extractReservationTimes(List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::getReservationTime)
                .toList();
    }

    private boolean isTimeBooked(Time time, List<Time> bookedTimes) {
        return bookedTimes.contains(time);
    }
}
