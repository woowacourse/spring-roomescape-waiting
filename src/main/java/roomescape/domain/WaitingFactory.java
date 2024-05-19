package roomescape.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.repository.MemberQueryRepository;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.ThemeQueryRepository;
import roomescape.domain.repository.TimeQueryRepository;
import roomescape.domain.repository.WaitingQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@DomainService
public class WaitingFactory {

    private final ReservationQueryRepository reservationQueryRepository;
    private final TimeQueryRepository timeQueryRepository;
    private final ThemeQueryRepository themeQueryRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final WaitingQueryRepository waitingQueryRepository;
    private final Clock clock;

    public WaitingFactory(ReservationQueryRepository reservationQueryRepository,
                          TimeQueryRepository timeQueryRepository,
                          ThemeQueryRepository themeQueryRepository,
                          MemberQueryRepository memberQueryRepository, WaitingQueryRepository waitingQueryRepository,
                          Clock clock) {
        this.reservationQueryRepository = reservationQueryRepository;
        this.timeQueryRepository = timeQueryRepository;
        this.themeQueryRepository = themeQueryRepository;
        this.memberQueryRepository = memberQueryRepository;
        this.waitingQueryRepository = waitingQueryRepository;
        this.clock = clock;
    }

    public Waiting create(Long memberId, LocalDate date, Long timeId, Long themeId) {
        Time time = timeQueryRepository.getById(timeId);
        Theme theme = themeQueryRepository.getById(themeId);
        Member member = memberQueryRepository.getById(memberId);
        validateDuplicatedReservationWaiting(date, time, theme, member);
        validateRequestDateAfterCurrentTime(date, time.getStartAt());
        return new Waiting(member, date, time, theme);
    }

    private void validateDuplicatedReservationWaiting(LocalDate date, Time time, Theme theme, Member member) {
        Reservation reservation = reservationQueryRepository.getByDateAndTimeAndTheme(date, time, theme);
        if (reservation.isSameMember(member)) {
            throw new RoomescapeException(RoomescapeErrorCode.ALREADY_RESERVED);
        }
        if (waitingQueryRepository.existsByMemberAndDateAndTimeAndTheme(member, date, time, theme)) {
            throw new RoomescapeException(RoomescapeErrorCode.ALREADY_WAITING);
        }
    }

    private void validateRequestDateAfterCurrentTime(LocalDate date, LocalTime time) {
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        LocalDateTime currentTime = LocalDateTime.now(clock);
        if (dateTime.isBefore(currentTime)) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "현재 시간보다 과거로 예약할 수 없습니다.");
        }
    }
}
