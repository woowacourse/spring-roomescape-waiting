package roomescape.registration.domain.waiting.domain;

public record WaitingWithRank(Waiting waiting, Long rank) {

    // todo: jpql 알수없는 컴파일 에러땜에 만듬. 나중에 해당 dto 인터페이스로 바꾸기
    public WaitingWithRank(Waiting waiting, int rank) {
        this(waiting, (long) rank);
    }
}
