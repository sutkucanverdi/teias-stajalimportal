package com.example.teiasbackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI teiasOpenApi() {
        final String sema = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("TEİAŞ Lise Stajyer Başvuru Portalı API")
                        .version("1.0.0")
                        .description("""
                                Lise öğrencilerinin TEİAŞ birimlerine staj başvurusu yapması, İnsan Kaynakları'nın başvuruları
                                incelemesi ve birim sorumlularının değerlendirmesi için REST API.

                                **Kullanım:** `/api/auth/login` ile token alın, sağ üstteki **Authorize** butonuna token'ı yapıştırın.
                                Varsayılan İK hesabı: `ik_admin` / `teias2026`""")
                        .contact(new Contact().name("TEİAŞ Bilgi Teknolojileri")))
                .addSecurityItem(new SecurityRequirement().addList(sema))
                .components(new Components().addSecuritySchemes(sema,
                        new SecurityScheme().name(sema).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
