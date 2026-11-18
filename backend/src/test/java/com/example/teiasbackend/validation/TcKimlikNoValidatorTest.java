package com.example.teiasbackend.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TcKimlikNoValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {"10000000078", "10000001440", "11111111110", "12345678950"})
    void gecerliNumaralariKabulEder(String tc) {
        assertThat(TcKimlikNoValidator.gecerliMi(tc)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "1234567890", "123456789012", "01234567890", "12345678901", "1234567895a", "11111111111"})
    void gecersizNumaralariReddeder(String tc) {
        assertThat(TcKimlikNoValidator.gecerliMi(tc)).isFalse();
    }

    @Test
    void nullDegerAnotasyondaGecerliSayilir() {
        assertThat(new TcKimlikNoValidator().isValid(null, null)).isTrue();
        assertThat(TcKimlikNoValidator.gecerliMi(null)).isFalse();
    }
}
