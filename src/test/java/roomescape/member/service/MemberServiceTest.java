package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import roomescape.auth.login.presentation.dto.LoginMemberInfo;
import roomescape.member.domain.MemberRepository;
import roomescape.member.infrastructure.JpaMemberRepository;
import roomescape.member.infrastructure.JpaMemberRepositoryAdapter;
import roomescape.member.presentation.dto.MyReservationResponse;
import roomescape.member.service.MemberServiceTest.MemberConfig;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.domain.ReservationTime;

@DataJpaTest
@Import(MemberConfig.class)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    /*
INSERT INTO reservation (date, time_id, theme_id, member_id, status) VALUES ('2025-05-13', 1, 1, 1, 'RESERVED');
INSERT INTO reservation (date, time_id, theme_id, member_id, status) VALUES ('2025-05-13', 2, 1, 1, 'RESERVED');
     */
    @DisplayName("내 예약을 조회 할 수 있다.")
    @Test
    void can_find_my_reservation() {
        LoginMemberInfo loginMemberInfo = new LoginMemberInfo(1L);

        List<MyReservationResponse> result = memberService.getMemberReservations(loginMemberInfo);

        List<MyReservationResponse> expected = List.of(
            new MyReservationResponse(1L, "테마1", LocalDate.of(2025, 4, 28), LocalTime.of(10, 0), "예약"),
            new MyReservationResponse(2L, "테마1", LocalDate.of(2025, 4, 28), LocalTime.of(11, 0), "예약"));
        assertThat(result).isEqualTo(expected);
    }

    static class MemberConfig {

        @Bean
        private MemberRepository memberRepository(JpaMemberRepository jpaMemberRepository) {
            return new JpaMemberRepositoryAdapter(jpaMemberRepository);
        }

        @Bean
        private MemberService memberService(MemberRepository memberRepository) {
            return new MemberService(memberRepository);
        }
    }
}
