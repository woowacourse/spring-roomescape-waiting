package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.MemberFixture;
import roomescape.ThemeFixture;
import roomescape.TimeFixture;
import roomescape.application.ServiceTest;
import roomescape.config.TestConfig;
import roomescape.domain.repository.MemberCommandRepository;
import roomescape.domain.repository.ReservationCommandRepository;
import roomescape.domain.repository.ThemeCommandRepository;
import roomescape.domain.repository.TimeCommandRepository;
import roomescape.domain.repository.WaitingCommandRepository;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@ServiceTest
class WaitingFactoryTest {

    @Autowired
    private WaitingFactory waitingFactory;

    @Autowired
    private ReservationCommandRepository reservationCommandRepository;

    @Autowired
    private TimeCommandRepository timeCommandRepository;

    @Autowired
    private ThemeCommandRepository themeCommandRepository;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @Autowired
    private WaitingCommandRepository waitingCommandRepository;

    @DisplayName("같은 날짜, 시간, 테마에 예약이 되어있으면 예약 대기할 수 없다.")
    @Test
    void createException() {
        Member member = memberCommandRepository.save(
                new Member(new PlayerName("test"), new Email("test@test.com"), new Password("testTest1!"), Role.BASIC));
        LocalDate date = LocalDate.now();
        Time time = timeCommandRepository.save(TimeFixture.defaultValue());
        Theme theme = themeCommandRepository.save(ThemeFixture.defaultValue());

        Reservation reservation = reservationCommandRepository.save(new Reservation(member, date, time, theme));

        assertThatCode(() -> waitingFactory.create(member.getId(), date, time.getId(), theme.getId()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.ALREADY_RESERVED);
    }

    @DisplayName("같은 예약 대기가 있으면 예약 대기할 수 없다.")
    @Test
    void createException2() {
        Member member = memberCommandRepository.save(MemberFixture.defaultValue());
        Member waitMember = memberCommandRepository.save(MemberFixture.defaultValue());
        LocalDate date = LocalDate.now();
        Time time = timeCommandRepository.save(TimeFixture.defaultValue());
        Theme theme = themeCommandRepository.save(ThemeFixture.defaultValue());
        Reservation reservation = reservationCommandRepository.save(new Reservation(member, date, time, theme));

        Waiting waiting = waitingFactory.create(waitMember.getId(), reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId());

        waitingCommandRepository.save(waiting);

        assertThatCode(() -> waitingFactory.create(waitMember.getId(),
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.ALREADY_WAITING);
    }

    @DisplayName("같은 날짜와 시간에 예약 대기가 있으면 예약 대기할 수 없다.")
    @Test
    void createException3() {
        Member member1 = memberCommandRepository.save(MemberFixture.defaultValue());
        Member member2 = memberCommandRepository.save(MemberFixture.defaultValue());
        Member member3 = memberCommandRepository.save(MemberFixture.defaultValue());
        LocalDate date = LocalDate.now();
        Time time1 = timeCommandRepository.save(TimeFixture.defaultValue());
        Theme theme1 = themeCommandRepository.save(ThemeFixture.defaultValue());
        Theme theme2 = themeCommandRepository.save(ThemeFixture.defaultValue());
        reservationCommandRepository.save(new Reservation(member1, date, time1, theme1));
        reservationCommandRepository.save(new Reservation(member2, date, time1, theme2));
        Waiting waiting1 = waitingFactory.create(member3.getId(), date, time1.getId(), theme1.getId());
        waitingCommandRepository.save(waiting1);

        assertThatCode(() -> waitingFactory.create(member3.getId(), date, time1.getId(), theme2.getId()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.ALREADY_WAITING);
    }

    @DisplayName("동일한 날짜와 시간에 예약이 존재하면 예외가 발생한다.")
    @Test
    void createException4() {
        Member member1 = memberCommandRepository.save(MemberFixture.defaultValue());
        Member member2 = memberCommandRepository.save(MemberFixture.defaultValue());
        LocalDate date = LocalDate.now();
        Time time = timeCommandRepository.save(TimeFixture.defaultValue());
        Theme theme1 = themeCommandRepository.save(ThemeFixture.defaultValue());
        Theme theme2 = themeCommandRepository.save(ThemeFixture.defaultValue());

        reservationCommandRepository.save(new Reservation(member1, date, time, theme1));
        reservationCommandRepository.save(new Reservation(member2, date, time, theme2));
        assertThatCode(() -> waitingFactory.create(member1.getId(), date, time.getId(), theme2.getId()))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.ALREADY_WAITING);
    }
}
