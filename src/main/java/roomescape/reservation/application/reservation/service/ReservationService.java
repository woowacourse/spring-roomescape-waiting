package roomescape.reservation.application.reservation.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import roomescape.common.time.CurrentDateTime;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.application.reservation.dto.ReservationCreateCommand;
import roomescape.reservation.application.reservation.dto.ReservationInfo;
import roomescape.reservation.application.reservation.dto.ReservationMineInfo;
import roomescape.reservation.application.reservation.dto.ReservationSearchCondition;
import roomescape.reservation.application.waiting.dto.ReservationWaitingInfo;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.reservation.ReservationRepository;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.theme.ThemeRepository;
import roomescape.reservation.domain.time.ReservationTime;
import roomescape.reservation.domain.time.ReservationTimeRepository;
import roomescape.reservation.domain.waiting.ReservationWaitingRepository;

@Service
public class ReservationService {

    private static final LocalDate MINIMUM_SEARCH_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDate MAXIMUM_SEARCH_DATE = LocalDate.of(2999, 12, 31);

    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;

    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final CurrentDateTime currentDateTime;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationWaitingRepository reservationWaitingRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository, final MemberRepository memberRepository,
                              final CurrentDateTime dateTimeGenerator) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.currentDateTime = dateTimeGenerator;
    }

    public ReservationInfo createReservation(final ReservationCreateCommand command) {
        final Reservation reservation = makeReservation(command);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationInfo(savedReservation);
    }

    private Reservation makeReservation(final ReservationCreateCommand command) {
        final ReservationTime reservationTime = findReservationTime(command.timeId());
        validatePastDateTime(command.date(), reservationTime);
        validateDuplicateReservation(command);
        final Member member = findMember(command.memberId());
        final Theme theme = findTheme(command.themeId());
        return command.convertToEntity(member, reservationTime, theme);
    }

    private void validatePastDateTime(final LocalDate date, final ReservationTime reservationTime) {
        final boolean isBefore = date.isBefore(currentDateTime.getDate()) ||
                date.isEqual(currentDateTime.getDate()) &&
                        reservationTime.isBefore(currentDateTime.getTime());
        if (isBefore) {
            throw new IllegalArgumentException("지나간 날짜와 시간은 예약 불가합니다.");
        }
    }

    private void validateDuplicateReservation(final ReservationCreateCommand command) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(command.date(), command.timeId(),
                command.themeId())) {
            throw new IllegalArgumentException("해당 시간에 이미 예약이 존재합니다.");
        }
    }

    private Member findMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));
    }

    private ReservationTime findReservationTime(final long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException("예약 시간이 존재하지 않습니다."));
    }

    private Theme findTheme(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("테마가 존재하지 않습니다."));
    }

    public List<ReservationInfo> findReservationsBySearchCondition() {
        return reservationRepository.findAll().stream()
                .map(ReservationInfo::new)
                .toList();
    }

    public List<ReservationInfo> findReservationsBySearchCondition(final ReservationSearchCondition condition) {
        final LocalDate fromDate = Optional.ofNullable(condition.fromDate()).orElse(MINIMUM_SEARCH_DATE);
        final LocalDate toDate = Optional.ofNullable(condition.toDate()).orElse(MAXIMUM_SEARCH_DATE);
        return reservationRepository.findAllByCondition(condition.memberId(), condition.themeId(), fromDate, toDate)
                .stream()
                .map(ReservationInfo::new)
                .toList();
    }

    public List<ReservationMineInfo> findReservationsByMemberId(final long memberId) {
        final List<ReservationInfo> reservationInfos = reservationRepository.findAllByMemberId(memberId)
                .stream()
                .map(ReservationInfo::new)
                .toList();
        final List<ReservationWaitingInfo> reservationWaitingInfos = reservationWaitingRepository.findAllWithRankByMemberId(memberId)
                .stream()
                .map(ReservationWaitingInfo::new)
                .toList();

        return Stream.concat(
                reservationInfos.stream().map(ReservationMineInfo::new),
                reservationWaitingInfos.stream().map(ReservationMineInfo::new)
        ).sorted(Comparator.comparing(ReservationMineInfo::date)).toList();
    }

    public void cancelReservationById(final long id) {
        reservationRepository.deleteById(id);
    }
}
