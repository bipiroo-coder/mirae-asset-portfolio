import { Currency } from '../engine/types';

export function formatCurrency(value: number, currency: Currency): string {
  if (currency === 'KRW') return Math.round(value).toLocaleString('ko-KR') + ' KRW';
  if (currency === 'JPY') return 'JPY ' + Math.round(value).toLocaleString('en-US');
  return 'USD ' + value.toLocaleString('en-US');
}

export function formatPercent(value: number, digits = 1): string {
  return value.toFixed(digits) + '%';
}
