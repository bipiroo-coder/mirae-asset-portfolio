import { Company, Holding } from '../engine/types';

const moat = { brand: 7, switchingCost: 6, scale: 8, networkEffect: 3, regulation: 4, costAdvantage: 7 };
const lens = { understandableBusiness: 7, managementTrust: 7, longTermCapitalAllocation: 7, fxRisk: 6, foreignShareholderFriendliness: 7, localCurrencyCashflowMatch: 7 };

export const companies: Company[] = [
  {
    ticker: '005930', name: 'Samsung Electronics', market: 'KR', sector: 'Semiconductors', currency: 'KRW', price: 76000, fairValue: 92000,
    roe: 9.8, roic: 8.1, operatingMargin: 12.5, marginStability: 60, revenueCagr5y: 4.2, epsCagr5y: 2.6, fcfConversion: 62, fcfPositiveYears: 8,
    debtToEquity: 34, netDebtToEbitda: 0, interestCoverage: 40, dividendYield: 2.0, payoutRatio: 35, buybackYield: 0.5, ownerEarningsYield: 5.8,
    moat: { ...moat, scale: 9 }, buffettLens: { ...lens, fxRisk: 8 },
    lastQuarter: { revenueYoY: 8, opProfitYoY: 28, marginChangePp: 1.8, fcfYoY: 16, debtYoY: 3, dividendCoverage: 2.5, accountingIssue: false, managementIssue: false, thesisBroken: false }
  },
  {
    ticker: '000810', name: 'Samsung Fire', market: 'KR', sector: 'Insurance', currency: 'KRW', price: 355000, fairValue: 465000,
    roe: 14.5, roic: 12.2, operatingMargin: 16.8, marginStability: 82, revenueCagr5y: 5.5, epsCagr5y: 8.3, fcfConversion: 86, fcfPositiveYears: 10,
    debtToEquity: 42, netDebtToEbitda: 0.4, interestCoverage: 28, dividendYield: 4.6, payoutRatio: 38, buybackYield: 0.3, ownerEarningsYield: 8.2,
    moat: { ...moat, brand: 8, regulation: 7 }, buffettLens: { ...lens, understandableBusiness: 8, fxRisk: 8 },
    lastQuarter: { revenueYoY: 5, opProfitYoY: 11, marginChangePp: 0.9, fcfYoY: 8, debtYoY: 2, dividendCoverage: 2.2, accountingIssue: false, managementIssue: false, thesisBroken: false }
  },
  {
    ticker: 'AAPL', name: 'Apple', market: 'US', sector: 'Consumer technology', currency: 'USD', price: 190, fairValue: 175,
    roe: 120, roic: 49, operatingMargin: 30, marginStability: 88, revenueCagr5y: 7.1, epsCagr5y: 10.8, fcfConversion: 95, fcfPositiveYears: 10,
    debtToEquity: 140, netDebtToEbitda: 0.5, interestCoverage: 35, dividendYield: 0.5, payoutRatio: 15, buybackYield: 3.2, ownerEarningsYield: 3.4,
    moat: { brand: 10, switchingCost: 9, scale: 9, networkEffect: 8, regulation: 1, costAdvantage: 7 }, buffettLens: { understandableBusiness: 8, managementTrust: 8, longTermCapitalAllocation: 9, fxRisk: 4, foreignShareholderFriendliness: 9, localCurrencyCashflowMatch: 5 },
    lastQuarter: { revenueYoY: 2, opProfitYoY: 1, marginChangePp: -0.5, fcfYoY: 2, debtYoY: 1, dividendCoverage: 6.5, accountingIssue: false, managementIssue: false, thesisBroken: false }
  },
  {
    ticker: 'MITSY', name: 'Mitsubishi Corp ADR', market: 'JP', sector: 'Trading company', currency: 'JPY', price: 20, fairValue: 27,
    roe: 12, roic: 8, operatingMargin: 8.5, marginStability: 72, revenueCagr5y: 6.1, epsCagr5y: 8.8, fcfConversion: 75, fcfPositiveYears: 8,
    debtToEquity: 78, netDebtToEbitda: 1.8, interestCoverage: 9, dividendYield: 3.8, payoutRatio: 32, buybackYield: 1.2, ownerEarningsYield: 8.6,
    moat: { ...moat }, buffettLens: { understandableBusiness: 7, managementTrust: 8, longTermCapitalAllocation: 8, fxRisk: 4, foreignShareholderFriendliness: 8, localCurrencyCashflowMatch: 9 },
    lastQuarter: { revenueYoY: -2, opProfitYoY: 4, marginChangePp: 0.3, fcfYoY: 5, debtYoY: 4, dividendCoverage: 2.4, accountingIssue: false, managementIssue: false, thesisBroken: false }
  }
];

export const holdings: Holding[] = [
  { ticker: '000810', shares: 3, avgPrice: 310000, thesis: 'Brand, scale, regulation and cash distribution profile' },
  { ticker: 'AAPL', shares: 2, avgPrice: 155, thesis: 'Ecosystem quality and capital return history' }
];
