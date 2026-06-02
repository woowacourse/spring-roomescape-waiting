package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StoreTest {

    @Test
    @DisplayName("id가 같으면 이름이 달라도 동등하다")
    void equalWhenSameId() {
        Store store = new Store(1L, "매장");
        Store sameId = new Store(1L, "다른매장");

        assertThat(store).isEqualTo(sameId);
    }

    @Test
    @DisplayName("id가 다르면 동등하지 않다")
    void notEqualWhenDifferentId() {
        Store store = new Store(1L, "매장");
        Store differentId = new Store(2L, "매장");

        assertThat(store).isNotEqualTo(differentId);
    }
}
