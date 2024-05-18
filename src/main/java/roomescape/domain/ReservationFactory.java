package roomescape.domain;

import java.time.Clock;
import java.time.LocalDateTime;
import roomescape.application.dto.ReservationRequest;
import roomescape.domain.repository.MemberQueryRepository;
import roomescape.domain.repository.ReservationQueryRepository;
import roomescape.domain.repository.ThemeQueryRepository;
import roomescape.domain.repository.TimeQueryRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@DomainService
public class ReservationFactory {

    private final ReservationQueryRepository reservationQueryRepository;
    private final TimeQueryRepository timeQueryRepository;
    private final ThemeQueryRepository themeQueryRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final Clock clock;

    public ReservationFactory(ReservationQueryRepository reservationQueryRepository,
                              TimeQueryRepository timeQueryRepository,
                              ThemeQueryRepository themeQueryRepository,
                              MemberQueryRepository memberQueryRepository,
                              Clock clock) {
        this.reservationQueryRepository = reservationQueryRepository;
        this.timeQueryRepository = timeQueryRepository;
        this.themeQueryRepository = themeQueryRepository;
        this.memberQueryRepository = memberQueryRepository;
        this.clock = clock;
    }

    public Reservation create(long memberId, ReservationRequest request) {
        Member member = memberQueryRepository.getById(memberId);
        Theme theme = themeQueryRepository.getById(request.themeId());
        Time time = timeQueryRepository.getById(request.timeId());
        LocalDateTime dateTime = LocalDateTime.of(request.date(), time.getStartAt());
        validateRequestDateAfterCurrentTime(dateTime);
        validateUniqueReservation(request);
        return request.toReservation(member, time, theme);
    }

    private void validateRequestDateAfterCurrentTime(LocalDateTime dateTime) {
        LocalDateTime currentTime = LocalDateTime.now(clock);
        if (dateTime.isBefore(currentTime)) {
            throw new RoomescapeException(RoomescapeErrorCode.BAD_REQUEST, "현재 시간보다 과거로 예약할 수 없습니다.");
        }
    }

    private void validateUniqueReservation(ReservationRequest request) {
        if (reservationQueryRepository.existsByDateAndTimeIdAndThemeId(request.date(), request.timeId(), request.themeId())) {
            throw new RoomescapeException(RoomescapeErrorCode.DUPLICATED_RESERVATION, "이미 존재하는 예약입니다.");
        }
    }
}
