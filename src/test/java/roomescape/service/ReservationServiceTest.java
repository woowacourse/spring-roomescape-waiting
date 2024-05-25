package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.DATE_AFTER_1DAY;
import static roomescape.TestFixture.DATE_AFTER_2DAY;
import static roomescape.TestFixture.MEMBER_BROWN;
import static roomescape.TestFixture.MEMBER_NAME;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.ROOM_THEME1;
import static roomescape.TestFixture.VALID_STRING_DATE;
import static roomescape.TestFixture.VALID_STRING_TIME;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;
import roomescape.domain.Status;
import roomescape.exception.BadRequestException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.RoomThemeRepository;
import roomescape.service.dto.AuthInfo;
import roomescape.service.dto.request.ReservationCreateRequest;
import roomescape.service.dto.response.MyReservationResponse;
import roomescape.service.dto.response.ReservationResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private RoomThemeRepository roomThemeRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        List<Reservation> reservations = reservationRepository.findAll();
        for (Reservation reservation : reservations) {
            reservationRepository.deleteById(reservation.getId());
        }
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        for (ReservationTime reservationTime : reservationTimes) {
            reservationTimeRepository.deleteById(reservationTime.getId());
        }
        List<RoomTheme> roomThemes = roomThemeRepository.findAll();
        for (RoomTheme roomTheme : roomThemes) {
            roomThemeRepository.deleteById(roomTheme.getId());
        }
        List<Member> members = memberRepository.findAll();
        for (Member member : members) {
            memberRepository.deleteById(member.getId());
        }
    }

    @DisplayName("현재 로그인된 멤버의 예약을 전부 조회한다.")
    @Test
    @Transactional
    void findMyReservations() {
        // given
        Member member = memberRepository.save(MEMBER_BROWN);
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10AM);
        RoomTheme roomTheme = roomThemeRepository.save(ROOM_THEME1);
        ReservationCreateRequest reservationCreateRequest = new ReservationCreateRequest(member.getId(),
                LocalDate.parse(VALID_STRING_DATE), reservationTime.getId(), roomTheme.getId(), Status.CREATED);
        ReservationResponse reservationResponse = reservationService.save(reservationCreateRequest);
        AuthInfo authInfo = new AuthInfo(member.getId(), member.getEmail(), member.getRole());

        // when
        List<MyReservationResponse> myReservations = reservationService.findMyReservations(authInfo);

        // then
        MyReservationResponse myReservationResponse = myReservations.get(0);

        assertAll(
                () -> assertThat(myReservationResponse.id()).isEqualTo(reservationResponse.id()),
                () -> assertThat(myReservationResponse.theme()).isEqualTo(ROOM_THEME1.getName()),
                () -> assertThat(myReservationResponse.date()).isEqualTo(VALID_STRING_DATE),
                () -> assertThat(myReservationResponse.time()).isEqualTo(VALID_STRING_TIME),
                () -> assertThat(myReservationResponse.status()).isEqualTo(Status.CREATED)
        );
    }


    @DisplayName("모든 예약 검색")
    @Test
    @Transactional
    void findAll() {
        assertThat(reservationService.findAll()).isEmpty();
    }

    @DisplayName("dateFrom이 dateTo보다 이후 시간이면 예외를 발생시킨다.")
    @Test
    void findByDateException() {
        assertThatThrownBy(() -> reservationService.findBy(1L, 1L, DATE_AFTER_2DAY, DATE_AFTER_1DAY))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("날짜를 잘못 입력하셨습니다.");
    }

    @DisplayName("예약 저장")
    @Test
    @Transactional
    void save() {
        // given
        ReservationCreateRequest reservationCreateRequest = createReservationRequest(MEMBER_BROWN,
                RESERVATION_TIME_10AM, ROOM_THEME1, VALID_STRING_DATE);
        // when
        ReservationResponse response = reservationService.save(reservationCreateRequest);
        // then
        assertAll(
                () -> assertThat(reservationService.findAll()).hasSize(1),
                () -> assertThat(response.member().name()).isEqualTo(MEMBER_NAME),
                () -> assertThat(response.theme().name()).isEqualTo(ROOM_THEME1.getName()),
                () -> assertThat(response.date()).isEqualTo(VALID_STRING_DATE),
                () -> assertThat(response.time().startAt()).isEqualTo(VALID_STRING_TIME)
        );
    }

    @DisplayName("지난 예약을 저장하려 하면 예외가 발생한다.")
    @Test
    void pastReservationSaveThrowsException() {
        // given
        ReservationCreateRequest reservationCreateRequest = createReservationRequest(MEMBER_BROWN,
                RESERVATION_TIME_10AM, ROOM_THEME1, "2000-11-09");
        // when & then
        assertThatThrownBy(() -> reservationService.save(reservationCreateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("지나간 날짜와 시간에 대한 예약을 생성할 수 없습니다.");
    }

    @DisplayName("삭제 테스트")
    @Test
    void deleteById() {
        // given
        ReservationCreateRequest request = createReservationRequest(MEMBER_BROWN, RESERVATION_TIME_10AM,
                ROOM_THEME1, VALID_STRING_DATE);
        ReservationResponse response = reservationService.save(request);
        // when
        reservationService.deleteById(response.id());
        // then
        assertThat(reservationService.findAll()).isEmpty();
    }

    private ReservationCreateRequest createReservationRequest(Member member, ReservationTime reservationTime,
                                                              RoomTheme roomTheme, String date) {
        Member savedMember = memberRepository.save(member);
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        RoomTheme savedRoomTheme = roomThemeRepository.save(roomTheme);
        return new ReservationCreateRequest(savedMember.getId(), LocalDate.parse(date),
                savedReservationTime.getId(), savedRoomTheme.getId(), Status.CREATED);
    }
}
