package com.example.teiasbackend;

import com.example.teiasbackend.validation.TcKimlikNoValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeiasBackendApplicationTests {

    @Test
    void seedHesaplarinTcKimlikNumaralariGecerli() {
        assertThat(TcKimlikNoValidator.gecerliMi("10000000078")).isTrue();
        assertThat(TcKimlikNoValidator.gecerliMi("10000001440")).isTrue();
    }
}
