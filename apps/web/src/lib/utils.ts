import {type ClassValue, clsx} from "clsx";
import {twMerge} from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

const CURRENCY_SYMBOLS: Record<string, string> = {
  EUR: '€',
  USD: '$',
  GBP: '£',
};

export function formatCurrency(amountCents: number, currency: string): string {
  const symbol = CURRENCY_SYMBOLS[currency] || currency;
  const amount = (amountCents / 100).toFixed(2);

  if (currency === 'EUR') {
    return `${amount} ${symbol}`;
  }
  return `${symbol}${amount}`;
}

