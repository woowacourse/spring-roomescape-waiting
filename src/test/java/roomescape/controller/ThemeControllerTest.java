package roomescape.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.AcceptanceTest;

class ThemeControllerTest extends AcceptanceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Clock clock;

    @Test
    void 테마_목록을_조회한다() {
        createTheme("방탈출1", "다함께 탈출해요 방탈출.", "https://asdfsdf.sdfs");

        RestAssured.given().log().all()
                .when().get("/themes")
                .then().log().all()
                .statusCode(200)
                .body("name", hasItem("방탈출1"));
    }

    @Test
    void 최근_7일간_인기_테마_상위_10개를_조회한다() {
        // given
        createRankingData();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/themes/rankings")
                .then().log().all()
                .statusCode(200)
                .body("name", contains(
                        "공포의 저택",
                        "사라진 연구소",
                        "시간 여행자",
                        "감옥 탈출",
                        "마법사의 방",
                        "좀비 바이러스",
                        "해적의 보물",
                        "스파이 미션",
                        "우주 정거장",
                        "고대 유적"
                ))
                .body("name", not(hasItem("지하 벙커")));
    }

    private void createRankingData() {
        LocalDate baseDate = LocalDate.now(clock);

        long time10 = createTime("10:00");
        long time11 = createTime("11:00");
        long time12 = createTime("12:00");
        long time13 = createTime("13:00");
        long time14 = createTime("14:00");
        long time15 = createTime("15:00");
        long time16 = createTime("16:00");
        long time17 = createTime("17:00");
        long time18 = createTime("18:00");

        long horror = createTheme("공포의 저택", "오래된 저택에서 탈출하세요", "https://example.com/theme1.jpg");
        long lab = createTheme("사라진 연구소", "비밀 연구소의 진실을 밝혀내세요", "https://example.com/theme2.jpg");
        long timeTravel = createTheme("시간 여행자", "시간의 틈에서 탈출하세요", "https://example.com/theme3.jpg");
        long prison = createTheme("감옥 탈출", "제한 시간 안에 감옥을 탈출하세요", "https://example.com/theme4.jpg");
        long wizard = createTheme("마법사의 방", "마법사의 숨겨진 방을 탐험하세요", "https://example.com/theme5.jpg");
        long zombie = createTheme("좀비 바이러스", "바이러스가 퍼진 도시에서 살아남으세요", "https://example.com/theme6.jpg");
        long pirate = createTheme("해적의 보물", "해적선에 숨겨진 보물을 찾으세요", "https://example.com/theme7.jpg");
        long spy = createTheme("스파이 미션", "비밀 요원이 되어 임무를 완수하세요", "https://example.com/theme8.jpg");
        long space = createTheme("우주 정거장", "고장난 우주 정거장에서 탈출하세요", "https://example.com/theme9.jpg");
        long ancient = createTheme("고대 유적", "고대 유적의 수수께끼를 풀어보세요", "https://example.com/theme10.jpg");
        long bunker = createTheme("지하 벙커", "폐쇄된 지하 벙커에서 탈출하세요", "https://example.com/theme12.jpg");

        // 공포의 저택: 최근 7일 안에 12건
        saveReservationFixture("예약자일", baseDate.minusDays(1), time10, horror);
        saveReservationFixture("예약자이", baseDate.minusDays(1), time11, horror);
        saveReservationFixture("예약자삼", baseDate.minusDays(1), time12, horror);
        saveReservationFixture("예약자사", baseDate.minusDays(1), time13, horror);
        saveReservationFixture("예약자오", baseDate.minusDays(1), time14, horror);
        saveReservationFixture("예약자육", baseDate.minusDays(1), time15, horror);
        saveReservationFixture("예약자칠", baseDate.minusDays(1), time16, horror);
        saveReservationFixture("예약자팔", baseDate.minusDays(1), time17, horror);
        saveReservationFixture("예약자구", baseDate.minusDays(1), time18, horror);
        saveReservationFixture("예약자십", baseDate.minusDays(2), time10, horror);
        saveReservationFixture("예약자십일", baseDate.minusDays(2), time11, horror);
        saveReservationFixture("예약자십이", baseDate.minusDays(2), time12, horror);

        // 사라진 연구소: 최근 7일 안에 10건
        saveReservationFixture("예약자일", baseDate.minusDays(1), time10, lab);
        saveReservationFixture("예약자이", baseDate.minusDays(1), time11, lab);
        saveReservationFixture("예약자삼", baseDate.minusDays(1), time12, lab);
        saveReservationFixture("예약자사", baseDate.minusDays(1), time13, lab);
        saveReservationFixture("예약자오", baseDate.minusDays(1), time14, lab);
        saveReservationFixture("예약자육", baseDate.minusDays(1), time15, lab);
        saveReservationFixture("예약자칠", baseDate.minusDays(1), time16, lab);
        saveReservationFixture("예약자팔", baseDate.minusDays(1), time17, lab);
        saveReservationFixture("예약자구", baseDate.minusDays(1), time18, lab);
        saveReservationFixture("예약자십", baseDate.minusDays(2), time10, lab);

        // 시간 여행자: 최근 7일 안에 9건
        saveReservationFixture("예약자일", baseDate.minusDays(1), time10, timeTravel);
        saveReservationFixture("예약자이", baseDate.minusDays(1), time11, timeTravel);
        saveReservationFixture("예약자삼", baseDate.minusDays(1), time12, timeTravel);
        saveReservationFixture("예약자사", baseDate.minusDays(1), time13, timeTravel);
        saveReservationFixture("예약자오", baseDate.minusDays(1), time14, timeTravel);
        saveReservationFixture("예약자육", baseDate.minusDays(1), time15, timeTravel);
        saveReservationFixture("예약자칠", baseDate.minusDays(1), time16, timeTravel);
        saveReservationFixture("예약자팔", baseDate.minusDays(1), time17, timeTravel);
        saveReservationFixture("예약자구", baseDate.minusDays(1), time18, timeTravel);

        // 감옥 탈출: 최근 7일 안에 8건
        saveReservationFixture("예약자일", baseDate.minusDays(1), time10, prison);
        saveReservationFixture("예약자이", baseDate.minusDays(1), time11, prison);
        saveReservationFixture("예약자삼", baseDate.minusDays(1), time12, prison);
        saveReservationFixture("예약자사", baseDate.minusDays(1), time13, prison);
        saveReservationFixture("예약자오", baseDate.minusDays(1), time14, prison);
        saveReservationFixture("예약자육", baseDate.minusDays(1), time15, prison);
        saveReservationFixture("예약자칠", baseDate.minusDays(1), time16, prison);
        saveReservationFixture("예약자팔", baseDate.minusDays(1), time17, prison);

        // 마법사의 방: 최근 7일 안에 7건
        saveReservationFixture("예약자일", baseDate.minusDays(1), time10, wizard);
        saveReservationFixture("예약자이", baseDate.minusDays(1), time11, wizard);
        saveReservationFixture("예약자삼", baseDate.minusDays(1), time12, wizard);
        saveReservationFixture("예약자사", baseDate.minusDays(1), time13, wizard);
        saveReservationFixture("예약자오", baseDate.minusDays(1), time14, wizard);
        saveReservationFixture("예약자육", baseDate.minusDays(1), time15, wizard);
        saveReservationFixture("예약자칠", baseDate.minusDays(1), time16, wizard);

        // 좀비 바이러스: 최근 7일 안에 6건
        saveReservationFixture("예약자일", baseDate.minusDays(1), time10, zombie);
        saveReservationFixture("예약자이", baseDate.minusDays(1), time11, zombie);
        saveReservationFixture("예약자삼", baseDate.minusDays(1), time12, zombie);
        saveReservationFixture("예약자사", baseDate.minusDays(1), time13, zombie);
        saveReservationFixture("예약자오", baseDate.minusDays(1), time14, zombie);
        saveReservationFixture("예약자육", baseDate.minusDays(1), time15, zombie);

        // 해적의 보물: 최근 7일 안에 5건
        saveReservationFixture("예약자일", baseDate.minusDays(1), time10, pirate);
        saveReservationFixture("예약자이", baseDate.minusDays(1), time11, pirate);
        saveReservationFixture("예약자삼", baseDate.minusDays(1), time12, pirate);
        saveReservationFixture("예약자사", baseDate.minusDays(1), time13, pirate);
        saveReservationFixture("예약자오", baseDate.minusDays(1), time14, pirate);

        // 스파이 미션: 최근 7일 안에 4건
        saveReservationFixture("예약자일", baseDate.minusDays(1), time10, spy);
        saveReservationFixture("예약자이", baseDate.minusDays(1), time11, spy);
        saveReservationFixture("예약자삼", baseDate.minusDays(1), time12, spy);
        saveReservationFixture("예약자사", baseDate.minusDays(1), time13, spy);

        // 우주 정거장: 최근 7일 안에 3건
        saveReservationFixture("예약자일", baseDate.minusDays(1), time10, space);
        saveReservationFixture("예약자이", baseDate.minusDays(1), time11, space);
        saveReservationFixture("예약자삼", baseDate.minusDays(1), time12, space);

        // 고대 유적: 최근 7일 안에 2건
        saveReservationFixture("예약자일", baseDate.minusDays(1), time10, ancient);
        saveReservationFixture("예약자이", baseDate.minusDays(1), time11, ancient);

        // 지하 벙커: 예약은 많지만 최근 7일 밖 데이터라 랭킹에 반영되면 안 됨
        saveReservationFixture("오래된예약자일", baseDate.minusDays(8), time10, bunker);
        saveReservationFixture("오래된예약자이", baseDate.minusDays(8), time11, bunker);
        saveReservationFixture("오래된예약자삼", baseDate.minusDays(8), time12, bunker);
        saveReservationFixture("오래된예약자사", baseDate.minusDays(8), time13, bunker);
        saveReservationFixture("오래된예약자오", baseDate.minusDays(8), time14, bunker);
        saveReservationFixture("오래된예약자육", baseDate.minusDays(8), time15, bunker);
        saveReservationFixture("오래된예약자칠", baseDate.minusDays(8), time16, bunker);
        saveReservationFixture("오래된예약자팔", baseDate.minusDays(8), time17, bunker);
        saveReservationFixture("오래된예약자구", baseDate.minusDays(8), time18, bunker);

        // 기준일 이후 예약도 최근 7일 랭킹에 반영되면 안 됨
        saveReservationFixture("미래예약자일", baseDate.plusDays(1), time10, bunker);
        saveReservationFixture("미래예약자이", baseDate.plusDays(2), time11, bunker);
        saveReservationFixture("미래예약자삼", baseDate.plusDays(2), time12, bunker);
    }

    private long createTime(String startAt) {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", startAt);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private long createTheme(String name, String description, String thumbnail) {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("description", description);
        params.put("thumbnail", thumbnail);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private void saveReservationFixture(String name, LocalDate date, long timeId, long themeId) {
        String sql = """
                INSERT INTO reservation (name, date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """;

        jdbcTemplate.update(sql, name, date, timeId, themeId);
    }
}
