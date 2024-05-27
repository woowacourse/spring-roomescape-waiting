package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.dto.LoginMember;
import roomescape.dto.request.MemberReservationRequest;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationMineResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.repository.*;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    private final MemberService memberService;
    private final ThemeService themeService;
    private final TimeService timeService;
    private final WaitingService waitingService;

    public ReservationService(ReservationRepository reservationRepository, WaitingRepository waitingRepository,
                              MemberService memberService, ThemeService themeService, TimeService timeService,
                              WaitingService waitingService) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.memberService = memberService;
        this.themeService = themeService;
        this.timeService = timeService;
        this.waitingService = waitingService;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findDistinctReservations(Long memberId, Long themeId,
                                                              String dateFrom, String dateTo) {
        LocalDate from = LocalDate.parse(dateFrom);
        LocalDate to = LocalDate.parse(dateTo);
        Member member = memberService.findMemberById(memberId);
        Theme theme = themeService.findThemeById(themeId);
        return reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, from, to)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationMineResponse> findMyReservationsAndWaitings(LoginMember loginMember) {
        List<ReservationMineResponse> myReservations = findMyReservations(loginMember);
        List<ReservationMineResponse> myWaitings = waitingService.findMyWaitings(loginMember);

        myReservations.addAll(myWaitings);
        return myReservations;
    }

    @Transactional(readOnly = true)
    public List<ReservationMineResponse> findMyReservations(LoginMember loginMember) {
        Member member = memberService.findMemberById(loginMember.id());
        List<Reservation> reservations = reservationRepository.findAllByMemberOrderByDateAsc(member);
        return reservations.stream()
                .map(ReservationMineResponse::from)
                .collect(Collectors.toList());
    }

    public ReservationResponse createByAdmin(ReservationRequest reservationRequest) {
        Member member = memberService.findMemberById(reservationRequest.memberId());
        TimeSlot timeSlot = timeService.findTimeSlotById(reservationRequest.timeId());
        Theme theme = themeService.findThemeById(reservationRequest.themeId());

        validate(reservationRequest.date(), timeSlot, theme, member);

        Reservation reservation = reservationRequest.toEntity(member, timeSlot, theme);
        Reservation createdReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(createdReservation);
    }

    public ReservationResponse createByClient(Long memberId, MemberReservationRequest reservationRequest) {
        Member member = memberService.findMemberById(memberId);
        TimeSlot timeSlot = timeService.findTimeSlotById(reservationRequest.timeId());
        Theme theme = themeService.findThemeById(reservationRequest.themeId());

        validate(reservationRequest.date(), timeSlot, theme, member);

        Reservation reservation = reservationRequest.toEntity(member, timeSlot, theme);
        Reservation createdReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(createdReservation);
    }

    public void delete(Long id) {
        Reservation currentReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 예약 내역이 존재하지 않습니다."));

        reservationRepository.deleteById(id);

        updateWaitingToReservation(currentReservation);
    }

    private void updateWaitingToReservation(final Reservation reservation) {

        Optional<Waiting> firstWaiting = waitingRepository
                .findFirstByDateAndTimeAndTheme(reservation.getDate(), reservation.getTime(), reservation.getTheme());

        firstWaiting.ifPresent(waiting -> {
            Reservation newReservation =
                    new Reservation(waiting.getMember(), reservation.getDate(), reservation.getTime(), reservation.getTheme());
            waitingRepository.delete(waiting);
            reservationRepository.save(newReservation);
        });
    }

    private void validate(LocalDate date, TimeSlot timeSlot, Theme theme, Member member) {
        validateReservation(date, timeSlot);
        validateDuplicatedReservation(date, timeSlot, theme, member);
    }

    private void validateReservation(LocalDate date, TimeSlot time) {
        if (time == null || (time.isTimeBeforeNow() && !date.isAfter(LocalDate.now()))) {
            throw new IllegalArgumentException("[ERROR] 지나간 날짜와 시간으로 예약할 수 없습니다");
        }
    }

    private void validateDuplicatedReservation(LocalDate date, TimeSlot timeSlot, Theme theme, Member member) {
        if (reservationRepository.existsByDateAndTimeAndThemeAndMember(date, timeSlot, theme, member)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약이 완료되었습니다");
        }

        if (reservationRepository.existsByDateAndTimeAndTheme(date, timeSlot, theme)) {
            throw new IllegalArgumentException("[ERROR] 예약이 종료되었습니다");
        }

        if (reservationRepository.existsByDateAndTimeAndMember(date, timeSlot, member)) {
            throw new IllegalArgumentException("[ERROR] 동일한 시간대에 예약을 두 개 이상 할 수 없습니다.");
        }
    }
}
