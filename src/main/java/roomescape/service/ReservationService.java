package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRank;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.dto.LoginMember;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.request.ReservationFilterRequest;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationMineResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;

@Service
@Transactional
public class ReservationService {

    private final MemberRepository memberRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ReservationService(MemberRepository memberRepository, TimeSlotRepository timeSlotRepository,
                              ReservationRepository reservationRepository, ThemeRepository themeRepository) {
        this.memberRepository = memberRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    public List<ReservationResponse> findEntireReservationList() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findDistinctReservations(ReservationFilterRequest request) {
        Member member = memberRepository.getMemberById(request.memberId());
        Theme theme = themeRepository.getThemeById(request.themeId());
        List<Reservation> reservations = reservationRepository.findAllByMemberAndThemeAndDateBetween(
                member,
                theme,
                request.dateFrom(),
                request.dateTo()
        );
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllPending() {
        List<Reservation> reservations =
                reservationRepository.findAllByStatusOrderByDateAscTime(ReservationStatus.PENDING);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse create(AdminReservationRequest request, LocalDateTime now) {
        Member member = memberRepository.getMemberById(request.memberId());
        TimeSlot timeSlot = timeSlotRepository.getTimeSlotById(request.timeId());
        Theme theme = themeRepository.getThemeById(request.themeId());
        Reservation reservation = request.toEntity(member, timeSlot, theme);
        reservation.validatePast(now);
        validateDuplicatedReservation(member, request.date(), timeSlot, theme);
        Reservation createdReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(createdReservation);
    }

    public ReservationResponse create(LoginMember loginMember, ReservationRequest request, LocalDateTime now) {
        Member member = memberRepository.getMemberById(loginMember.id());
        TimeSlot timeSlot = timeSlotRepository.getTimeSlotById(request.timeId());
        Theme theme = themeRepository.getThemeById(request.themeId());
        Reservation reservation = request.toBookingEntity(member, timeSlot, theme);
        reservation.validatePast(now);
        validateDuplicatedReservation(member, request.date(), timeSlot, theme);
        Reservation createdReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(createdReservation);
    }

    public ReservationResponse createPending(LoginMember loginMember, ReservationRequest request, LocalDateTime now) {
        Member member = memberRepository.getMemberById(loginMember.id());
        TimeSlot timeSlot = timeSlotRepository.getTimeSlotById(request.timeId());
        Theme theme = themeRepository.getThemeById(request.themeId());
        Reservation reservation = request.toPendingEntity(member, timeSlot, theme);
        reservation.validatePast(now);
        validateDuplicatedReservation(member, request.date(), timeSlot, theme);
        Reservation createdReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(createdReservation);
    }

    public List<ReservationMineResponse> findMyReservations(LoginMember loginMember) {
        Member member = memberRepository.getMemberById(loginMember.id());
        List<ReservationRank> reservationRanks = reservationRepository.findReservationRanksWithMember(member);
        return reservationRanks.stream()
                .map(ReservationMineResponse::from)
                .toList();
    }

    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }

    public void bookPendingReservation(Long id) {
        Reservation reservation = reservationRepository.getReservationBy(id);
        validateAlreadyBooked(reservation.getDate(), reservation.getTime(), reservation.getTheme());
        reservation.updateStatusBooked();
    }

    private void validateAlreadyBooked(LocalDate date, TimeSlot timeSlot, Theme theme) {
        boolean hasAlreadyBooked = reservationRepository.existsByDateAndTimeAndThemeAndStatusIs(
                date,
                timeSlot,
                theme,
                ReservationStatus.BOOKING
        );
        if (hasAlreadyBooked) {
            throw new IllegalArgumentException("예약이 존재하여 변경할 수 없습니다.");
        }
    }

    private void validateDuplicatedReservation(Member member, LocalDate date, TimeSlot timeSlot, Theme theme) {
        if (reservationRepository.existsByMemberAndDateAndTimeAndTheme(member, date, timeSlot, theme)) {
            throw new IllegalArgumentException("이미 예약을 시도 하였습니다.");
        }
    }
}
