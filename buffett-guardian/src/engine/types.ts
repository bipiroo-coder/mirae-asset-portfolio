export type Market = 'KR' | 'US' | 'JP';
export type Currency = 'KRW' | 'USD' | 'JPY';
export type BuyGrade = 'S' | 'A' | 'B' | 'C' | 'D' | 'EXCLUDE';
export type SellGrade = 'HOLD' | 'WATCH' | 'REVIEW_SELL' | 'SELL';
export type SignalSeverity = 'info' | 'warning' | 'critical';

export type MoatProfile = {
  brand: number;
  switchingCost: number;
  scale: number;
  networkEffect: number;
  regulation: number;
  costAdvantage: number;
};

export type BuffettLens = {
  understandableBusiness: number;
  managementTrust: number;
  longTermCapitalAllocation: number;
  fxRisk: number;
  foreignShareholderFriendliness: number;
  localCurrencyCashflowMatch: number;
};

export type QuarterSnapshot = {
  revenueYoY: number;
  opProfitYoY: number;
  marginChangePp: number;
  fcfYoY: number;
  debtYoY: number;
  dividendCoverage: number;
  accountingIssue: boolean;
  managementIssue: boolean;
  thesisBroken: boolean;
};

export type Company = {
  ticker: string;
  name: string;
  market: Market;
  sector: string;
  currency: Currency;
  price: number;
  fairValue: number;
  roe: number;
  roic: number;
  operatingMargin: number;
  marginStability: number;
  revenueCagr5y: number;
  epsCagr5y: number;
  fcfConversion: number;
  fcfPositiveYears: number;
  debtToEquity: number;
  netDebtToEbitda: number;
  interestCoverage: number;
  dividendYield: number;
  payoutRatio: number;
  buybackYield: number;
  ownerEarningsYield: number;
  moat: MoatProfile;
  buffettLens: BuffettLens;
  lastQuarter: QuarterSnapshot;
};

export type Holding = {
  ticker: string;
  shares: number;
  avgPrice: number;
  thesis: string;
};

export type Signal = {
  ticker: string;
  title: string;
  reason: string;
  severity: SignalSeverity;
};

export type ScoreBreakdown = {
  total: number;
  quality: number;
  moat: number;
  balance: number;
  shareholder: number;
  valuation: number;
  buffettForeignLens: number;
  marginOfSafety: number;
  buyGrade: BuyGrade;
};
