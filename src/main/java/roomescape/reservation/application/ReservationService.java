package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.impl.BadRequestException;
import roomescape.common.exception.impl.ConflictException;
import roomescape.common.exception.impl.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.application.dto.AdminReservationRequest;
import roomescape.reservation.application.dto.AvailableReservationTimeResponse;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.ReservationResponse;
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

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository,
                              final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> findAll() {
        final List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::of)
                .toList();
    }

    public ReservationResponse addMemberReservation(final MemberReservationRequest request, final Long memberId) {
        return addReservation(request.timeId(), request.themeId(), memberId, request.date());
    }

    public ReservationResponse addAdminReservation(final AdminReservationRequest request) {
        return addReservation(request.timeId(), request.themeId(), request.memberId(), request.date());
    }

    public void deleteById(final Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("존재하지 않는 예약입니다.");
        }
        reservationRepository.deleteById(id);
    }

    public List<AvailableReservationTimeResponse> findAvailableReservationTime(final Long themeId, final String date) {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        final Theme selectedTheme = getTheme(themeId);
        final List<Reservation> bookedReservations = reservationRepository.findByDateAndTheme_Id(
                LocalDate.parse(date),
                themeId);
        return getAvailableReservationTimeResponses(reservationTimes, bookedReservations, selectedTheme);
    }

    public List<ReservationResponse> findReservationByThemeIdAndMemberIdInDuration(
            final long themeId,
            final long memberId,
            final LocalDate start,
            final LocalDate end
    ) {
        final List<Reservation> reservations = reservationRepository
                .findByTheme_IdAndMember_IdAndDateBetween(themeId, memberId, start, end);
        return reservations.stream()
                .map(ReservationResponse::of)
                .toList();
    }

    private ReservationResponse addReservation(final Long timeId, final Long themeId, final Long memberId,
                                               final LocalDate date) {
        final ReservationTime reservationTime = getReservationTime(timeId);
        final Theme theme = getTheme(themeId);
        final Member member = getMember(memberId);

        final List<Reservation> sameTimeReservations = reservationRepository.findByDateAndTheme_Id(date,
                themeId);

        validateIsBooked(sameTimeReservations, reservationTime, theme);
        validatePastDateTime(date, reservationTime.getStartAt());

        final Reservation reservation = new Reservation(date, reservationTime, theme, member);
        final Reservation saved = reservationRepository.save(reservation);
        return ReservationResponse.of(saved);
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
