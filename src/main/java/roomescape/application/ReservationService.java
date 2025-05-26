package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.exception.AuthorizationException;
import roomescape.application.exception.DuplicateReservationException;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationDateTime;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.infrastructure.repository.WaitingRepository;
import roomescape.presentation.dto.request.AdminReservationCreateRequest;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.request.ReservationCreateRequest;
import roomescape.presentation.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;
    private final CurrentTimeService currentTimeService;

    public ReservationService(ReservationRepository reservationRepository, ReservationTimeService reservationTimeService, ThemeService themeService, MemberService memberService, CurrentTimeService currentTimeService, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.memberService = memberService;
        this.currentTimeService = currentTimeService;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationResponse> getReservations() {
        List<Reservation> reservations = reservationRepository.findAll();

        return ReservationResponse.from(reservations);
    }

    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request, LoginMember loginMember) {
        ReservationDate reservationDate = new ReservationDate(request.date());
        Long timeId = request.timeId();
        Long themeId = request.themeId();
        ReservationTime time = reservationTimeService.findReservationTimeById(timeId);
        Theme theme = themeService.findThemeById(themeId);

        validateExistsReservation(reservationDate, time, theme);

        Member member = memberService.findMemberByEmail(loginMember.email());
        ReservationDateTime reservationDateTime = getReservationDateTime(timeId, reservationDate);
        Reservation reservation = Reservation.create(member, reservationDateTime.reservationDate().getDate(), reservationDateTime.reservationTime(), theme);
        Reservation created = reservationRepository.save(reservation);

        return ReservationResponse.from(created);
    }

    @Transactional
    public ReservationResponse createAdminReservation(AdminReservationCreateRequest request) {
        ReservationDate reservationDate = new ReservationDate(request.date());
        Long timeId = request.timeId();
        Long themeId = request.themeId();
        ReservationTime time = reservationTimeService.findReservationTimeById(timeId);
        Theme theme = themeService.findThemeById(themeId);

        validateExistsReservation(reservationDate, time, theme);

        Member member = memberService.findMemberById(request.memberId());
        ReservationDateTime reservationDateTime = getReservationDateTime(timeId, reservationDate);
        Reservation reservation = Reservation.create(member, reservationDateTime.reservationDate().getDate(), reservationDateTime.reservationTime(), theme);
        Reservation created = reservationRepository.save(reservation);

        return ReservationResponse.from(created);
    }

    @Transactional
    public void deleteReservationById(Long id, Long memberId) {
        Reservation reservation = findReservationById(id);

        Member member = memberService.findMemberById(memberId);

        boolean isOwner = Objects.equals(reservation.getMember().getId(), memberId);
        boolean isAdmin = member.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AuthorizationException("[ERROR] 본인 또는 관리자만 예약을 삭제할 수 있습니다.");
        }

        Optional<Waiting> firstWaiting = waitingRepository.findFirstByDateAndThemeIdAndTimeIdOrderByCreatedAtAsc(
                reservation.getDate(), reservation.getTheme().getId(), reservation.getTime().getId()
        );
        firstWaiting.ifPresent(this::promoteWaitingToReservation);
        reservationRepository.delete(reservation);
    }

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 예약을 찾을 수 없습니다. : " + id));
    }

    public List<ReservationResponse> getReservationsByFilter(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        List<Reservation> reservations = reservationRepository.findAllByThemeAndMemberAndDate(themeId, memberId, dateFrom, dateTo);

        return ReservationResponse.from(reservations);
    }

    private ReservationDateTime getReservationDateTime(Long timeId, ReservationDate reservationDate) {
        ReservationTime reservationTime = reservationTimeService.findReservationTimeById(timeId);
        return ReservationDateTime.create(reservationDate, reservationTime, currentTimeService.now());
    }

    private void validateExistsReservation(ReservationDate reservationDate, ReservationTime time, Theme theme) {
        if (reservationRepository.existsByDateAndTimeAndTheme(reservationDate.getDate(), time, theme)) {
            throw new DuplicateReservationException("[ERROR] 이미 예약이 찼습니다.");
        }
    }

    private void promoteWaitingToReservation(Waiting waiting) {
        Reservation reservation = Reservation.create(waiting.getMember(), waiting.getDate(), waiting.getTime(), waiting.getTheme());
        reservationRepository.save(reservation);
        waitingRepository.delete(waiting);
    }
}
