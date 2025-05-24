package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.login.presentation.dto.LoginMemberInfo;
import roomescape.auth.login.presentation.dto.SearchCondition;
import roomescape.common.exception.BusinessException;
import roomescape.member.application.service.MemberQueryService;
import roomescape.member.domain.Member;
import roomescape.reservation.application.service.ReservationCommandService;
import roomescape.reservation.application.service.ReservationQueryService;
import roomescape.reservation.application.service.WaitingReservationQueryService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.dto.WaitingReservationWithRank;
import roomescape.reservation.presentation.dto.MyReservationResponse;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.time.application.service.ReservationTimeQueryService;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.application.service.ThemeQueryService;
import roomescape.theme.domain.Theme;

@Service
public class ReservationFacadeService {

    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;
    private final WaitingReservationQueryService waitingReservationQueryService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ThemeQueryService themeQueryService;
    private final MemberQueryService memberQueryService;

    public ReservationFacadeService(ReservationQueryService reservationQueryService,
                                    ReservationCommandService reservationCommandService,
                                    WaitingReservationQueryService waitingReservationQueryService,
                                    ReservationTimeQueryService reservationTimeQueryService,
                                    ThemeQueryService themeQueryService,
                                    MemberQueryService memberQueryService) {
        this.reservationQueryService = reservationQueryService;
        this.reservationCommandService = reservationCommandService;
        this.waitingReservationQueryService = waitingReservationQueryService;
        this.reservationTimeQueryService = reservationTimeQueryService;
        this.themeQueryService = themeQueryService;
        this.memberQueryService = memberQueryService;
    }

    @Transactional
    public ReservationResponse createReservation(final ReservationRequest request, final Long memberId) {
        ReservationTime time = reservationTimeQueryService.findById(request.timeId());
        Theme theme = themeQueryService.findById(request.themeId());
        Member member = memberQueryService.findById(memberId);
        validateExistDuplicateReservation(theme.getId(), time.getId(), request.date());

        LocalDateTime now = LocalDateTime.now();
        Reservation reservation = new Reservation(request.date(), time, theme, member);
        validateCanReserveDateTime(reservation, now);
        reservation = reservationCommandService.save(reservation);

        return ReservationResponse.from(reservation);
    }

    private void validateExistDuplicateReservation(Long themeId, Long timeId, LocalDate date) {
        if (reservationQueryService.isExistsReservedReservation(themeId, timeId, date)) {
            throw new BusinessException("이미 예약이 존재합니다.");
        }
    }

    private void validateCanReserveDateTime(final Reservation reservation, final LocalDateTime now) {
        if (reservation.isCannotReserveDateTime(now)) {
            throw new BusinessException("예약할 수 없는 날짜와 시간입니다.");
        }
    }

    public void deleteReservationById(final Long id) {
        reservationCommandService.deleteById(id);
    }

    public List<ReservationResponse> searchReservationWithCondition(final SearchCondition condition) {
        List<Reservation> reservations = reservationQueryService.findBySearchCondition(condition);

        return reservations.stream()
            .map(ReservationResponse::from)
            .toList();
    }

    public List<ReservationResponse> getReservations() {
        return reservationQueryService.findAll().stream()
            .map(ReservationResponse::from)
            .toList();
    }

    public List<MyReservationResponse> getMemberReservations(final LoginMemberInfo loginMemberInfo) {
        List<Reservation> reservations = reservationQueryService.findByMemberId(loginMemberInfo.id());
        List<WaitingReservationWithRank> waitings = waitingReservationQueryService.findWaitingsWithRankByMemberId(
            loginMemberInfo.id());

        List<MyReservationResponse> responses = new ArrayList<>(reservations.stream()
            .map(reservation -> new MyReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                "예약"
            ))
            .toList());

        responses.addAll(waitings.stream()
            .map(waiting -> new MyReservationResponse(
                waiting.waitingReservation().getId(),
                waiting.waitingReservation().getTheme().getName(),
                waiting.waitingReservation().getDate(),
                waiting.waitingReservation().getTime().getStartAt(),
                (waiting.rank()+1) + "번째 예약대기"
            ))
            .toList());

        return responses;
    }
}
