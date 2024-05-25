package roomescape.registration.domain.waiting.domain;

// todo rank: 같은 테마, 날짜, 시간의 예약 대기 중 내 예약 대기 보다 빨리 생성된 갯수를 함께 응답함
public record WaitingWithRank(Waiting waiting, Long rank) {

    // todo: jpql 알수없는 컴파일 에러땜에 만듬. 나중에 해당 dto 인터페이스로 바꾸기
    public WaitingWithRank(Waiting waiting, int rank) {
        this(waiting, (long) rank);
    }
}
