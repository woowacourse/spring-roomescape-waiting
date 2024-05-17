package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.exceptions.DuplicationException;
import roomescape.exceptions.ValidationException;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberRequest;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationOfMemberResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.repository.ReservationJpaRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.service.ThemeService;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationJpaRepository reservationJpaRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;

    public ReservationService(
            ReservationJpaRepository ReservationJpaRepository,
            ReservationTimeService reservationTimeService,
            ThemeService themeService,
            MemberService memberService
    ) {
        this.reservationJpaRepository = ReservationJpaRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.memberService = memberService;
    }

    public ReservationResponse addReservation(
            ReservationRequest reservationRequest,
            MemberRequest memberRequest
    ) {
        ReservationTimeResponse timeResponse = reservationTimeService.getTime(reservationRequest.timeId());
        ThemeResponse themeResponse = themeService.getTheme(reservationRequest.themeId());

        Reservation reservation = new Reservation(
                reservationRequest.date(),
                timeResponse.toReservationTime(),
                themeResponse.toTheme(),
                memberRequest.toLoginMember()
        );
        validateIsBeforeNow(reservation);
        validateIsDuplicated(reservation);

        return new ReservationResponse(reservationJpaRepository.save(reservation));
    }

    public ReservationResponse addReservation(AdminReservationRequest adminReservationRequest) {
        Member member = memberService.getLoginMemberById(adminReservationRequest.memberId());
        ReservationTimeResponse timeResponse = reservationTimeService.getTime(adminReservationRequest.timeId());
        ThemeResponse themeResponse = themeService.getTheme(adminReservationRequest.themeId());

        Reservation reservation = new Reservation(
                adminReservationRequest.date(),
                timeResponse.toReservationTime(),
                themeResponse.toTheme(),
                member
        );
        validateIsBeforeNow(reservation);
        validateIsDuplicated(reservation);

        return new ReservationResponse(reservationJpaRepository.save(reservation));
    }

    private void validateIsBeforeNow(Reservation reservation) {
        if (reservation.isBeforeNow()) {
            throw new ValidationException("과거 시간은 예약할 수 없습니다.");
        }
    }

    private void validateIsDuplicated(Reservation reservation) {
        if (reservationJpaRepository.existsByDateAndReservationTimeAndTheme(reservation.getDate(), reservation.getReservationTime(), reservation.getTheme())) {
            throw new DuplicationException("이미 예약이 존재합니다.");
        }
    }

    @Transactional
    public List<ReservationResponse> findReservations() {
        return reservationJpaRepository.findAll()
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> searchReservations(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        Theme theme = themeService.getById(themeId);
        Member member = memberService.getById(memberId);
        return reservationJpaRepository.findByThemeAndMember(theme, member)
                .stream()
                .filter(reservation -> reservation.isBetweenInclusive(dateFrom, dateTo))
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationOfMemberResponse> findReservationsByMember(Member member) {
        return reservationJpaRepository.findByMember(member)
                .stream()
                .map(ReservationOfMemberResponse::new)
                .toList();
    }

    public void deleteReservation(Long id) {
        reservationJpaRepository.deleteById(id);
    }
}
