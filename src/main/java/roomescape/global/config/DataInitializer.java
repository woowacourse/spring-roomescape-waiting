package roomescape.global.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Profile("!test")
@Component
public class DataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Clear existing data
        jdbcTemplate.execute("DELETE FROM reservation_waiting");
        jdbcTemplate.execute("DELETE FROM reservation");
        jdbcTemplate.execute("DELETE FROM reservation_time");
        jdbcTemplate.execute("DELETE FROM theme");

        // Reset auto-increment
        jdbcTemplate.execute("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");

        // Insert reservation_time
        String[] times = {
            "10:00:00", "11:00:00", "12:00:00", "13:00:00", "14:00:00",
            "15:00:00", "16:00:00", "17:00:00", "18:00:00"
        };
        for (String time : times) {
            jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", time);
        }

        // Insert theme
        Object[][] themes = {
            {"우주선 탈출", "고장 난 우주선에서 제한 시간 안에 탈출하세요.", "https://example.com/themes/space-escape.jpg"},
            {"좀비 아포칼립스", "봉쇄된 도시에서 생존 키트를 찾아 탈출해야 합니다.", "https://example.com/themes/zombie-apocalypse.jpg"},
            {"고대 피라미드", "피라미드 깊숙한 곳의 비밀 방을 열어 보물을 찾으세요.", "https://example.com/themes/pyramid.jpg"},
            {"마법학교의 비밀", "사라진 마법서를 찾아 학교의 저주를 풀어야 합니다.", "https://example.com/themes/magic-school.jpg"},
            {"해적선의 보물", "해적선 선장의 단서를 모아 숨겨진 보물창고를 여세요.", "https://example.com/themes/pirate-treasure.jpg"},
            {"미스터리 연구소", "폐쇄된 연구소에서 실험 기록을 복구하고 탈출하세요.", "https://example.com/themes/lab-mystery.jpg"},
            {"시간여행자", "뒤틀린 시간 장치를 복구해 현재로 돌아오세요.", "https://example.com/themes/time-traveler.jpg"},
            {"유령의 저택", "밤이 끝나기 전 저택의 원혼을 달래는 의식을 완성하세요.", "https://example.com/themes/haunted-mansion.jpg"},
            {"사라진 화가의 작품", "실종된 화가가 남긴 암호를 풀어 진짜 작품을 찾으세요.", "https://example.com/themes/missing-painting.jpg"},
            {"심해 탐험", "산소가 떨어지기 전에 심해 기지의 전원을 복구해야 합니다.", "https://example.com/themes/deep-sea.jpg"},
            {"왕실 음모", "왕궁에서 벌어진 음모의 증거를 찾아 누명을 벗기세요.", "https://example.com/themes/royal-conspiracy.jpg"},
            {"폐병원 탈출", "버려진 병원에서 수상한 흔적을 추적해 출구를 찾으세요.", "https://example.com/themes/abandoned-hospital.jpg"},
            {"한밤중의 서커스", "멈춰버린 서커스 공연의 비밀을 밝히고 무대를 탈출하세요.", "https://example.com/themes/midnight-circus.jpg"},
            {"비밀 요원 작전", "이중 잠금 장치를 해제하고 기밀 문서를 회수하세요.", "https://example.com/themes/secret-agent.jpg"},
            {"드래곤의 동굴", "드래곤이 잠든 사이 고대 룬을 해독해 동굴을 빠져나오세요.", "https://example.com/themes/dragon-cave.jpg"}
        };
        for (Object[] theme : themes) {
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                    theme[0], theme[1], theme[2]);
        }

        // Insert reservation
        Object[][] reservations = {
            {"Minsu Kim", 3, 1, 1},
            {"Soyeon Lee", 3, 2, 2},
            {"Jihoon Park", 3, 3, 3},
            {"Yujin Choi", 3, 4, 4},
            {"Haneul Jung", 3, 5, 5},
            {"Jimin Han", 2, 1, 6},
            {"Sehun Oh", 2, 2, 7},
            {"Areum Yoon", 2, 3, 8},
            {"Doyoon Kang", 2, 4, 9},
            {"Yerin Shin", 2, 5, 10},
            {"Jaehyun Lim", 1, 1, 11},
            {"Nayeon Song", 1, 2, 12},
            {"Hyunwoo Jo", 1, 3, 13},
            {"Sujin Baek", 1, 4, 14},
            {"Jiho Moon", 1, 5, 15},
            {"Daeun Seo", 0, 1, 2},
            {"Minjae Kwon", -1, 2, 4},
            {"Jisu Nam", -2, 3, 6},
            {"Yejun Hong", -3, 4, 8},
            {"Dain Yoo", -3, 5, 10},
            {"Taeyoon Jang", -4, 1, 3},
            {"Seojin Noh", -6, 2, 5},
            {"Siwoo Ryu", -8, 3, 7},
            {"Gaeun Bae", -11, 4, 9},
            {"Hyunseo Ahn", -16, 5, 11},
            {"Mina Koo", -26, 1, 13},
            {"Dohyun Cha", -39, 2, 15}
        };

        for (Object[] res : reservations) {
            String name = (String) res[0];
            int offset = (int) res[1];
            LocalDate targetDate = LocalDate.now().plusDays(offset);
            int timeId = (int) res[2];
            int themeId = (int) res[3];
            LocalDateTime updatedAt = targetDate.atStartOfDay();

            jdbcTemplate.update(
                "INSERT INTO reservation (name, reservation_date, time_id, theme_id, updated_at) VALUES (?, ?, ?, ?, ?)",
                name, java.sql.Date.valueOf(targetDate), timeId, themeId, java.sql.Timestamp.valueOf(updatedAt)
            );
        }

        // Insert reservation_waiting
        Object[][] waitings = {
            {"Waiting Man", 3, 1, 1},
            {"Waiting Woman", 3, 2, 2},
            {"Waiting Boy", 3, 3, 3}
        };

        for (Object[] w : waitings) {
            String name = (String) w[0];
            int offset = (int) w[1];
            LocalDate targetDate = LocalDate.now().plusDays(offset);
            int timeId = (int) w[2];
            int themeId = (int) w[3];
            LocalDateTime updatedAt = targetDate.atStartOfDay();

            jdbcTemplate.update(
                "INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id, updated_at) VALUES (?, ?, ?, ?, ?)",
                name, java.sql.Date.valueOf(targetDate), timeId, themeId, java.sql.Timestamp.valueOf(updatedAt)
            );
        }
    }
}
