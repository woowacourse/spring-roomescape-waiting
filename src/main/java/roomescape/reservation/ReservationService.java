package roomescape.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.reservation.ReservationConflictException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsMemberException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsPendingException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsThemeException;
import roomescape.exception.custom.reason.reservation.ReservationNotExistsTimeException;
import roomescape.exception.custom.reason.reservation.ReservationNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.AdminFilterReservationRequest;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.MineReservationResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationResponse create(final ReservationRequest request, final LoginMember loginMember) {
        final ReservationTime reservationTime = getReservationTimeById(request.timeId());
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberByEmail(loginMember.email());

        return save(request.date(), reservationTime, theme, member);
    }

    public ReservationResponse createForAdmin(final AdminReservationRequest request) {
        final ReservationTime reservationTime = getReservationTimeById(request.timeId());
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberById(request.memberId());

        return save(request.date(), reservationTime, theme, member);
    }

    private ReservationResponse save(final LocalDate date, final ReservationTime reservationTime,
                                     final Theme theme, final Member member) {
        final LocalDateTime currentTimestamp = LocalDateTime.now();
        final ReservationDate reservationDate = ReservationDate.of(date, currentTimestamp.toLocalDate());

        validateDuplicatePending(reservationDate, reservationTime, theme);

        final Reservation notSavedReservation = Reservation.of(reservationDate, member, reservationTime, theme,
                ReservationStatus.PENDING, currentTimestamp);
        final Reservation savedReservation = reservationRepository.save(notSavedReservation);
        return ReservationResponse.from(savedReservation);
    }

    public ReservationResponse createWaiting(
            final ReservationRequest request, final LoginMember loginMember
    ) {
        final ReservationTime reservationTime = getReservationTimeById(request.timeId());
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberByEmail(loginMember.email());
        final LocalDateTime currentTimestamp = LocalDateTime.now();
        final ReservationDate reservationDate = ReservationDate.of(request.date(), currentTimestamp.toLocalDate());

        validateNotExistsPending(reservationDate, reservationTime, theme);
        validateDuplicateMember(reservationDate, reservationTime, theme, member);

        final Reservation notSavedReservation = Reservation.of(reservationDate, member, reservationTime, theme,
                ReservationStatus.WAITING, currentTimestamp);
        final Reservation savedReservation = reservationRepository.save(notSavedReservation);
        return ReservationResponse.from(savedReservation);
    }

    public List<MineReservationResponse> readAllMine(final LoginMember loginMember) {
        final Member member = getMemberByEmail(loginMember.email());
        return reservationRepository.findAllWaitingRankByMember(member).stream()
                .map(MineReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> readAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> readAllByMemberAndThemeAndDateRange(final AdminFilterReservationRequest request) {
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberById(request.memberId());
        final ReservationDate fromDate = ReservationDate.fromQuery(request.from());
        final ReservationDate toDate = ReservationDate.fromQuery(request.to());

        return reservationRepository.findAllByMemberAndThemeAndDateBetween(
                        member, theme,
                        fromDate, toDate
                ).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> readAllWaiting() {
        return reservationRepository.findAllByReservationStatus(ReservationStatus.WAITING).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteById(final Long id) {
        final Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);

        pendingNextReservation(reservation);

        reservationRepository.deleteById(id);
    }

    private void pendingNextReservation(final Reservation reservation) {
        if (reservation.isWaiting()) {
            return;
        }

        final List<Reservation> waitingReservations = reservationRepository.findAllByDateAndReservationTimeAndThemeAndReservationStatusOrderByAsc(
                reservation.getDate(),
                reservation.getReservationTime(),
                reservation.getTheme(),
                ReservationStatus.WAITING
        );
        if (!waitingReservations.isEmpty()) {
            final Reservation nextReservation = waitingReservations.getFirst();
            nextReservation.pending();
        }
    }

    private void validateDuplicatePending(
            final ReservationDate date, final ReservationTime reservationTime,
            final Theme theme
    ) {
        if (reservationRepository.existsDuplicateStatus(
                reservationTime, date, theme, ReservationStatus.PENDING)) {
            throw new ReservationConflictException();
        }
    }

    private void validateNotExistsPending(
            final ReservationDate date, final ReservationTime reservationTime,
            final Theme theme
    ) {
        if (!reservationRepository.existsDuplicateStatus(
                reservationTime, date, theme, ReservationStatus.PENDING)) {
            throw new ReservationNotExistsPendingException();
        }
    }

    private void validateDuplicateMember(final ReservationDate date, final ReservationTime reservationTime,
                                         final Theme theme,
                                         final Member member) {
        if (reservationRepository.existsByDuplicateMember(
                date, reservationTime, theme, member)) {
            throw new ReservationConflictException();
        }
    }

    private Theme getThemeById(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(ReservationNotExistsThemeException::new);
    }

    private ReservationTime getReservationTimeById(final Long reservationTimeId) {
        return reservationTimeRepository.findById(reservationTimeId)
                .orElseThrow(ReservationNotExistsTimeException::new);
    }

    private Member getMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(ReservationNotExistsMemberException::new);
    }

    private Member getMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(ReservationNotExistsMemberException::new);
    }
}
