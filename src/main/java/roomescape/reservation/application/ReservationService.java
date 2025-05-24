package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.admin.reservation.presentation.dto.AdminReservationRequest;
import roomescape.admin.reservation.presentation.dto.AdminWaitingReservationResponse;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.UserReservationsResponse;
import roomescape.reservation.time.application.ReservationTimeService;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.application.ThemeService;
import roomescape.theme.domain.Theme;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberService memberService;
    private final ThemeService themeService;
    private final ReservationTimeService reservationTimeService;
    private final ReservationValidator reservationValidator;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ThemeService themeService,
                              final ReservationTimeService reservationTimeService,
                              final MemberService memberService,
                              final ReservationValidator reservationValidator) {
        this.reservationRepository = reservationRepository;
        this.memberService = memberService;
        this.themeService = themeService;
        this.reservationTimeService = reservationTimeService;
        this.reservationValidator = reservationValidator;
    }

    @Transactional
    public ReservationResponse createReservation(final ReservationRequest request, final Long memberId) {
        return createAndSaveReservationBy(
                memberId,
                request.getThemeId(),
                request.getDate(),
                request.getTimeId(),
                false
        );
    }

    public ReservationResponse createWaitingReservation(final ReservationRequest request, final Long memberId) {
        return createAndSaveReservationBy(
                memberId,
                request.getThemeId(),
                request.getDate(),
                request.getTimeId(),
                true
        );
    }

    @Transactional
    public ReservationResponse createReservation(final AdminReservationRequest request) {
        return createAndSaveReservationBy(
                request.getMemberId(),
                request.getThemeId(),
                request.getDate(),
                request.getTimeId(),
                false
        );
    }

    public List<ReservationResponse> getReservations(Long memberId, Long themeId, LocalDate dateFrom,
                                                     LocalDate dateTo) {
        return reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(memberId, themeId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<UserReservationsResponse> getUserReservations(final Long memberId) {
        return reservationRepository.findReservationsWithRankByMemberId(memberId).stream()
                .map(UserReservationsResponse::new)
                .toList();
    }

    @Transactional
    public void deleteReservationByUser(final Long reservationId, final Long memberId) {
        reservationValidator.validateReservationExists(reservationId);

        Reservation reservation = getReservationById(reservationId);
        reservationValidator.validateUserDeletion(reservation, memberId);

        reservationRepository.delete(reservation);
        acceptStatusByFirstWaiting(reservation);
    }

    @Transactional
    public void deleteReservationByAdmin(final Long reservationId) {
        reservationValidator.validateReservationExists(reservationId);

        Reservation reservation = getReservationById(reservationId);
        reservationRepository.deleteById(reservationId);
        acceptStatusByFirstWaiting(reservation);
    }

    public List<AdminWaitingReservationResponse> getWaitingReservation() {
        return reservationRepository.findReservationsWithRankOfWaitingStatus().stream()
                .map(AdminWaitingReservationResponse::new)
                .toList();
    }

    @Transactional
    public void acceptWaitingReservation(final Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(NoSuchElementException::new);

        reservation.acceptStatus();
    }

    private Reservation getReservationById(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("예약 정보를 찾을 수 없습니다."));
    }

    private ReservationResponse createAndSaveReservationBy(
            final Long memberId,
            final Long themeId,
            final LocalDate date,
            final Long reservationTimeId,
            final boolean isWaiting
    ) {
        Member member = memberService.getMemberById(memberId);
        Theme theme = themeService.getThemeById(themeId);
        ReservationTime reservationTime = reservationTimeService.getReservationTimeById(reservationTimeId);
        reservationValidator.validateReservationDateTime(date, reservationTime);

        if (isWaiting) {
            Reservation waiting = Reservation.createWaiting(member, theme, date, reservationTime);
            return new ReservationResponse(reservationRepository.save(waiting));
        }
        Reservation reserved = Reservation.createReserved(member, theme, date, reservationTime);
        return new ReservationResponse(reservationRepository.save(reserved));
    }

    private void acceptStatusByFirstWaiting(final Reservation reservation) {
        Reservation waitingReservation = reservationRepository.findFirstWaitingReservation(
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme()
        );
        waitingReservation.acceptStatus();
    }
}
