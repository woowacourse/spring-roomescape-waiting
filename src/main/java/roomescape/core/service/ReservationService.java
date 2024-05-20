package roomescape.core.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.core.domain.Member;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Theme;
import roomescape.core.domain.Waiting;
import roomescape.core.dto.member.LoginMember;
import roomescape.core.dto.reservation.MyReservationResponse;
import roomescape.core.dto.reservation.ReservationRequest;
import roomescape.core.dto.reservation.ReservationResponse;
import roomescape.core.repository.MemberRepository;
import roomescape.core.repository.ReservationRepository;
import roomescape.core.repository.ReservationTimeRepository;
import roomescape.core.repository.ThemeRepository;
import roomescape.core.repository.WaitingRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository,
                              final MemberRepository memberRepository,
                              final WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional
    public ReservationResponse create(final ReservationRequest request) {
        final Reservation reservation = createReservation(request);
        reservation.validateDateAndTime();

        final Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation.getId(), savedReservation);
    }

    private Reservation createReservation(final ReservationRequest request) {
        final Member member = getMemberById(request.getMemberId());
        final ReservationTime reservationTime = getReservationTimeById(request.getTimeId());
        final Theme theme = getThemeById(request.getThemeId());
        final Reservation reservation = new Reservation(member, request.getDate(), reservationTime, theme);

        validateDuplicatedReservation(reservation, reservationTime);

        return reservation;
    }

    private Member getMemberById(final Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private ReservationTime getReservationTimeById(final Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));
    }

    private Theme getThemeById(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));
    }

    private void validateDuplicatedReservation(final Reservation reservation, final ReservationTime reservationTime) {
        final Integer reservationCount = reservationRepository.countByDateAndTimeAndTheme(
                reservation.getDate(), reservationTime, reservation.getTheme());
        if (reservationCount > 0) {
            throw new IllegalArgumentException("해당 시간에 이미 예약 내역이 존재합니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> findAllByMember(final LoginMember loginMember) {
        final Member member = getMemberById(loginMember.getId());

        final List<MyReservationResponse> waitings = getResponsesByWaiting(member);
        final List<MyReservationResponse> reservations = getResponsesByReservation(member);
        reservations.addAll(waitings);

        return reservations;
    }

    private List<MyReservationResponse> getResponsesByReservation(final Member member) {
        return new ArrayList<>(reservationRepository.findAllByMember(member)
                .stream()
                .map(MyReservationResponse::from)
                .toList());
    }

    private List<MyReservationResponse> getResponsesByWaiting(final Member member) {
        return waitingRepository.findAllWithRankByMember(member)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllByMemberAndThemeAndPeriod(final Long memberId, final Long themeId,
                                                                      final String from, final String to) {
        final Member member = getMemberById(memberId);
        final Theme theme = getThemeById(themeId);
        final LocalDate dateFrom = LocalDate.parse(from);
        final LocalDate dateTo = LocalDate.parse(to);

        return reservationRepository.findAllByMemberAndThemeAndDateBetween(member, theme, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    @Transactional
    public void delete(final long id) {
        final Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        reservationRepository.delete(reservation);
        changeFirstWaitingToReservation(reservation);
    }

    private void changeFirstWaitingToReservation(final Reservation reservation) {
        final LocalDate date = reservation.getDate();
        final ReservationTime time = reservation.getReservationTime();
        final Theme theme = reservation.getTheme();

        if (waitingRepository.existsByDateAndTimeAndTheme(date, time, theme)) {
            final Waiting waiting = waitingRepository.findFirstByDateAndTimeAndTheme(date, time, theme);
            final Member member = waiting.getMember();
            final String dateString = date.format(DateTimeFormatter.ISO_DATE);

            final Reservation nextReservation = new Reservation(member, dateString, time, theme);

            waitingRepository.delete(waiting);
            reservationRepository.save(nextReservation);
        }
    }
}
