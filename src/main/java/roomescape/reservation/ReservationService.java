package roomescape.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.reservation.ReservationConflictException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsMemberException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsThemeException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsTimeException;
import roomescape.exception.custom.reason.reservation.ReservationNotFoundException;
import roomescape.exception.custom.reason.reservation.ReservationPastDateException;
import roomescape.exception.custom.reason.reservation.ReservationPastTimeException;
import roomescape.member.Member;
import roomescape.member.MemberRepositoryFacade;
import roomescape.reservation.dto.AdminFilterReservationRequest;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.MineReservationResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeRepositoryFacade;
import roomescape.theme.Theme;
import roomescape.theme.ThemeRepositoryFacade;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepositoryFacade reservationRepositoryFacade;
    private final ReservationTimeRepositoryFacade reservationTimeRepositoryFacade;
    private final ThemeRepositoryFacade themeRepositoryFacade;
    private final MemberRepositoryFacade memberRepositoryFacade;

    public ReservationResponse create(final ReservationRequest request, final LoginMember loginMember) {
        final ReservationTime reservationTime = getReservationTimeById(request.timeId());
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberByEmail(loginMember.email());

        validateDuplicateDateTimeAndTheme(request.date(), reservationTime, theme);
        validatePastDateTime(request.date(), reservationTime);

        final Reservation notSavedReservation = new Reservation(request.date(), member, reservationTime, theme,
                ReservationStatus.PENDING);
        final Reservation savedReservation = reservationRepositoryFacade.save(notSavedReservation);
        return ReservationResponse.from(savedReservation);
    }

    public ReservationResponse createForAdmin(final AdminReservationRequest request) {
        final ReservationTime reservationTime = getReservationTimeById(request.timeId());
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberById(request.memberId());

        validateDuplicateDateTimeAndTheme(request.date(), reservationTime, theme);
        validatePastDateTime(request.date(), reservationTime);

        final Reservation notSavedReservation = new Reservation(request.date(), member, reservationTime, theme,
                ReservationStatus.PENDING);
        final Reservation savedReservation = reservationRepositoryFacade.save(notSavedReservation);
        return ReservationResponse.from(savedReservation);
    }

    public ReservationResponse createWaiting(
            final ReservationRequest request, final LoginMember loginMember
    ) {
        final ReservationTime reservationTime = getReservationTimeById(request.timeId());
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberByEmail(loginMember.email());

        validateDuplicateReservation(request, reservationTime, theme, member);
        validatePastDateTime(request.date(), reservationTime);

        final Reservation notSavedReservation = new Reservation(request.date(), member, reservationTime, theme,
                ReservationStatus.WAITING);
        final Reservation savedReservation = reservationRepositoryFacade.save(notSavedReservation);
        return ReservationResponse.from(savedReservation);
    }

    public List<MineReservationResponse> readAllMine(final LoginMember loginMember) {
        final Member member = getMemberByEmail(loginMember.email());
        return reservationRepositoryFacade.findAllByMember(member).stream()
                .map(MineReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> readAll() {
        return reservationRepositoryFacade.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> readAllByMemberAndThemeAndDateRange(final AdminFilterReservationRequest request) {
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberById(request.memberId());

        return reservationRepositoryFacade.findAllByMemberAndThemeAndDateBetween(
                        member, theme,
                        request.from(), request.to()
                ).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> readAllWaiting() {
        return reservationRepositoryFacade.findAllByReservationStatus(ReservationStatus.WAITING).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteById(final Long id) {
        final Reservation reservation = reservationRepositoryFacade.findById(id)
                .orElseThrow(ReservationNotFoundException::new);

        pendingNextReservation(reservation);

        reservationRepositoryFacade.deleteById(id);
    }

    private void pendingNextReservation(final Reservation reservation) {
        if (reservation.isWaiting()) {
            return;
        }

        final List<Reservation> waitingReservations = reservationRepositoryFacade.findAllByDateAndReservationTimeAndThemeAndReservationStatusOrderByAsc(
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme(),
                ReservationStatus.WAITING
        );
        if (!waitingReservations.isEmpty()) {
            final Reservation nextReservation = waitingReservations.getFirst();
            nextReservation.pending();
            reservationRepositoryFacade.save(nextReservation);
        }
    }

    private void validatePastDateTime(final LocalDate date, final ReservationTime reservationTime) {
        final LocalDate today = LocalDate.now();
        final LocalDate reservationDate = date;
        if (reservationDate.isBefore(today)) {
            throw new ReservationPastDateException();
        }
        if (reservationDate.isEqual(today)) {
            validatePastTime(reservationTime);
        }
    }

    private void validatePastTime(final ReservationTime reservationTime) {
        if (reservationTime.isBefore(LocalTime.now())) {
            throw new ReservationPastTimeException();
        }
    }

    private void validateDuplicateDateTimeAndTheme(
            final LocalDate date, final ReservationTime reservationTime,
            final Theme theme
    ) {
        if (reservationRepositoryFacade.existsByReservationTimeAndDateAndThemeAndReservationStatus(
                reservationTime, date, theme, ReservationStatus.PENDING)) {
            throw new ReservationConflictException();
        }
    }

    private Theme getThemeById(final Long themeId) {
        return themeRepositoryFacade.findById(themeId)
                .orElseThrow(ReservationNotExistsThemeException::new);
    }

    private ReservationTime getReservationTimeById(final Long reservationTimeId) {
        return reservationTimeRepositoryFacade.findById(reservationTimeId)
                .orElseThrow(ReservationNotExistsTimeException::new);
    }

    private Member getMemberById(final Long memberId) {
        return memberRepositoryFacade.findById(memberId)
                .orElseThrow(ReservationNotExistsMemberException::new);
    }

    private Member getMemberByEmail(final String email) {
        return memberRepositoryFacade.findByEmail(email)
                .orElseThrow(ReservationNotExistsMemberException::new);
    }

    private void validateDuplicateReservation(final ReservationRequest request, final ReservationTime reservationTime,
                                              final Theme theme,
                                              final Member member) {
        if (reservationRepositoryFacade.existsByDateAndReservationTimeAndThemeAndMember(
                request.date(), reservationTime, theme, member)) {
            throw new ReservationConflictException();
        }
    }
}
