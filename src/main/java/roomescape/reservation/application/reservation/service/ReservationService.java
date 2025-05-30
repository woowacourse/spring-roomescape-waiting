package roomescape.reservation.application.reservation.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.datetime.CurrentDateTime;
import roomescape.common.exception.RoomescapeException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.reservation.application.reservation.dto.ReservationCreateCommand;
import roomescape.reservation.application.reservation.dto.ReservationInfo;
import roomescape.reservation.application.reservation.dto.ReservationMineInfo;
import roomescape.reservation.application.reservation.dto.ReservationSearchCondition;
import roomescape.reservation.application.waiting.dto.WaitingInfo;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.reservation.ReservationRepository;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.theme.ThemeRepository;
import roomescape.reservation.domain.timeslot.TimeSlot;
import roomescape.reservation.domain.timeslot.TimeSlotRepository;
import roomescape.reservation.domain.waiting.Waiting;
import roomescape.reservation.domain.waiting.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final LocalDate MINIMUM_SEARCH_DATE = LocalDate.of(2000, 1, 1);
    private static final LocalDate MAXIMUM_SEARCH_DATE = LocalDate.of(2999, 12, 31);

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final CurrentDateTime currentDateTime;

    public ReservationService(final ReservationRepository reservationRepository,
                              final WaitingRepository waitingRepository,
                              final TimeSlotRepository timeSlotRepository,
                              final ThemeRepository themeRepository, final MemberRepository memberRepository,
                              final CurrentDateTime dateTimeGenerator) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.currentDateTime = dateTimeGenerator;
    }

    @Transactional
    public ReservationInfo createReservation(final ReservationCreateCommand command) {
        final Reservation reservation = makeReservation(command);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationInfo(savedReservation);
    }

    private Reservation makeReservation(final ReservationCreateCommand command) {
        final TimeSlot timeSlot = findTimeSlot(command.timeId());
        validatePastDateTime(command.date(), timeSlot);
        validateDuplicateReservation(command);
        final Member member = findMember(command.memberId());
        final Theme theme = findTheme(command.themeId());
        return command.convertToEntity(member, timeSlot, theme);
    }

    private void validatePastDateTime(final LocalDate date, final TimeSlot timeSlot) {
        final LocalDate currentDate = currentDateTime.getDate();
        final LocalTime currentTime = currentDateTime.getTime();

        final boolean isPastDate = date.isBefore(currentDate);
        final boolean isSameDateButPastTime = date.isEqual(currentDate) && timeSlot.isBefore(currentTime);

        if (isPastDate || isSameDateButPastTime) {
            throw new RoomescapeException("지나간 날짜와 시간은 예약할 수 없습니다.");
        }
    }

    private void validateDuplicateReservation(final ReservationCreateCommand command) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(command.date(), command.timeId(),
                command.themeId())) {
            throw new RoomescapeException("해당 시간에 이미 예약이 존재합니다.");
        }
    }

    @Transactional
    public void cancelReservationById(final long id) {
        final Reservation reservation = findReservation(id);
        if (waitingRepository.existsByReservation(reservation.date(), reservation.time().id(), reservation.theme().id())) {
            final Waiting waiting = waitingRepository.findTopByReservation(reservation.date(), reservation.time().id(), reservation.theme().id())
                    .orElseThrow(() -> new RoomescapeException("예약 대기가 존재하지 않습니다."));
            final Reservation promotedReservation = new Reservation(waiting.date(), waiting.member(), waiting.time(), waiting.theme());
            reservationRepository.save(promotedReservation);
            waitingRepository.deleteById(waiting.id());
        }
        reservationRepository.deleteById(id);
    }

    public List<ReservationInfo> findReservations() {
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
        final List<WaitingInfo> waitingInfos = waitingRepository.findAllWithRankByMemberId(memberId)
                .stream()
                .map(WaitingInfo::new)
                .toList();

        return Stream.concat(
                reservationInfos.stream().map(ReservationMineInfo::new),
                waitingInfos.stream().map(ReservationMineInfo::new)
        ).sorted(Comparator.comparing(ReservationMineInfo::date)).toList();
    }

    private Reservation findReservation(final long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException("예약이 존재하지 않습니다."));
    }

    private TimeSlot findTimeSlot(final long timeId) {
        return timeSlotRepository.findById(timeId)
                .orElseThrow(() -> new RoomescapeException("예약 시간이 존재하지 않습니다."));
    }

    private Theme findTheme(final long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomescapeException("테마가 존재하지 않습니다."));
    }

    private Member findMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomescapeException("멤버가 존재하지 않습니다."));
    }
}
