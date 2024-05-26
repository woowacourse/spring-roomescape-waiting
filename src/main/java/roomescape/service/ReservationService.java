package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.dto.ReservationRank;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.dto.LoginMember;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.request.ReservationFilterRequest;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationMineResponse;
import roomescape.dto.response.ReservationPendingResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.TimeSlotRepository;
import roomescape.validation.ReservationValidator;

@Service
@Transactional
public class ReservationService {

    private final ReservationValidator reservationValidator;
    private final MemberRepository memberRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationValidator reservationValidator,
                              MemberRepository memberRepository,
                              TimeSlotRepository timeSlotRepository,
                              ThemeRepository themeRepository,
                              ReservationRepository reservationRepository) {
        this.reservationValidator = reservationValidator;
        this.memberRepository = memberRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
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

    public List<ReservationPendingResponse> findAllPending() {
        List<ReservationRank> reservationRanks =
                reservationRepository.findAllPendingOrderByDateAscTime();
        return reservationRanks.stream()
                .map(ReservationPendingResponse::from)
                .toList();
    }

    public ReservationResponse create(AdminReservationRequest request, LocalDateTime now) {
        Member member = memberRepository.getMemberById(request.memberId());
        TimeSlot timeSlot = timeSlotRepository.getTimeSlotById(request.timeId());
        Theme theme = themeRepository.getThemeById(request.themeId());
        Reservation reservation = getReservation(request, member, timeSlot, theme);
        reservation.validatePast(now);
        reservationValidator.validateDuplicatedReservation(member, request.date(), timeSlot, theme);
        Reservation createdReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(createdReservation);
    }

    public ReservationResponse create(LoginMember loginMember, ReservationRequest request, LocalDateTime now) {
        Member member = memberRepository.getMemberById(loginMember.id());
        TimeSlot timeSlot = timeSlotRepository.getTimeSlotById(request.timeId());
        Theme theme = themeRepository.getThemeById(request.themeId());
        Reservation reservation = getReservation(request, member, timeSlot, theme);
        reservation.validatePast(now);
        reservationValidator.validateDuplicatedReservation(member, request.date(), timeSlot, theme);
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
        Reservation reservation = reservationRepository.getReservationBy(id);
        if (!reservation.isPending()) {
            Optional<Reservation> pendingReservation = reservationRepository.findFirstByDateAndTimeAndThemeAndStatusOrderById(
                    reservation.getDate(), reservation.getTime(), reservation.getTheme(), ReservationStatus.PENDING);
            pendingReservation.ifPresent(Reservation::book);
        }
        reservationRepository.deleteById(id);
    }

    private Reservation getReservation(AdminReservationRequest request,
                                       Member member,
                                       TimeSlot timeSlot,
                                       Theme theme) {
        if (reservationRepository.existsByDateAndTimeAndTheme(request.date(), timeSlot, theme)) {
            return request.toEntity(member, timeSlot, theme, ReservationStatus.PENDING);
        }
        return request.toEntity(member, timeSlot, theme, ReservationStatus.BOOKING);
    }

    private Reservation getReservation(ReservationRequest request,
                                       Member member,
                                       TimeSlot timeSlot,
                                       Theme theme) {
        if (reservationRepository.existsByDateAndTimeAndTheme(request.date(), timeSlot, theme)) {
            return request.toEntity(member, timeSlot, theme, ReservationStatus.PENDING);
        }
        return request.toEntity(member, timeSlot, theme, ReservationStatus.BOOKING);
    }
}
