package roomescape.common;

import static roomescape.member.role.Role.ADMIN;

import java.time.LocalDateTime;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.reservation.domain.ReservationDate;

public class Constant {
    public static final LocalDateTime NOW = LocalDateTime.now();
    public static final LocalDateTime YESTERDAY = NOW.minusDays(1);
    public static final LocalDateTime TOMORROW = NOW.plusDays(1);

    public static ReservationDate 예약날짜_내일 = new ReservationDate(TOMORROW.toLocalDate());

    public static final Member MATT = new Member(
            1L, new Name("매트"), new Email("matt.kakao"), new Password("1234"), ADMIN
    );
}
