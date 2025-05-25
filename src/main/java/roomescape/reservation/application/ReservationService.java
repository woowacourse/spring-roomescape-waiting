package roomescape.reservation.application;

import static roomescape.reservation.domain.ReservationStatus.BOOKED;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.auth.AuthorizationException;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.ui.dto.request.AvailableReservationTimeRequest;
import roomescape.reservation.ui.dto.request.CreateBookedReservationRequest;
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

    @Transactional
    public ReservationResponse create(
            final CreateBookedReservationRequest.ForMember request,
            final Long memberId
    ) {
        final ReservationTime time = getReservationTime(request.date(), request.timeId());
        final Theme theme = themeRepository.getById(request.themeId());
        final Member member = memberRepository.getById(memberId);

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
        final ReservationTime reservationTime = reservationTimeRepository.getById(timeId);
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        if (reservationDateTime.isBefore(now)) {
            throw new IllegalArgumentException("예약 시간은 현재 시간보다 이후여야 합니다.");
        }

        return reservationTime;
    }

    @Transactional
    public void deleteIfOwner(final Long reservationId, final Long memberId) {
        final Reservation reservation = reservationRepository.getById(reservationId);
        final Member member = memberRepository.getById(memberId);

        if (!Objects.equals(reservation.getMember(), member)) {
            throw new AuthorizationException("본인이 아니면 삭제할 수 없습니다.");
        }

        reservationRepository.deleteById(reservationId);
    }

    @Transactional(readOnly = true)
    public List<AvailableReservationTimeResponse> findAvailableReservationTimes(
            final AvailableReservationTimeRequest request
    ) {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final Theme theme = themeRepository.getById(request.themeId());

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

    @Transactional(readOnly = true)
    public List<ReservationResponse.ForMember> findReservationsByMemberId(final Long memberId) {
        final Member member = memberRepository.getById(memberId);

        return reservationRepository.findAllByMember(member).stream()
                .map(ReservationResponse.ForMember::from)
                .toList();
    }
}
