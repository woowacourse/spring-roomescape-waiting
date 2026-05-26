package roomescape.common;

public record Pageable(Integer page, Integer size) {

    public int offset() {
        return (page - 1) * size;
    }
}
