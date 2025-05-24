package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.domain.MemberAuthInfo;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;
import roomescape.reservation.domain.BookingStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationTimeRepository;
import roomescape.reservation.ui.dto.request.AdminCreateReservationRequest;
import roomescape.reservation.ui.dto.request.AvailableReservationTimeRequest;
import roomescape.reservation.ui.dto.request.MemberCreateReservationRequest;
import roomescape.reservation.ui.dto.request.ReservationsByfilterRequest;
import roomescape.reservation.ui.dto.response.AdminReservationResponse;
import roomescape.reservation.ui.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.ui.dto.response.MemberReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public MemberReservationResponse createForMember(
            final MemberCreateReservationRequest request,
            final Long memberId
    ) {
        validateMemberDuplicateReservation(request.date(), request.timeId(), request.themeId(), memberId);
        final BookingStatus bookingStatus = getBookingStatus(request.date(), request.timeId(), request.themeId());

        final Reservation reservation = registerReservation(request.date(), request.timeId(), request.themeId(),
                memberId,
                bookingStatus);

        return MemberReservationResponse.from(reservation,
                reservationRepository.getReservationRankByReservationId(reservation.getId()));
    }

    @Transactional
    public AdminReservationResponse createForAdmin(final AdminCreateReservationRequest request) {
        validateMemberDuplicateReservation(request.date(), request.timeId(), request.themeId(), request.memberId());
        final Reservation reservation = registerReservation(request.date(), request.timeId(), request.themeId(),
                request.memberId(),
                request.status());

        return AdminReservationResponse.from(reservation,
                reservationRepository.getReservationRankByReservationId(reservation.getId()));
    }

    @Transactional
    public void deleteReservation(final Long reservationId, final MemberAuthInfo memberAuthInfo) {
        final Reservation reservation = reservationRepository.getByIdOrThrow(reservationId);
        final Member member = memberRepository.getByIdOrThrow(memberAuthInfo.id());

        validateDeletableBy(reservation, member);

        cancelAndConfirmNextReservation(reservation);
    }

    @Transactional(readOnly = true)
    public List<AdminReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(reservation -> AdminReservationResponse.from(reservation,
                        reservationRepository.getReservationRankByReservationId(reservation.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminReservationResponse> findAllByFilter(final ReservationsByfilterRequest request) {
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        return reservationRepository.findAllByThemeIdAndMemberIdAndDateRange(
                        request.themeId(), request.memberId(), request.dateFrom(), request.dateTo()
                )
                .stream()
                .map(reservation -> AdminReservationResponse.from(reservation,
                        reservationRepository.getReservationRankByReservationId(reservation.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(
            final AvailableReservationTimeRequest request
    ) {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final List<LocalTime> bookedTimes = reservationRepository.findAllByDateAndThemeId(
                        request.date(),
                        request.themeId()
                ).stream()
                .map(reservation -> reservation.getTime().getStartAt())
                .toList();

        return reservationTimes.stream()
                .map(reservationTime ->
                        new AvailableReservationTimeResponse(
                                reservationTime.getId(),
                                reservationTime.getStartAt(),
                                bookedTimes.contains(reservationTime.getStartAt())
                        )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> findReservationsByMemberId(final Long memberId) {
        return reservationRepository.findAllByMemberId(memberId).stream()
                .map(reservation -> MemberReservationResponse.from(reservation,
                        reservationRepository.getReservationRankByReservationId(reservation.getId())))
                .toList();
    }

    private void cancelAndConfirmNextReservation(Reservation reservation) {
        reservationRepository.delete(reservation);

        reservationRepository.findFirstRankWaitingBy(
                        reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId())
                .ifPresent(Reservation::confirmReservation);
    }

    private BookingStatus getBookingStatus(final LocalDate date, final Long timeId, final Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            return BookingStatus.WAITING;
        }
        return BookingStatus.CONFIRMED;
    }

    private Reservation registerReservation(final LocalDate date, final Long timeId, final Long themeId,
                                            final Long memberId, final BookingStatus status) {
        final ReservationTime time = reservationTimeRepository.getByIdOrThrow(timeId);
        final Theme theme = themeRepository.getByIdOrThrow(themeId);
        final Member member = memberRepository.getByIdOrThrow(memberId);
        final Reservation reservation = Reservation.createForRegister(date, time, theme, member, status);
        return reservationRepository.save(reservation);
    }

    private void validateMemberDuplicateReservation(final LocalDate date, final Long timeId, final Long themeId,
                                                    final Long memberId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId)) {
            throw new AlreadyExistException("해당 날짜와 시간에 이미 해당 테마에 대한 예약이 있습니다.");
        }
    }

    private void validateDeletableBy(Reservation reservation, Member member) {
        if (member.isMember() && !Objects.equals(reservation.getMember(), member)) {
            throw new AuthorizationException("삭제할 권한이 없습니다.");
        }
    }
}
