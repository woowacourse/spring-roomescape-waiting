package roomescape.service;

import static roomescape.domain.reservation.ReservationStatus.RESERVED;
import static roomescape.domain.reservation.ReservationStatus.WAITING;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Date;
import roomescape.domain.reservation.RankCalculator;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationWithRank;
import roomescape.domain.theme.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.system.exception.RoomescapeException;

@Service
public class ReservationService {

    public static final int EXCLUDE_CURRENT_RESERVATION = 1;
    public static final int DEFAULT_RANK = 0;

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final RankCalculator rankCalculator;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationTimeRepository reservationTimeRepository,
        ThemeRepository themeRepository,
        MemberRepository memberRepository,
        RankCalculator rankCalculator
    ) {

        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.rankCalculator = rankCalculator;
    }

    public Reservation save(Long memberId, String date, Long timeId, Long themeId) {
        Member member = findMember(memberId);
        ReservationTime time = findTime(timeId);
        Theme theme = findTheme(themeId);
        Reservation reservation = new Reservation(member, date, time, theme, RESERVED);

        List<Reservation> reservations = reservationRepository.findAllByDateAndTimeIdAndThemeId(
            new Date(date), timeId, themeId);
        reservation.validateDuplication(reservations);

        return reservationRepository.save(reservation);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new RoomescapeException("입력한 사용자 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    private ReservationTime findTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
            .orElseThrow(() -> new RoomescapeException("입력한 시간 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
            .orElseThrow(() -> new RoomescapeException("입력한 테마 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    public void delete(Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException("ID에 해당하는 예약이 존재하지 않습니다."));

        List<Reservation> reservations = reservationRepository.findAllByDateAndTimeIdAndThemeId(
            reservation.getDate(),
            reservation.getTime().getId(),
            reservation.getTheme().getId());

        reservations.stream()
            .sorted(Comparator.comparingLong(Reservation::getId))
            .skip(EXCLUDE_CURRENT_RESERVATION)
            .findFirst()
            .ifPresentOrElse(
                waiting -> {
                    waiting.setStatus(RESERVED);
                    reservationRepository.save(waiting);
                    reservationRepository.deleteById(id);
                },
                () -> reservationRepository.deleteById(id)
            );
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll().stream().filter(reservation -> reservation.isNotWaiting()).toList();
    }

    public List<Reservation> findAllBy(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
    ) {
        if (dateFrom.isAfter(dateTo)) {
            throw new RoomescapeException("날짜 조회 범위가 올바르지 않습니다.");
        }
        return reservationRepository.findAllByThemeIdAndMemberIdAndDateIsBetween(themeId, memberId,
            new Date(dateFrom.toString()), new Date(dateTo.toString()));
    }

    public List<ReservationWithRank> findMyReservations(Long memberId) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(memberId);

        return reservations.stream()
            .map(reservation -> new ReservationWithRank(
                reservation.getId(),
                reservation.getMember(),
                reservation.getDate().getValue().toString(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getStatus(),
                reservation.isWaiting() ? rankCalculator.calculate(reservation) : DEFAULT_RANK
            ))
            .toList();
    }

    public Reservation saveWaiting(Long memberId, String date, Long timeId, Long themeId) {
        Member member = findMember(memberId);
        ReservationTime time = findTime(timeId);
        Theme theme = findTheme(themeId);

        if (reservationRepository.existsByMemberIdAndTimeIdAndThemeIdAndDate(memberId, timeId, themeId, new Date(date))) {
            throw new RoomescapeException("동일한 멤버가 다수의 예약을 생성할 수 없습니다.");
        }

        Reservation reservation = new Reservation(member, date, time, theme, WAITING);

        return reservationRepository.save(reservation);
    }

    public void deleteWaiting(Member member, Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException("주어진 id에 해당하는 예약대기가 존재하지 않습니다."));
        reservation.validateNotWaiting();

        if (member.isNotAdmin()) {
            reservation.validateNotMyWaiting(member.getId());
        }

        reservationRepository.deleteById(id);
    }

    public List<Reservation> findAllWaitingReservations() {
        return reservationRepository.findAllByStatus(WAITING);
    }
}
