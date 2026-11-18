package com.example.teiasbackend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Resmi T.C. Kimlik No doğrulama algoritması:
 * <ul>
 *     <li>11 haneli, tamamı rakam, ilk hane 0 olamaz</li>
 *     <li>10. hane = ((1+3+5+7+9. hanelerin toplamı) * 7 - (2+4+6+8. hanelerin toplamı)) mod 10</li>
 *     <li>11. hane = (ilk 10 hanenin toplamı) mod 10</li>
 * </ul>
 */
public class TcKimlikNoValidator implements ConstraintValidator<TcKimlikNo, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || gecerliMi(value);
    }

    public static boolean gecerliMi(String tc) {
        if (tc == null || !tc.matches("\\d{11}") || tc.charAt(0) == '0') {
            return false;
        }
        int[] d = new int[11];
        for (int i = 0; i < 11; i++) {
            d[i] = tc.charAt(i) - '0';
        }
        int tekler = d[0] + d[2] + d[4] + d[6] + d[8];
        int ciftler = d[1] + d[3] + d[5] + d[7];
        int onuncu = ((tekler * 7 - ciftler) % 10 + 10) % 10;
        if (onuncu != d[9]) {
            return false;
        }
        int ilkOnToplam = 0;
        for (int i = 0; i < 10; i++) {
            ilkOnToplam += d[i];
        }
        return ilkOnToplam % 10 == d[10];
    }
}
