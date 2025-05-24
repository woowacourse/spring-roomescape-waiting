package roomescape.reservation.application;

import static roomescape.reservation.domain.ReservationStatus.BOOKED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeRepository;
import roomescape.reservation.ui.dto.request.AvailableReservationTimeRequest;
import roomescape.reservation.ui.dto.request.CreateReservationRequest;
import roomescape.reservation.ui.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.ui.dto.response.ReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public ReservationResponse create(
            final CreateReservationRequest.ForMember request,
            final Long memberId
    ) {
        final ReservationTime time = getReservationTime(request.date(), request.timeId());
        final Theme theme = getThemeById(request.themeId());
        final Member member = getMemberById(memberId);

        return ReservationResponse.from(createReservedReservation(request.date(), time, theme, member));
    }

    private Reservation createReservedReservation(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        final ReservationSlot reservationSlot = ReservationSlot.of(date, time, theme);

        if (reservationRepository.existsByReservationSlot(reservationSlot)) {
            throw new AlreadyExistException("해당 예약 슬롯에 예약이 있습니다.");
        }

        final Reservation reservation = Reservation.of(reservationSlot, member, BOOKED);

        return reservationRepository.save(reservation);
    }

    private ReservationTime getReservationTime(final LocalDate date, final Long timeId) {
        final ReservationTime reservationTime = getReservationTime(timeId);
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        if (reservationDateTime.isBefore(now)) {
            throw new IllegalArgumentException("예약 시간은 현재 시간보다 이후여야 합니다.");
        }

        return reservationTime;
    }

    public void deleteIfOwner(final Long reservationId, final Long memberId) {
        final Reservation reservation = getReservationById(reservationId);
        final Member member = getMemberById(memberId);

        if (!Objects.equals(reservation.getMember(), member)) {
            throw new AuthorizationException("본인이 아니면 삭제할 수 없습니다.");
        }

        reservationRepository.deleteById(reservationId);
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(
            final AvailableReservationTimeRequest request
    ) {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final Theme theme = getThemeById(request.themeId());

        final List<LocalTime> bookedTimes = reservationRepository.findAllByDateAndTheme(
                        request.date(),
                        theme
                ).stream()
                .map(reservation -> reservation.getReservationSlot().getTime().getStartAt())
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
        final Member member = getMemberById(memberId);

        return reservationRepository.findAllByMember(member).stream()
                .map(ReservationResponse.ForMember::from)
                .toList();
    }

    private Reservation getReservationById(final Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약을 찾을 수 없습니다."));
    }

    private ReservationTime getReservationTime(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 시간이 존재하지 않습니다."));
    }

    private Theme getThemeById(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 테마가 존재하지 않습니다."));
    }

    private Member getMemberById(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다."));
    }
}
