package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.reservation.fixture.MemberFixture.MATT;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.BaseTest;
import roomescape.member.domain.Email;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.repository.MemberJdbcRepository;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.fixture.ReservationDateFixture;
import roomescape.reservation.fixture.ReservationTimeDbFixture;
import roomescape.reservation.fixture.ThemeDbFixture;
import roomescape.theme.domain.Theme;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.time.domain.ReservationTime;
import roomescape.user.controller.dto.ReservationRequest;

public class ReservationServiceTest extends BaseTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeDbFixture reservationTimeDbFixture;

    @Autowired
    private MemberJdbcRepository memberJdbcRepository;

    @Autowired
    private ThemeDbFixture themeDbFixture;

    @Test
    void 예약을_생성한다() {
        memberJdbcRepository.save(new Name("매트"), new Email("matt.kakao"), new Password("1234"));
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();

        ReservationRequest reservationRequest = new ReservationRequest(
                ReservationDateFixture.예약날짜_내일.getDate(),
                reservationTime.getId(),
                theme.getId()
        );

        ReservationResponse response = reservationService.create(MATT.getId(), reservationRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo(MATT.getName());
        assertThat(response.date()).isEqualTo(ReservationDateFixture.예약날짜_내일.getDate());
        assertThat(response.time()).isEqualTo(
                new ReservationTimeResponse(reservationTime.getId(), reservationTime.getStartAt().toString()));
    }

    @Test
    void 예약이_존재하면_예약을_생성할_수_없다() {
        memberJdbcRepository.save(new Name("매트"), new Email("matt.kakao"), new Password("1234"));
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();

        reservationService.create(MATT.getId(), new ReservationRequest(
                ReservationDateFixture.예약날짜_내일.getDate(),
                reservationTime.getId(),
                theme.getId()
        ));

        ReservationRequest request = new ReservationRequest(
                ReservationDateFixture.예약날짜_내일.getDate(),
                reservationTime.getId(),
                theme.getId()
        );

        assertThatThrownBy(() -> reservationService.create(MATT.getId(), request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 예약을_모두_조회한다() {
        memberJdbcRepository.save(new Name("매트"), new Email("matt.kakao"), new Password("1234"));
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();

        reservationService.create(MATT.getId(), new ReservationRequest(
                ReservationDateFixture.예약날짜_내일.getDate(),
                reservationTime.getId(),
                theme.getId()
        ));

        List<ReservationResponse> responses = reservationService.getAll();
        ReservationResponse response = responses.get(0);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo(MATT.getName());
        assertThat(response.date()).isEqualTo(ReservationDateFixture.예약날짜_내일.getDate());
        assertThat(response.time()).isEqualTo(
                new ReservationTimeResponse(reservationTime.getId(), reservationTime.getStartAt().toString()));
    }

    @Test
    void 예약을_삭제한다() {
        memberJdbcRepository.save(new Name("매트"), new Email("matt.kakao"), new Password("1234"));
        ReservationTime reservationTime = reservationTimeDbFixture.예약시간_10시();
        Theme theme = themeDbFixture.공포();

        reservationService.create(MATT.getId(), new ReservationRequest(
                ReservationDateFixture.예약날짜_내일.getDate(),
                reservationTime.getId(),
                theme.getId()
        ));

        reservationService.deleteById(1L);

        List<ReservationResponse> responses = reservationService.getAll();

        assertThat(responses).hasSize(0);
    }

    @Test
    void 존재하지_않는_예약을_삭제할_수_없다() {
        assertThatThrownBy(() -> reservationService.deleteById(3L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
