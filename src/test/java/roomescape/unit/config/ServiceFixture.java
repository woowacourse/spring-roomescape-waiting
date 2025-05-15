package roomescape.unit.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import roomescape.infrastructure.auth.jwt.JwtTokenProvider;
import roomescape.unit.repository.member.FakeMemberRepository;
import roomescape.unit.repository.reservation.FakeReservationRepository;
import roomescape.unit.repository.reservation.FakeReservationTimeRepository;
import roomescape.unit.repository.reservation.FakeThemeRepository;
import roomescape.unit.repository.reserveticket.FakeReserveTicketRepository;

public class ServiceFixture {

    public final static String TEST_SECRET_KEY = "testtesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttesttest";

    public static JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(TEST_SECRET_KEY);
    }

    public static BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static FakeMemberRepository fakeMemberRepository() {
        return new FakeMemberRepository();
    }

    public static FakeReservationRepository fakeReservationRepository() {
        return new FakeReservationRepository();
    }

    public static FakeReservationTimeRepository fakeReservationTimeRepository() {
        return new FakeReservationTimeRepository();
    }

    public static FakeThemeRepository fakeThemeRepository() {
        return new FakeThemeRepository();
    }

    public static FakeReserveTicketRepository fakeReservationMemberRepository() {
        return new FakeReserveTicketRepository();
    }
}
