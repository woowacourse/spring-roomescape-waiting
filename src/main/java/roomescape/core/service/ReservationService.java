package roomescape.core.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.core.domain.Member;
import roomescape.core.domain.Reservation;
import roomescape.core.domain.ReservationTime;
import roomescape.core.domain.Status;
import roomescape.core.domain.Theme;
import roomescape.core.dto.member.LoginMember;
import roomescape.core.dto.reservation.MyReservationResponse;
import roomescape.core.dto.reservation.ReservationRequest;
import roomescape.core.dto.reservation.ReservationResponse;
import roomescape.core.repository.MemberRepository;
import roomescape.core.repository.ReservationRepository;
import roomescape.core.repository.ReservationTimeRepository;
import roomescape.core.repository.ThemeRepository;

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

    @Transactional
    public ReservationResponse create(final ReservationRequest request) {
        final Member member = getMember(request);
        final ReservationTime reservationTime = getReservationTime(request);
        final Theme theme = getTheme(request);
        final Reservation reservation = new Reservation(
                member, request.getDate(), reservationTime, theme, Status.BOOKED, LocalDateTime.now());

        validateDuplicatedReservation(reservation);
        reservation.validateDateAndTime();

        final Reservation savedReservation = reservationRepository.save(reservation);

        return new ReservationResponse(savedReservation.getId(), savedReservation);
    }

    @Transactional
    public ReservationResponse createWaiting(final ReservationRequest request) {
        final Member member = getMember(request);
        final ReservationTime reservationTime = getReservationTime(request);
        final Theme theme = getTheme(request);
        final Reservation reservation = new Reservation(
                member, request.getDate(), reservationTime, theme, Status.STANDBY, LocalDateTime.now());

        validateDuplicatedMemberWaiting(reservation);
        reservation.validateDateAndTime();

        final Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation.getId(), savedReservation);
    }

    private Theme getTheme(ReservationRequest request) {
        return themeRepository.findById(request.getThemeId())
                .orElseThrow(IllegalArgumentException::new);
    }

    private ReservationTime getReservationTime(ReservationRequest request) {
        return reservationTimeRepository.findById(request.getTimeId())
                .orElseThrow(IllegalArgumentException::new);
    }

    private Member getMember(ReservationRequest request) {
        return memberRepository.findById(request.getMemberId())
                .orElseThrow(IllegalArgumentException::new);
    }

    private void validateDuplicatedReservation(final Reservation reservation) {
        final Integer reservationCount = reservationRepository.countByDateAndTimeAndTheme(
                reservation.getDate(), reservation.getReservationTime(), reservation.getTheme());
        if (reservationCount > 0) {
            throw new IllegalArgumentException("해당 시간에 이미 예약 내역이 존재합니다.");
        }
    }

    private void validateDuplicatedMemberWaiting(final Reservation reservation) {
        final Integer waitingCount = reservationRepository.countByMemberAndDateAndTimeAndTheme(
                reservation.getMember(), reservation.getDate(), reservation.getReservationTime(), reservation.getTheme()
        );
        if (waitingCount > 0) {
            throw new IllegalArgumentException("해당 시간에 이미 예약 대기 내역이 존재합니다.");
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
        final Member member = memberRepository.findById(loginMember.getId())
                .orElseThrow(IllegalArgumentException::new);
        return reservationRepository.findAllByMember(member)
                .stream()
                .map(reservation -> new MyReservationResponse(reservation.getId(), reservation.getTheme().getName(),
                        reservation.getDateString(), reservation.getReservationTime().getStartAtString(),
                        getStatus(reservation)))
                .toList();
    }

    private String getStatus(final Reservation reservation) {
        Integer rank = findRankByCreateAt(reservation);
        if (rank == 0) {
            return reservation.getStatus().getValue();
        }
        return reservation.getStatus().waitingRankStatus(rank);
    }

    private Integer findRankByCreateAt(final Reservation reservation) {
        List<Reservation> reservations = reservationRepository.findAllByDateAndTimeAndThemeOrderByCreateAtAsc(
                reservation.getDate(), reservation.getReservationTime(), reservation.getTheme());
        return IntStream.range(0, reservations.size())
                .filter(i -> reservations.get(i).isEqualCreateAt(reservation))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    @Transactional
    public void delete(final long id) {
        reservationRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllByMemberAndThemeAndPeriod(final Long memberId, final Long themeId,
                                                                      final String from, final String to) {
        final LocalDate dateFrom = LocalDate.parse(from);
        final LocalDate dateTo = LocalDate.parse(to);
        return reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(memberId, themeId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }
}
