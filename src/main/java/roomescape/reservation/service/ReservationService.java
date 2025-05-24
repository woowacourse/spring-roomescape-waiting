package roomescape.reservation.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.ReservationException;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Status;
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

    private final Clock clock;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public List<ReservationResponse> findReservationsByCriteria(final ReservationSearchRequest request) {
        final List<Reservation> reservations = reservationRepository.findByCriteria(request.themeId(),
                request.memberId(), request.dateFrom(), request.dateTo());
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<AvailableReservationTimeResponse> findAllReservationTime(final LocalDate date, final Long themeId) {
        return reservationTimeRepository.findAllAvailable(date, themeId);
    }

    public ReservationResponse saveReservation(final ReservationRequest request, final LoginMember loginMember) {
        final ReservationTime reservationTime = reservationTimeRepository.getById(request.timeId());
        final Theme theme = themeRepository.getById(request.themeId());
        if (reservationRepository.existsByDateAndTimeAndTheme(request.date(), reservationTime, theme)
                && !request.isWaiting()) {
            throw new ReservationException("해당 시간은 이미 예약되어있습니다.");
        }

        final Member member = Member.from(loginMember);
        final Reservation reservation = saveReservation(request.date(), reservationTime, theme, member,
                request.isWaiting());
        return new ReservationResponse(reservation);
    }

    private Reservation saveReservation(LocalDate date, ReservationTime reservationTime, Theme theme,
            Member member, boolean isWaiting) {
        Reservation reservation = null;
        if (isWaiting) {
            Long lastWaitingRank = reservationRepository.getLastWaitingRank(theme, date, reservationTime).orElse(0L);
            reservation = Reservation.waiting(date, reservationTime, theme, member, LocalDateTime.now(clock),
                    lastWaitingRank + 1);
        } else {
            reservation = Reservation.of(date, reservationTime, theme, member, LocalDateTime.now(clock));
        }

        return reservationRepository.save(reservation);
    }

    public ReservationResponse saveAdminReservation(final AdminReservationRequest request) {
        final ReservationTime reservationTime = reservationTimeRepository.getById(request.timeId());
        final Theme theme = themeRepository.getById(request.themeId());
        final Member member = memberRepository.getById(request.memberId());
        if (reservationRepository.existsByDateAndTimeAndTheme(request.date(), reservationTime, theme)) {
            throw new ReservationException("해당 시간은 이미 예약되어있습니다.");
        }
        final Reservation reservation = Reservation.of(request.date(), reservationTime, theme, member,
                LocalDateTime.now(clock));
        final Reservation newReservation = reservationRepository.save(reservation);
        return new ReservationResponse(newReservation);
    }

    public void deleteReservation(final Long id) {
        Reservation reservation = reservationRepository.getById(id);
        Long deleteRank = reservation.getReservationStatus().getRank();
        if (reservation.getReservationStatus().getStatus() == Status.BOOKED) {
            deleteRank = 0L;
        }
        List<ReservationStatus> reservationStatuses = reservationRepository.findAllWaiting(reservation.getDate(), reservation.getTime(),
                reservation.getTheme());
        reduceWaitingRanks(deleteRank, reservationStatuses);
        reservationRepository.deleteById(id);
    }

    private void reduceWaitingRanks(final Long deleteRank, final List<ReservationStatus> reservationStatuses) {
        reservationStatuses.stream()
                .filter(waiting -> waiting.getRank() != null)
                .filter(waiting -> waiting.getRank() > deleteRank)
                .forEach(ReservationStatus::reduceRank);
    }

    public List<MyReservationResponse> findMyReservations(final LoginMember loginMember) {
        final Member member = Member.from(loginMember);
        return reservationRepository.findAllByMember(member).stream()
                .map(MyReservationResponse::new)
                .toList();
    }
}
