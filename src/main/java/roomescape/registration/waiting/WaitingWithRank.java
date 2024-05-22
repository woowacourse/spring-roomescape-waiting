package roomescape.registration.waiting;

// todo rank: 같은 테마, 날짜, 시간의 예약 대기 중 내 예약 대기 보다 빨리 생성된 갯수를 함께 응답함
public record WaitingWithRank(Waiting waiting, Long rank) {
}
