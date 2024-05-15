package roomescape.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.fixture.ThemeFixture;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ThemeRepositoryTest {
    @Autowired
    ThemeRepository sut;

    @BeforeEach
    void setup(){
        sut.deleteAll();
    }

    @Test
    void create() {
        final var result = sut.save(ThemeFixture.getDomain());
        assertThat(result).isNotNull();
    }

    @Test
    void findById() {
        final var id = sut.save(ThemeFixture.getDomain())
                .getId();
        final var result = sut.findById(id);
        assertThat(result).contains(ThemeFixture.getDomain());
    }
    @Test
    void delete(){
        final var theme = sut.save(ThemeFixture.getDomain());
        sut.delete(theme);
        final var result = sut.findById(theme.getId());
        assertThat(result).isNotPresent();
    }
    @Test
    void getAll(){
        sut.save(ThemeFixture.getDomain());
        sut.save(ThemeFixture.getDomain());

        final var result = sut.findAll();
        assertThat(result).hasSize(2);
    }
}
