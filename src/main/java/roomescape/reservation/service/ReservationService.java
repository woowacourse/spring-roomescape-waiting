package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.NotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSearchRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public List<ReservationResponse> findReservationsByCriteria(final ReservationSearchRequest request) {
        final List<Reservation> reservations = reservationRepository.findByCriteria(request.themeId(),
                request.memberId(), request.dateFrom(),
                request.dateTo());
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAllReservationTime(final LocalDate date, final Long themeId) {
        return reservationTimeRepository.findAllAvailable(date, themeId);
    }

    public ReservationResponse saveReservation(final ReservationRequest request, final LoginMember loginMember) {
        final ReservationTime reservationTime = findReservationTimeById(request.timeId());
        final Member member = findMemberById(loginMember.id());
        final Theme theme = findThemeById(request.themeId());
        return saveReservationInternal(request.date(), reservationTime, theme, member);
    }

    public ReservationResponse saveAdminReservation(final AdminReservationRequest request) {
        final ReservationTime reservationTime = findReservationTimeById(request.timeId());
        final Theme theme = findThemeById(request.themeId());
        final Member member = findMemberById(request.memberId());
        return saveReservationInternal(request.date(), reservationTime, theme, member);
    }

    private ReservationResponse saveReservationInternal(
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme, Member member
    ) {
        Reservation reservation;
        if (hasReservation(date, reservationTime, theme)) {
            reservation = Reservation.waiting(date, reservationTime, theme, member);
        } else {
            reservation = Reservation.booked(date, reservationTime, theme, member);
        }
        final Reservation newReservation = reservationRepository.save(reservation);
        return new ReservationResponse(newReservation);
    }

    public void deleteReservation(final Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("존재하지 않는 예약입니다. id=" + id);
        }
        reservationRepository.deleteById(id);
    }

    public List<MyReservationResponse> findMyReservations(final LoginMember loginMember) {
        final Member member = findMemberById(loginMember.id());
        final List<MyReservationResponse> bookedReservations = reservationRepository.findByMemberAndStatus(member,
                        ReservationStatus.BOOKED).stream()
                .map(MyReservationResponse::new)
                .toList();
        final List<MyReservationResponse> waitingReservations = reservationRepository.findWaitingReservationByMemberWithRank(
                        member).stream()
                .map(MyReservationResponse::new)
                .toList();
        return Stream.concat(bookedReservations.stream(), waitingReservations.stream())
                .toList();
    }

    public List<ReservationResponse> findAllWaitingReservation() {
        final List<Reservation> waitingReservations = reservationRepository.findAllByStatus(ReservationStatus.WAITING);
        return waitingReservations.stream().map(ReservationResponse::new).toList();
    }

    public void approveWaitingReservation(final Long id) {
        final Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약이 존재하지 않습니다."));

        reservation.updateStatus(ReservationStatus.BOOKED);

        reservationRepository.save(reservation);
    }

    private Member findMemberById(final Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 멤버입니다."));
    }

    private ReservationTime findReservationTimeById(final Long timeId) {
        return reservationTimeRepository.findById(timeId).orElseThrow(() -> new NotFoundException("존재하지 않는 예약 시간입니다."));
    }

    private Theme findThemeById(final Long themeId) {
        return themeRepository.findById(themeId).orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
    }

    private boolean hasReservation(final LocalDate date, final ReservationTime time, final Theme theme) {
        return reservationRepository.existsByDateAndTimeAndTheme(date, time, theme);
    }
}
