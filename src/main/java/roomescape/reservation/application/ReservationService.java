package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.impl.BadRequestException;
import roomescape.common.exception.impl.ConflictException;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.application.dto.AdminReservationRequest;
import roomescape.reservation.application.dto.AvailableReservationTimeResponse;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.MyReservationResponse;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.domain.BookingStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
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

    @Transactional(readOnly = true)
    public List<ReservationResponse> findReservedReservations() {
        final List<Reservation> reservations = reservationRepository.findByStatusWithAssociations(
                BookingStatus.RESERVED);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> findWaitingReservations() {
        final List<Reservation> reservations = reservationRepository.findByStatusWithAssociations(
                BookingStatus.WAITING);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse addMemberReservation(final MemberReservationRequest request, final Long memberId) {
        final ReservationTime reservationTime = getReservationTime(request.timeId());
        final Theme theme = getTheme(request.themeId());
        final Member member = getMember(memberId);

        validatePastDateTime(request.date(), reservationTime.getStartAt());

        final Reservation reservation = new Reservation(request.date(), reservationTime, theme, member,
                request.status());
        final Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    @Transactional
    public ReservationResponse addAdminReservation(final AdminReservationRequest request) {
        final ReservationTime reservationTime = getReservationTime(request.timeId());
        final Theme theme = getTheme(request.themeId());
        final Member member = getMember(request.memberId());

        final List<Reservation> sameDateAndThemeReservations = reservationRepository.findByDateAndThemeIdWithAssociations(
                request.date(),
                request.themeId());

        validateIsBooked(sameDateAndThemeReservations, reservationTime, theme);
        validatePastDateTime(request.date(), reservationTime.getStartAt());

        final Reservation reservation = new Reservation(request.date(), reservationTime, theme, member,
                BookingStatus.RESERVED);
        final Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.from(saved);
    }

    @Transactional
    public void deleteById(final Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("존재하지 않는 예약입니다.");
        }
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void deleteById(final Long reservationId, final Long memberId) {
        if (!reservationRepository.existsByIdAndMemberId(reservationId, memberId)) {
            throw new BadRequestException("사용자 본인의 예약이 아닙니다.");
        }
        deleteById(reservationId);
    }

    @Transactional(readOnly = true)
    public List<AvailableReservationTimeResponse> findAvailableReservationTime(final Long themeId, final String date) {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final Theme selectedTheme = getTheme(themeId);
        final List<Reservation> bookedReservations = reservationRepository.findByDateAndThemeIdWithAssociations(
                LocalDate.parse(date),
                themeId);
        return getAvailableReservationTimeResponses(reservationTimes, bookedReservations, selectedTheme);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findReservationByThemeIdAndMemberIdInDuration(
            final Long themeId,
            final Long memberId,
            final LocalDate start,
            final LocalDate end
    ) {
        final List<Reservation> reservations = reservationRepository
                .findByFilteringWithAssociations(themeId, memberId, start, end, BookingStatus.RESERVED);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> findByMemberId(final Long memberId) {
        final List<Reservation> reservations = reservationRepository.findByMemberIdWithAssociations(memberId);
        return reservations.stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(final Long id) {
        final Reservation reservation = getReservation(id);
        if (!reservation.isWaiting()) {
            throw new BadRequestException("대기 중인 예약이 아닙니다.");
        }
        boolean isDuplicated = reservationRepository.existsByDateAndThemeAndTimeAndBookingStatus(
                reservation.getDate(),
                reservation.getTheme(),
                reservation.getTime(),
                BookingStatus.RESERVED
        );
        if (isDuplicated) {
            throw new BadRequestException("이미 동일한 시간에 예약된 건이 있습니다.");
        }
        reservation.setStatus(BookingStatus.RESERVED);
    }

    private void validateIsBooked(final List<Reservation> sameTimeReservations, final ReservationTime reservationTime,
                                  final Theme theme) {
        final boolean isBooked = sameTimeReservations.stream()
                .anyMatch(reservation -> reservation.hasConflictWith(reservationTime, theme));
        if (isBooked) {
            throw new ConflictException("해당 테마 이용시간이 겹칩니다.");
        }
    }

    private void validatePastDateTime(final LocalDate date, final LocalTime time) {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time);
        if (reservationDateTime.isBefore(now)) {
            throw new BadRequestException("현재보다 과거의 날짜로 예약 할 수 없습니다.");
        }
    }

    private List<AvailableReservationTimeResponse> getAvailableReservationTimeResponses(
            final List<ReservationTime> reservationTimes,
            final List<Reservation> bookedReservations,
            final Theme selectedTheme
    ) {
        final List<AvailableReservationTimeResponse> responses = new ArrayList<>();
        for (final ReservationTime reservationTime : reservationTimes) {
            final boolean isBooked = bookedReservations.stream()
                    .anyMatch(reservation -> reservation.hasConflictWith(reservationTime, selectedTheme));
            final AvailableReservationTimeResponse response = AvailableReservationTimeResponse
                    .from(reservationTime, isBooked);
            responses.add(response);
        }
        return responses;
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
