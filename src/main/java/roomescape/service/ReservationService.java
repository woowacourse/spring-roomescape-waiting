package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.reservationwaiting.ReservationWaitingRepository;
import roomescape.domain.reservationwaiting.WaitingWithRank;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.dto.response.PersonalReservationResponse;
import roomescape.dto.response.ReservationResponse;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationWaitingRepository reservationWaitingRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public List<ReservationResponse> getReservationsByConditions(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        List<Reservation> reservations = reservationRepository
                .findAllByConditions(memberId, themeId, dateFrom, dateTo);

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse addReservation(LocalDate date, long timeId, long themeId, long memberId) {
        Reservation reservation = createReservation(date, timeId, themeId, memberId);
        reservation.validateFutureReservation(LocalDateTime.now(clock));
        validateDuplicatedReservation(reservation);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private Reservation createReservation(LocalDate date, long timeId, long themeId, long memberId) {
        Member member = getMember(memberId);
        ReservationTime reservationTime = getTime(timeId);
        Theme theme = getTheme(themeId);
        return new Reservation(date, member, reservationTime, theme);
    }

    private Member getMember(long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private ReservationTime getTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 테마입니다."));
    }

    private void validateDuplicatedReservation(Reservation reservation) {
        Optional<Reservation> optionalReservation = reservationRepository.findByDateAndTimeAndTheme(
                reservation.getDate(), reservation.getTime(), reservation.getTheme());
        optionalReservation.ifPresent(r -> {
            throw new IllegalArgumentException("해당 날짜/시간에 이미 예약이 존재합니다.");
        });
    }

    @Transactional
    public void deleteReservationById(Long id) {
        if (!reservationRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 id의 예약이 존재하지 않습니다.");
        }

        reservationRepository.deleteById(id);
        // todo 예약 대기 자동 승인
    }

    public List<PersonalReservationResponse> getReservationsByMemberId(long memberId) {
        Member member = getMember(memberId);
        List<Reservation> reservations = reservationRepository.findAllByMember(member);
        List<WaitingWithRank> waitingWithRanks = reservationWaitingRepository.findAllWithRankByMember(member);

        return Stream.concat(
                        reservations.stream().map(PersonalReservationResponse::from),
                        waitingWithRanks.stream().map(PersonalReservationResponse::from))
                .sorted(Comparator.comparing(PersonalReservationResponse::date)
                        .thenComparing(PersonalReservationResponse::time))
                .toList();
    }
}
