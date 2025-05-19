package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthRole;
import roomescape.auth.domain.MemberAuthInfo;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberQueryRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationCommandRepository;
import roomescape.reservation.domain.ReservationQueryRepository;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeQueryRepository;
import roomescape.reservation.ui.dto.request.AvailableReservationTimeRequest;
import roomescape.reservation.ui.dto.request.CreateReservationRequest;
import roomescape.reservation.ui.dto.request.ReservationsByFilterRequest;
import roomescape.reservation.ui.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.ui.dto.response.ReservationResponse;
import roomescape.reservation.ui.dto.response.ReservationStatusResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationCommandRepository reservationCommandRepository;
    private final ReservationQueryRepository reservationQueryRepository;
    private final ReservationTimeQueryRepository reservationTimeQueryRepository;
    private final ThemeRepository themeRepository;
    private final MemberQueryRepository memberQueryRepository;

    public ReservationResponse create(
            final CreateReservationRequest request
    ) {
        return ReservationResponse.from(
                createReservation(
                        request.date(),
                        request.timeId(),
                        request.themeId(),
                        request.memberId(),
                        request.status()
                ));
    }

    public ReservationResponse create(
            final CreateReservationRequest.ForMember request,
            final Long memberId
    ) {

        return ReservationResponse.from(
                createReservation(
                        request.date(),
                        request.timeId(),
                        request.themeId(),
                        memberId,
                        ReservationStatus.CONFIRMED
                )
        );
    }

    private Reservation createReservation(
            final LocalDate date,
            final Long timeId,
            final Long themeId,
            final Long memberId,
            final ReservationStatus status
    ) {
        final ReservationTime reservationTime = getReservationTime(date, timeId);
        validateNoDuplicateReservation(date, timeId, themeId);

        final Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 테마가 존재하지 않습니다."));
        final Member member = memberQueryRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        final Reservation reservation = new Reservation(date, reservationTime, theme, member, status);

        return reservationCommandRepository.save(reservation);
    }

    private ReservationTime getReservationTime(final LocalDate date, final Long timeId) {
        final ReservationTime reservationTime = reservationTimeQueryRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 시간이 존재하지 않습니다."));
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        if (reservationDateTime.isBefore(now)) {
            throw new IllegalArgumentException("예약 시간은 현재 시간보다 이후여야 합니다.");
        }
        return reservationTime;
    }

    private void validateNoDuplicateReservation(final LocalDate date, final Long timeId, final Long themeId) {
        if (reservationQueryRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            throw new AlreadyExistException("해당 날짜와 시간에 이미 해당 테마에 대한 예약이 있습니다.");
        }
    }

    public void deleteAsAdmin(final Long reservationId, final MemberAuthInfo memberAuthInfo) {
        if (memberAuthInfo.authRole() != AuthRole.ADMIN) {
            throw new AuthorizationException("관리자만 삭제할 권한이 있습니다.");
        }

        try {
            reservationCommandRepository.deleteById(reservationId);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("해당 예약을 찾을 수 없습니다.");
        }
    }

    public void deleteIfOwner(final Long reservationId, final MemberAuthInfo memberAuthInfo) {
        final Reservation reservation = reservationQueryRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약을 찾을 수 없습니다."));
        final Member member = memberQueryRepository.findById(memberAuthInfo.id())
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));

        if (!Objects.equals(reservation.getMember(), member)) {
            throw new AuthorizationException("본인이 아니면 삭제할 수 없습니다.");
        }

        reservationCommandRepository.deleteById(reservationId);
    }

    public List<ReservationResponse> findAll() {
        return reservationQueryRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findAllByFilter(final ReservationsByFilterRequest request) {
        if (request.dateFrom().isAfter(request.dateTo())) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }

        return reservationQueryRepository.findAllByThemeIdAndMemberIdAndDateRange(
                        request.themeId(), request.memberId(), request.dateFrom(), request.dateTo()
                )
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(
            final AvailableReservationTimeRequest request
    ) {
        final List<ReservationTime> reservationTimes = reservationTimeQueryRepository.findAll();
        final List<LocalTime> bookedTimes = reservationQueryRepository.findAllByDateAndThemeId(
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

    public List<ReservationResponse.ForMember> findReservationsByMemberId(final Long memberId) {
        return reservationQueryRepository.findAllByMemberId(memberId).stream()
                .map(ReservationResponse.ForMember::from)
                .toList();
    }

    public List<ReservationStatusResponse> findAllReservationStatuses() {
        return Arrays.stream(ReservationStatus.values())
                .map(ReservationStatusResponse::from)
                .toList();
    }
}
