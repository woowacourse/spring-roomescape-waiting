package roomescape.reservationtime.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.constant.TestData.RESERVATION_TIME_COUNT;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exception.ReservationException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.domain.Password;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.RoomEscapeInformation;
import roomescape.reservation.repository.RoomEscapeInformationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.ReservationTimeRequest;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
@Sql("/data.sql")
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private RoomEscapeInformationRepository roomEscapeInformationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationTimeService service;
    private Theme theme;
    private Member member;

    @BeforeEach
    void setUp() {
        service = new ReservationTimeService(reservationTimeRepository, roomEscapeInformationRepository);

        theme = Theme.of("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        member = Member.builder()
                .name("사용자1")
                .email("user1@example.com")
                .password(Password.createForMember("pass123"))
                .role(MemberRole.MEMBER)
                .build();
        memberRepository.save(member);
    }

    @Test
    void 예약_시간이_정상적으로_저장된다() {
        // given
        LocalTime newTime = LocalTime.of(16, 30);
        ReservationTimeRequest request = new ReservationTimeRequest(newTime);

        // when
        ReservationTimeResponse response = service.saveTime(request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.startAt()).isEqualTo(newTime);
    }

    @Test
    void 모든_예약_시간을_조회한다() {
        // given
        LocalTime time1 = LocalTime.of(10, 0);
        LocalTime time2 = LocalTime.of(11, 30);
        reservationTimeRepository.saveAll(List.of(
                ReservationTime.from(time1),
                ReservationTime.from(time2)
        ));

        // when
        List<ReservationTimeResponse> responses = service.findAll();

        // then
        assertThat(responses).hasSize(RESERVATION_TIME_COUNT + 2)
                .extracting(ReservationTimeResponse::startAt)
                .contains(time1, time2);
    }

    @Test
    void 예약이_없는_시간은_삭제된다() {
        // given
        ReservationTime time = ReservationTime.from(LocalTime.of(14, 30));
        ReservationTime savedTime = reservationTimeRepository.save(time);

        // when
        service.delete(savedTime.getId());

        // then
        assertThat(reservationTimeRepository.findById(savedTime.getId())).isEmpty();
    }

    @Test
    void 예약이_있는_시간은_삭제할_수_없다() {
        // given
        ReservationTime time = ReservationTime.from(LocalTime.of(15, 0));
        ReservationTime savedTime = reservationTimeRepository.save(time);

        RoomEscapeInformation info = RoomEscapeInformation.builder()
                .date(LocalDate.of(2999, 12, 31))
                .time(savedTime)
                .theme(theme)
                .build();
        roomEscapeInformationRepository.save(info);

        // when & then
        assertThatThrownBy(() -> service.delete(savedTime.getId()))
                .isInstanceOf(ReservationException.class)
                .hasMessage("해당 시간으로 예약된 건이 존재합니다.");
    }
}
