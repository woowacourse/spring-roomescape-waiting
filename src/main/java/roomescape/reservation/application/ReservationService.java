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
import roomescape.reservation.domain.BookingState;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.infrastructure.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationTimeRepository;
import roomescape.reservation.ui.dto.request.AvailableReservationTimeRequest;
import roomescape.reservation.ui.dto.request.CreateReservationRequest;
import roomescape.reservation.ui.dto.request.ReservationsByfilterRequest;
import roomescape.reservation.ui.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.ui.dto.response.ReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final BookingState DEFAULT_MEMBER_RESERVATION_STATUS = BookingState.WAITING;

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReservationResponse createForAdmin(final CreateReservationRequest.ForAdmin request) {
        return registerReservation(request.date(), request.timeId(), request.themeId(), request.memberId(),
                request.status());
    }

    @Transactional
    public ReservationResponse createForMember(
            final CreateReservationRequest.ForMember request,
            final Long memberId
    ) {
        return registerReservation(request.date(), request.timeId(), request.themeId(), memberId,
                DEFAULT_MEMBER_RESERVATION_STATUS);
    }

    @Transactional
    public void deleteIfOwner(final Long reservationId, final MemberAuthInfo memberAuthInfo) {
        final Reservation reservation = reservationRepository.getByIdOrThrow(reservationId);
        final Member member = memberRepository.getByIdOrThrow(memberAuthInfo.id());

        if (member.isAdmin()) {
            reservationRepository.deleteById(reservationId);
            return;
        }

        if (!Objects.equals(reservation.getMember(), member)) {
            throw new AuthorizationException("삭제할 권한이 없습니다.");
        }

        reservationRepository.deleteById(reservationId);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllByFilter(final ReservationsByfilterRequest request) {
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        return reservationRepository.findAllByThemeIdAndMemberIdAndDateRange(
                        request.themeId(), request.memberId(), request.dateFrom(), request.dateTo()
                )
                .stream()
                .map(ReservationResponse::from)
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
    public List<ReservationResponse.ForMember> findReservationsByMemberId(final Long memberId) {
        return reservationRepository.findAllByMemberId(memberId).stream()
                .map(ReservationResponse.ForMember::from)
                .toList();
    }

    private ReservationResponse registerReservation(final LocalDate date, final Long timeId, final Long themeId,
                                                    final Long memberId, final BookingState state) {
        validateNoDuplicateReservation(date, timeId, themeId);
        final ReservationTime time = reservationTimeRepository.getByIdOrThrow(timeId);
        final Theme theme = themeRepository.getByIdOrThrow(themeId);
        final Member member = memberRepository.getByIdOrThrow(memberId);
        final Reservation reservation = Reservation.createForRegister(date, time, theme, member, state);

        final Reservation saved = reservationRepository.save(reservation);

        return ReservationResponse.from(saved);
    }

    private void validateNoDuplicateReservation(final LocalDate date, final Long timeId, final Long themeId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new AlreadyExistException("해당 날짜와 시간에 이미 해당 테마에 대한 예약이 있습니다.");
        }
    }

}
