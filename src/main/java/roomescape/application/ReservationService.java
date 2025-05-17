package roomescape.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationDateTime;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.presentation.dto.request.LoginMember;
import roomescape.presentation.dto.request.AdminReservationCreateRequest;
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

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeService reservationTimeService,
                              ThemeService themeService,
                              MemberService memberService,
                              CurrentTimeService currentTimeService) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.memberService = memberService;
        this.currentTimeService = currentTimeService;
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
        reservation.reserve();
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
        reservation.reserve();
        Reservation created = reservationRepository.save(reservation);

        return ReservationResponse.from(created);
    }

    private ReservationDateTime getReservationDateTime(Long timeId, ReservationDate reservationDate) {
        ReservationTime reservationTime = reservationTimeService.findReservationTimeById(timeId);
        return ReservationDateTime.create(reservationDate, reservationTime, currentTimeService.now());
    }

    private void validateExistsReservation(ReservationDate reservationDate, ReservationTime time, Theme theme) {
        if (reservationRepository.existsByDateAndTimeAndTheme(reservationDate.getDate(), time, theme)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약이 찼습니다.");
        }
    }

    @Transactional
    public void deleteReservationById(Long id) {
        Reservation reservation = findReservationById(id);
        reservationRepository.deleteById(reservation.getId());
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
        return MyReservationResponse.from(reservations);
    }
}
