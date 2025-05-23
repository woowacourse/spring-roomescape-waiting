package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationDateTime;
import roomescape.domain.ReservationInfo;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.presentation.dto.request.AdminReservationCreateRequest;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.request.ReservationCreateRequest;
import roomescape.presentation.dto.response.MyReservationResponse;
import roomescape.presentation.dto.response.ReservationResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;
    private final CurrentTimeService currentTimeService;
    private final WaitingService waitingService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeService reservationTimeService,
                              ThemeService themeService,
                              MemberService memberService,
                              CurrentTimeService currentTimeService,
                              WaitingService waitingService) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.memberService = memberService;
        this.currentTimeService = currentTimeService;
        this.waitingService = waitingService;
    }

    public List<ReservationResponse> getReservations() {
        List<Reservation> reservations = reservationRepository.findAllByStatus(ReservationStatus.RESERVED);

        return ReservationResponse.from(reservations);
    }

    @Transactional
    public ReservationResponse createMemberReservation(ReservationCreateRequest request, LoginMember loginMember) {
        Member member = memberService.findMemberByEmail(loginMember.email());
        Reservation created = createReservation(request.date(), request.timeId(), request.themeId(), member);

        return ReservationResponse.from(created);
    }

    @Transactional
    public ReservationResponse createAdminReservation(AdminReservationCreateRequest request) {
        Member member = memberService.findMemberById(request.memberId());
        Reservation created = createReservation(request.date(), request.timeId(), request.themeId(), member);

        return ReservationResponse.from(created);
    }

    private Reservation createReservation(LocalDate date, Long timeId, Long themeId, Member member) {
        ReservationDate reservationDate = new ReservationDate(date);
        ReservationTime reservationTime = reservationTimeService.findReservationTimeById(timeId);
        ReservationDateTime reservationDateTime = ReservationDateTime.create(reservationDate, reservationTime, currentTimeService.now());

        Theme theme = themeService.findThemeById(themeId);
        validateExistsReservation(reservationDate, reservationTime, theme);

        Reservation reservation = Reservation.create(member, reservationDateTime.reservationDate().getDate(), reservationDateTime.reservationTime(), theme);
        return reservationRepository.save(reservation);
    }

    private void validateExistsReservation(ReservationDate reservationDate, ReservationTime time, Theme theme) {
        if (reservationRepository.existsByDateAndTimeAndThemeAndStatus(reservationDate.getDate(), time, theme, ReservationStatus.RESERVED)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약이 찼습니다.");
        }
    }

    @Transactional
    public void cancelReservationById(Long id) {
        Reservation reservation = findReservationById(id);
        reservation.cancel();
        ReservationInfo reservationInfo = ReservationInfo.create(reservation);
        if (waitingService.existsWaitings(reservationInfo)) {
            processWaitingToReservation(reservationInfo);
        }
    }

    private void processWaitingToReservation(ReservationInfo reservationInfo) {
        Waiting firstRankWaiting = waitingService.findFirstRankWaitingByReservationInfo(reservationInfo);
        waitingService.deleteWaitingById(firstRankWaiting.getId());

        LocalDate date = reservationInfo.getDate();
        Long timeId = reservationInfo.getTime().getId();
        Long themeId = reservationInfo.getTheme().getId();
        Member member = firstRankWaiting.getMember();
        Reservation newReservation = createReservation(date, timeId, themeId, member);
        ReservationInfo newReservationInfo = ReservationInfo.create(newReservation);

        waitingService.updateWaitings(reservationInfo, newReservationInfo);
    }

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 예약을 찾을 수 없습니다."));
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

    public List<MyReservationResponse> getMyReservations(LoginMember loginMember) {
        Member member = memberService.findMemberById(loginMember.id());
        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        List<Waiting> waitings = waitingService.findWaitingsByMember(member);
        return MyReservationResponse.from(reservations, waitings);
    }
}
