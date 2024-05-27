package roomescape.common.config;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.auth.domain.Role;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;

public class DataFixture {

    // member
    public static final Member MEMBER_JOJO = new Member(null, Role.MEMBER, new MemberName("조조"), "jojo@email.com", "1234");
    public static final Member ADMIN = new Member(null, Role.ADMIN, new MemberName("어드민"), "admin@email.com", "1234");
    public static final Member MEMBER_SOLAR = new Member(null, Role.MEMBER, new MemberName("솔라"), "solar@email.com", "1234");
    public static final Member MEMBER_BROWN = new Member(null, Role.MEMBER, new MemberName("브라운"), "brown@email.com", "1234");
    public static final Member MEMBER_NEO = new Member(null, Role.MEMBER, new MemberName("네오"), "neo@email.com", "1234");
    public static final Member MEMBER_BRE = new Member(null, Role.MEMBER, new MemberName("브리"), "bre@email.com", "1234");
    public static final Member MEMBER_POBI = new Member(null, Role.MEMBER, new MemberName("포비"), "pobi@email.com", "1234");
    public static final Member MEMBER_GOOGOO = new Member(null, Role.MEMBER, new MemberName("구구"), "googoo@email.com", "1234");
    public static final Member MEMBER_TOMI = new Member(null, Role.MEMBER, new MemberName("토미"), "tomi@email.com", "1234");
    public static final Member MEMBER_LISA = new Member(null, Role.MEMBER, new MemberName("리사"), "risa@email.com", "1234");

    // theme
    public static final Theme THEME_HORROR = new Theme(new ThemeName("공포"), new Description("무서워요"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_SF = new Theme(new ThemeName("SF"), new Description("미래"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_MONKEY = new Theme(new ThemeName("원숭이 사원"), new Description("원숭이들의 공격"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_ZOMBIE = new Theme(new ThemeName("나가야 산다"), new Description("빨리 탈출"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_NAGAYA_SANDA = new Theme(new ThemeName("좀비 사태"), new Description("좀비들의 공격"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_HORROR_THEME_PARK = new Theme(new ThemeName("공포의 놀이공원"), new Description("놀이공원 탈출"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_BASEMENT = new Theme(new ThemeName("지하실"), new Description("지하실 탈출"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_TITANIC = new Theme(new ThemeName("타이타닉"), new Description("타이타닉에서 탈출"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_ART_GALLERY = new Theme(new ThemeName("미술관을 털어라"), new Description("미술관을 탈출"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_VIRUS = new Theme(new ThemeName("바이러스"), new Description("바이러스를 막으세요"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_PRISON_BREAK = new Theme(new ThemeName("프리즌 브레이크"), new Description("감옥을 탈출"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_AZTEC_TEMPLE = new Theme(new ThemeName("아즈텍 신전"), new Description("신전을 탈출"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_SPACE_STATION = new Theme(new ThemeName("우주 정거장"), new Description("우주 정거장을 탈출"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_DENTIST = new Theme(new ThemeName("치과의사"), new Description("치과의사를 피해 탈출"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    public static final Theme THEME_SECRET_AGENT = new Theme(new ThemeName("비밀요원"), new Description("비밀요원이 돼 탈출"), "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");

    // reservationTime
    public static final ReservationTime TIME_10_00 = new ReservationTime(LocalTime.parse("10:00"));
    public static final ReservationTime TIME_10_30 = new ReservationTime(LocalTime.parse("10:30"));
    public static final ReservationTime TIME_11_00 = new ReservationTime(LocalTime.parse("11:00"));
    public static final ReservationTime TIME_11_30 = new ReservationTime(LocalTime.parse("11:30"));
    public static final ReservationTime TIME_12_00 = new ReservationTime(LocalTime.parse("12:00"));
    public static final ReservationTime TIME_12_30 = new ReservationTime(LocalTime.parse("12:30"));
    public static final ReservationTime TIME_13_00 = new ReservationTime(LocalTime.parse("13:00"));
    public static final ReservationTime TIME_13_30 = new ReservationTime(LocalTime.parse("13:30"));
    public static final ReservationTime TIME_14_00 = new ReservationTime(LocalTime.parse("14:00"));
    public static final ReservationTime TIME_14_30 = new ReservationTime(LocalTime.parse("14:30"));

    // LocalTime
    public static final LocalDate TODAY = LocalDate.now();
    public static final LocalDate TOMORROW = TODAY.plusDays(1);

    private DataFixture() {
    }
}
