package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.impl.BadRequestException;
import roomescape.common.exception.impl.ConflictException;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.application.dto.AdminReservationRequest;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.domain.BookingStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Service
@Transactional
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationCommandService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse addMemberReservation(final MemberReservationRequest request, final Long memberId) {
        final ReservationTime time = getReservationTime(request.timeId());
        final Theme theme = getTheme(request.themeId());
        final Member member = getMember(memberId);

        validatePastDateTime(request.date(), time.getStartAt());

        final Reservation reservation = new Reservation(request.date(), time, theme, member, request.status());
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    public ReservationResponse addAdminReservation(final AdminReservationRequest request) {
        final ReservationTime time = getReservationTime(request.timeId());
        final Theme theme = getTheme(request.themeId());
        final Member member = getMember(request.memberId());

        validateHasConflict(request, time, theme);
        validatePastDateTime(request.date(), time.getStartAt());

        final Reservation reservation = new Reservation(request.date(), time, theme, member, BookingStatus.RESERVED);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    public void deleteById(final Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("존재하지 않는 예약입니다.");
        }
        reservationRepository.deleteById(id);
    }

    public void deleteById(final Long reservationId, final Long memberId) {
        if (!reservationRepository.existsByIdAndMemberId(reservationId, memberId)) {
            throw new BadRequestException("사용자 본인의 예약이 아닙니다.");
        }
        deleteById(reservationId);
    }

    public void updateStatus(final Long id) {
        final Reservation reservation = getReservation(id);
        validateIsWaiting(reservation);
        validateIsBooked(reservation);
        reservation.setStatus(BookingStatus.RESERVED);
    }

    private void validatePastDateTime(final LocalDate date, final LocalTime time) {
        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
            throw new BadRequestException("현재보다 과거의 날짜로 예약할 수 없습니다.");
        }
    }

    private void validateHasConflict(
            final AdminReservationRequest request,
            final ReservationTime time,
            final Theme theme
    ) {
        boolean isConflict = reservationRepository.findByDateAndThemeIdWithAssociations(request.date(),
                        request.themeId())
                .stream()
                .anyMatch(r -> r.hasConflictWith(time, theme));

        if (isConflict) {
            throw new ConflictException("해당 테마 이용시간이 겹칩니다.");
        }
    }

    private static void validateIsWaiting(final Reservation reservation) {
        if (!reservation.isWaiting()) {
            throw new BadRequestException("대기 중인 예약이 아닙니다.");
        }
    }

    private void validateIsBooked(final Reservation reservation) {
        boolean isDuplicated = reservationRepository.existsByDateAndThemeAndTimeAndBookingStatus(
                reservation.getDate(), reservation.getTheme(), reservation.getTime(), BookingStatus.RESERVED
        );
        if (isDuplicated) {
            throw new BadRequestException("이미 동일한 시간에 예약된 건이 있습니다.");
        }
    }

    private Reservation getReservation(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("선택한 예약이 존재하지 않습니다."));
    }

    private ReservationTime getReservationTime(final Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new NotFoundException("선택한 예약 시간이 존재하지 않습니다."));
    }

    private Theme getTheme(final Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new NotFoundException("선택한 테마가 존재하지 않습니다."));
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("선택한 멤버가 존재하지 않습니다."));
    }
}

