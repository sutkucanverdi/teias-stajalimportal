package com.example.teiasbackend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/** T.C. Kimlik Numarasını resmi algoritma ile doğrular. Boş (null) değer geçerli sayılır; zorunluluk için @NotBlank ekleyin. */
@Documented
@Constraint(validatedBy = TcKimlikNoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TcKimlikNo {
    String message() default "Geçerli bir T.C. Kimlik Numarası giriniz.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
