package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;
import roomescape.user.controller.dto.ReservationRequest;
import roomescape.user.controller.dto.response.MemberReservationResponse;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeService reservationTimeService,
                              ThemeService themeService, MemberService memberService) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.memberService = memberService;
    }

    @Transactional
    public ReservationResponse create(Long memberId, ReservationRequest request) {
        Long timeId = request.timeId();
        ReservationDate reservationDate = new ReservationDate(request.date());

        if (reservationRepository.existsByReservationDateAndReservationTimeId(reservationDate, timeId)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약이 찼습니다.");
        }

        Member member = memberService.findById(memberId);
        return createReservation(request, reservationDate, member);
    }

    @Transactional
    public ReservationResponse createByName(String name, ReservationRequest request) {
        Long timeId = request.timeId();
        ReservationDate reservationDate = new ReservationDate(request.date());

        if (reservationRepository.existsByReservationDateAndReservationTimeId(reservationDate, timeId)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약이 찼습니다.");
        }

        Member member = memberService.findByName(name);
        return createReservation(request, reservationDate, member);
    }

    public void deleteById(Long id) {
        Reservation reservation = getReservation(id);
        reservationRepository.deleteById(reservation.getId());
    }

    public List<ReservationResponse> getAll() {
        List<Reservation> reservations = reservationRepository.findAll();

        return ReservationResponse.from(reservations);
    }

    public List<ReservationResponse> searchReservations(Long memberId, Long themeId, LocalDate start, LocalDate end) {
        return ReservationResponse.from(
                reservationRepository.findByFilter(
                        memberId, themeId, start, end
                )
        );
    }

    public List<MemberReservationResponse> findAllByMemberId(Long id) {
        return reservationRepository.findAllByMemberId(id).stream()
                .map(MemberReservationResponse::from)
                .toList();
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
                .getDate(), reservationTime, theme, member, ReservationStatus.RESERVATION));

        return ReservationResponse.from(created);
    }
}
