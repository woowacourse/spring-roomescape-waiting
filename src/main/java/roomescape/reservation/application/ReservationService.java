package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import roomescape.reservation.application.dto.MyReservation;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSchedule;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationScheduleService reservationScheduleService;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(
        final ReservationScheduleService reservationScheduleService,
        final ReservationRepository reservationRepository,
        final MemberRepository memberRepository,
        final WaitingRepository waitingRepository) {
        this.reservationScheduleService = reservationScheduleService;
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<ReservationResponse> findAll() {
        final List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
            .map(ReservationResponse::of)
            .toList();
    }

    @Transactional
    public ReservationResponse addMemberReservation(final MemberReservationRequest request,
        final Long memberId) {
        return addReservation(request, memberId);
    }

    @Transactional
    public ReservationResponse addAdminReservation(final AdminReservationRequest request) {
        return addReservation(
            new MemberReservationRequest(request.date(), request.timeId(), request.themeId()),
            request.memberId());
    }

    @Transactional
    public void deleteById(final Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("존재하지 않는 예약입니다.");
        }
        reservationRepository.deleteById(id);
    }

    @Transactional
    public void deleteReservationAndGetFirstWaiting(final Long reservationId) {
        Reservation reservation = getReservation(reservationId);
        deleteById(reservation.getId());
        waitingRepository.findFirstWaiting(
            reservation.getTheme().getId(),
            reservation.getDate(),
            reservation.getTime().getId()
        ).ifPresent(waiting -> {
            waitingRepository.delete(waiting);
            reservationRepository.save(waiting.convertToReservation());
        });
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTime(final Long themeId,
        final String date) {
        final List<ReservationTime> reservationTimes = reservationScheduleService.getAllReservationTimes();
        final Theme selectedTheme = reservationScheduleService.findThemeById(themeId);
        final List<Reservation> bookedReservations = reservationRepository.findByDateAndThemeId(
            LocalDate.parse(date),
            themeId);
        return getAvailableReservationTimeResponses(reservationTimes, bookedReservations,
            selectedTheme);
    }

    public List<ReservationResponse> findReservationByThemeIdAndMemberIdInDuration(
        final long themeId,
        final long memberId,
        final LocalDate start,
        final LocalDate end
    ) {
        final List<Reservation> reservations = reservationRepository
            .findByThemeIdAndMemberIdAndDateBetween(themeId, memberId, start, end);
        return reservations.stream()
            .map(ReservationResponse::of)
            .toList();
    }

    public List<MyReservation> findByMemberId(final Long memberId) {
        final List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        return reservations.stream()
            .map(MyReservation::from)
            .toList();
    }

    private ReservationResponse addReservation(final MemberReservationRequest request,
        Long memberId) {
        final Member member = getMember(memberId);
        ReservationSchedule schedule = reservationScheduleService.createReservationSchedule(
            request);
        final List<Reservation> sameTimeReservations = reservationRepository.findByDateAndThemeId(
            schedule.getDate(), schedule.getThemeId());
        validateIsBooked(sameTimeReservations, schedule);
        validatePastDateTime(schedule);

        final Reservation reservation = new Reservation(schedule, member);
        final Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.of(saved);
    }

    private void validateIsBooked(final List<Reservation> sameTimeReservations,
        final ReservationSchedule reservationSchedule) {
        final boolean isBooked = sameTimeReservations.stream()
            .anyMatch(
                reservation -> reservation.hasConflictWith(reservationSchedule.getReservationTime(),
                    reservationSchedule.getTheme()));
        if (isBooked) {
            throw new ConflictException("해당 테마 이용시간이 겹칩니다.");
        }
    }

    private void validatePastDateTime(ReservationSchedule reservationSchedule) {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime reservationDateTime = LocalDateTime.of(reservationSchedule.getDate(),
            reservationSchedule.getStartAt());
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
                .anyMatch(
                    reservation -> reservation.hasConflictWith(reservationTime, selectedTheme));
            final AvailableReservationTimeResponse response = AvailableReservationTimeResponse
                .from(reservationTime, isBooked);
            responses.add(response);
        }
        return responses;
    }

    private Reservation getReservation(final Long reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new NotFoundException("선택한 예약이 존재하지 않습니다."));
    }

    private Member getMember(final Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException("선택한 멤버가 존재하지 않습니다."));
    }
}
