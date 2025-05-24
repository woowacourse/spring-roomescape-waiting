package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.user.controller.dto.request.ReservationRequest;
import roomescape.user.controller.dto.response.MemberReservationResponse;
import roomescape.waiting.service.WaitingService;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;
    private final WaitingService waitingService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeService reservationTimeService,
                              ThemeService themeService, MemberService memberService, WaitingService waitingService) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.memberService = memberService;
        this.waitingService = waitingService;
    }

    public ReservationResponse create(Long memberId, ReservationRequest request) {
        Long timeId = request.timeId();
        ReservationDate reservationDate = new ReservationDate(request.date());
        Long themeId = request.themeId();

        if (reservationRepository.existsByReservationDateAndReservationTimeIdAndThemeId(reservationDate, timeId,
                themeId)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약이 찼습니다.");
        }

        Member member = memberService.findById(memberId);
        return createReservation(request, reservationDate, member);
    }

    public void deleteById(Long id) {
        Reservation reservation = getReservation(id);
        reservationRepository.deleteById(reservation.getId());
    }

    public List<ReservationResponse> getAll() {
        List<Reservation> reservations = reservationRepository.findAll();

        List<ReservationResponse> reservationResponses = new ArrayList<>(
                ReservationResponse.fromReservation(reservations));

        reservationResponses.sort(Comparator
                .comparing(ReservationResponse::date)
                .thenComparing(r -> r.time().startAt())
                .thenComparing(r -> r.theme().name())
        );

        return reservationResponses;
    }

    public List<ReservationResponse> searchReservations(Long memberId, Long themeId, LocalDate start, LocalDate end) {
        List<ReservationResponse> responses = new ArrayList<>(
                ReservationResponse.fromReservation(
                        reservationRepository.findByFilter(memberId, themeId, start, end)
                )
        );

        responses.sort(Comparator
                .comparing(ReservationResponse::date)
                .thenComparing(r -> r.time().startAt())
                .thenComparing(r -> r.theme().name())
        );

        return responses;
    }

    public List<MemberReservationResponse> findAllByMemberId(Long id) {
        return reservationRepository.findAllByMemberId(id).stream()
                .map(MemberReservationResponse::fromReservation)
                .toList();
    }

    public List<MemberReservationResponse> findAllReservationsAndWaitings(Long id) {
        List<MemberReservationResponse> allReservation = findAllByMemberId(id);
        List<MemberReservationResponse> allWaitings = waitingService.findAllByMemberId(id);
        List<MemberReservationResponse> allCombined = new ArrayList<>(allReservation);
        allCombined.addAll(allWaitings);
        allCombined.sort(Comparator
                .comparing(MemberReservationResponse::date)
                .thenComparing(r -> r.time().startAt())
                .thenComparing(r -> r.theme().name())
        );
        return allCombined;
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 예약을 찾을 수 없습니다."));
    }

    private ReservationResponse createReservation(ReservationRequest request, ReservationDate reservationDate,
                                                  Member member) {
        ReservationTime reservationTime = reservationTimeService.getReservationTime(request.timeId());
        ReservationDateTime reservationDateTime = new ReservationDateTime(reservationDate, reservationTime);
        Theme theme = themeService.getTheme(request.themeId());
        Reservation created = reservationRepository.save(Reservation.create(reservationDateTime.getReservationDate()
                .getDate(), reservationTime, theme, member));

        return ReservationResponse.fromReservation(created);
    }
}
