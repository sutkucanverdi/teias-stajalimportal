import { describe, expect, it } from 'vitest';
import { dosyaBoyutu, tcKimlikGecerliMi } from './yardimcilar';

describe('tcKimlikGecerliMi', () => {
  it('geçerli numaraları kabul eder', () => {
    expect(tcKimlikGecerliMi('10000000078')).toBe(true);
    expect(tcKimlikGecerliMi('10000001440')).toBe(true);
    expect(tcKimlikGecerliMi('12345678950')).toBe(true);
  });

  it('geçersiz numaraları reddeder', () => {
    expect(tcKimlikGecerliMi('')).toBe(false);
    expect(tcKimlikGecerliMi('01234567890')).toBe(false);
    expect(tcKimlikGecerliMi('12345678901')).toBe(false);
    expect(tcKimlikGecerliMi('1234567895')).toBe(false);
    expect(tcKimlikGecerliMi(null)).toBe(false);
  });
});

describe('dosyaBoyutu', () => {
  it('okunabilir boyut üretir', () => {
    expect(dosyaBoyutu(500)).toBe('500 B');
    expect(dosyaBoyutu(2048)).toBe('2 KB');
    expect(dosyaBoyutu(3 * 1024 * 1024)).toBe('3.0 MB');
  });
});
