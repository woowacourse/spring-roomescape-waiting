package roomescape.reservation.application;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.login.presentation.dto.LoginMemberInfo;
import roomescape.auth.login.presentation.dto.SearchCondition;
import roomescape.common.exception.BusinessException;
import roomescape.member.domain.Member;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.member.presentation.dto.MyReservationResponse;
import roomescape.member.application.service.MemberQueryService;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.reservation.time.presentation.dto.ReservationTimeResponse;
import roomescape.reservation.time.application.service.ReservationTimeQueryService;
import roomescape.theme.domain.Theme;
import roomescape.theme.presentation.dto.ThemeResponse;
import roomescape.theme.application.service.ThemeQueryService;

@Service
public class ReservationFacadeService {

    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ThemeQueryService themeQueryService;
    private final MemberQueryService memberQueryService;

    public ReservationFacadeService(ReservationQueryService reservationQueryService,
                                    ReservationCommandService reservationCommandService,
                                    ReservationTimeQueryService reservationTimeQueryService,
                                    ThemeQueryService themeQueryService,
                                    MemberQueryService memberQueryService
    ) {
        this.reservationQueryService = reservationQueryService;
        this.reservationCommandService = reservationCommandService;
        this.reservationTimeQueryService = reservationTimeQueryService;
        this.themeQueryService = themeQueryService;
        this.memberQueryService = memberQueryService;
    }

    @Transactional
    public ReservationResponse createReservation(final ReservationRequest request, final Long memberId) {
        ReservationTime time = reservationTimeQueryService.findById(request.timeId());
        Theme theme = themeQueryService.findById(request.themeId());
        Member member = memberQueryService.findById(memberId);

        List<Reservation> reservations = reservationQueryService.findByThemeIdAndDate(request.themeId(), request.date());
        validateExistDuplicateReservation(reservations, time);

        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation(request.date(), time, theme, member, ReservationStatus.RESERVED);
        validateCanReserveDateTime(reservation, now);
        reservation = reservationCommandService.save(reservation);

        return ReservationResponse.from(reservation);
    }

    private void validateExistDuplicateReservation(final List<Reservation> reservations, final ReservationTime time) {
        boolean isBooked = reservations.stream()
            .anyMatch(reservation -> reservation.isSameTime(time));

        if (isBooked) {
            throw new BusinessException("이미 예약이 존재합니다.");
        }
    }

    private void validateCanReserveDateTime(final Reservation reservation, final LocalDateTime now) {
        if (reservation.isCannotReserveDateTime(now)) {
            throw new BusinessException("예약할 수 없는 날짜와 시간입니다.");
        }
    }

    public List<ReservationResponse> getReservations() {
        return reservationQueryService.findAll().stream()
            .map(ReservationResponse::from)
            .toList();
    }

    public void deleteReservationById(final Long id) {
        reservationCommandService.deleteById(id);
    }

    public List<ReservationResponse> searchReservationWithCondition(final SearchCondition condition) {
        List<Reservation> reservations = reservationQueryService.findBySearchCondition(condition);

        return reservations.stream()
            .map(reservation -> new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                new ReservationTimeResponse(reservation.getTime().getId(), reservation.getTime().getStartAt()),
                new ThemeResponse(reservation.getTheme().getId(), reservation.getTheme().getName(),
                    reservation.getTheme().getDescription(), reservation.getTheme().getThumbnail()),
                new MemberResponse(reservation.getMember().getId(), reservation.getMember().getName().name())
            ))
            .toList();
    }

    public List<MyReservationResponse> getMemberReservations(final LoginMemberInfo loginMemberInfo) {
        List<Reservation> reservations = reservationQueryService.findByMemberId(loginMemberInfo.id());

        return reservations.stream()
            .map(MyReservationResponse::from)
            .toList();
    }
}
