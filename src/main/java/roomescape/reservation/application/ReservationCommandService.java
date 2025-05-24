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
import roomescape.reservation.application.dto.MemberWaitingRequest;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.application.dto.WaitingResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Service
@Transactional
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationCommandService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final MemberRepository memberRepository,
            final WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse addMemberReservation(final MemberReservationRequest request, final Long memberId) {
        final ReservationTime time = getReservationTime(request.timeId());
        final Theme theme = getTheme(request.themeId());
        final Member member = getMember(memberId);

        validateHasTimeConflict(request.date(), time, theme);
        validatePastDateTime(request.date(), time.getStartAt());

        final Reservation reservation = new Reservation(request.date(), time, theme, member);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    public ReservationResponse addAdminReservation(final AdminReservationRequest request) {
        final ReservationTime time = getReservationTime(request.timeId());
        final Theme theme = getTheme(request.themeId());
        final Member member = getMember(request.memberId());

        validateHasTimeConflict(request.date(), time, theme);
        validatePastDateTime(request.date(), time.getStartAt());

        final Reservation reservation = new Reservation(request.date(), time, theme, member);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    public WaitingResponse addMemberWaiting(final MemberWaitingRequest request, final Long memberId) {
        final ReservationTime time = getReservationTime(request.timeId());
        final Theme theme = getTheme(request.themeId());
        final Member member = getMember(memberId);

        validatePastDateTime(request.date(), time.getStartAt());
        validateHasSameReservation(request, member, theme, time);

        final Waiting waiting = new Waiting(request.date(), time, theme, member);
        return WaitingResponse.from(waitingRepository.save(waiting));
    }

    public void cancelOwnWaitingById(final Long id, final Long memberId) {
        final Waiting waiting = getWaiting(id);
        if (!waiting.isOwner(memberId)) {
            throw new BadRequestException("사용자 본인의 예약이 아닙니다.");
        }
        waiting.cancel();
    }

    public void rejectWaitingById(final Long id) {
        final Waiting waiting = getWaiting(id);
        waiting.reject();
    }

    public void deleteReservationById(final Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("존재하지 않는 예약입니다.");
        }
        reservationRepository.deleteById(id);
    }

    public void acceptReservation(final Long id) {
        final Waiting waiting = getWaitingWithAssociations(id);
        validateIsBooked(waiting);
        waiting.accept();
        reservationRepository.save(
                new Reservation(waiting.getDate(), waiting.getTime(), waiting.getTheme(), waiting.getMember()));
    }

    private void validatePastDateTime(final LocalDate date, final LocalTime time) {
        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
            throw new BadRequestException("현재보다 과거의 날짜로 예약할 수 없습니다.");
        }
    }

    private void validateHasSameReservation(
            final MemberWaitingRequest request,
            final Member member,
            final Theme theme,
            final ReservationTime time
    ) {
        if (reservationRepository.existsByDateAndTimeAndThemeAndMember(request.date(), time, theme, member) ||
                waitingRepository.existsByDateAndTimeAndThemeAndMember(request.date(), time, theme, member)
        ) {
            throw new ConflictException("이미 예약 확정 및 대기 건수가 있습니다.");
        }
    }

    private void validateHasTimeConflict(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        boolean isConflict = reservationRepository.findByDateAndThemeIdWithAssociations(date, time.getId())
                .stream()
                .anyMatch(r -> r.hasConflictWith(time, theme));

        if (isConflict) {
            throw new ConflictException("해당 테마 이용시간이 겹칩니다.");
        }
    }

    private void validateIsBooked(final Waiting waiting) {
        boolean isBooked = reservationRepository.existsByDateAndTimeAndTheme(
                waiting.getDate(), waiting.getTime(), waiting.getTheme());

        if (isBooked) {
            throw new ConflictException("이미 예약 확정된 건이 있습니다.");
        }
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

    private Waiting getWaiting(final Long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));
    }

    private Waiting getWaitingWithAssociations(final Long id) {
        return waitingRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));
    }
}

