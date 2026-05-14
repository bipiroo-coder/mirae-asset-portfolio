import { BuyGrade, Company, ScoreBreakdown, SellGrade, Signal } from './types';

const clamp = (value: number, min: number, max: number) => Math.max(min, Math.min(max, value));
const avg = (values: number[]) => values.reduce((a, b) => a + b, 0) / values.length;

function scoreQuality(c: Company): number {
  return clamp(c.roe / 20, 0, 1) * 25 +
    clamp(c.roic / 20, 0, 1) * 25 +
    clamp(c.operatingMargin / 30, 0, 1) * 15 +
    clamp(c.marginStability / 100, 0, 1) * 15 +
    clamp((c.revenueCagr5y + c.epsCagr5y) / 20, 0, 1) * 10 +
    clamp(c.fcfConversion / 100, 0, 1) * 10;
}

function scoreMoat(c: Company): number {
  return avg([c.moat.brand, c.moat.switchingCost, c.moat.scale, c.moat.networkEffect, c.moat.regulation, c.moat.costAdvantage]) * 10;
}

function scoreBalance(c: Company): number {
  const debtScore = c.netDebtToEbitda <= 0 ? 35 : clamp((4 - c.netDebtToEbitda) / 4, 0, 1) * 35;
  const leverageScore = clamp((180 - c.debtToEquity) / 180, 0, 1) * 25;
  const coverageScore = clamp(c.interestCoverage / 15, 0, 1) * 25;
  const fcfYearsScore = clamp(c.fcfPositiveYears / 10, 0, 1) * 15;
  return debtScore + leverageScore + coverageScore + fcfYearsScore;
}

function scoreShareholder(c: Company): number {
  const dividendScore = clamp(c.dividendYield / 5, 0, 1) * 30;
  const payoutScore = c.payoutRatio > 85 ? 5 : clamp((85 - Math.abs(c.payoutRatio - 45)) / 85, 0, 1) * 30;
  const buybackScore = clamp(c.buybackYield / 4, 0, 1) * 25;
  const ownerYieldScore = clamp(c.ownerEarningsYield / 9, 0, 1) * 15;
  return dividendScore + payoutScore + buybackScore + ownerYieldScore;
}

function scoreValuation(c: Company): { score: number; marginOfSafety: number } {
  const marginOfSafety = (c.fairValue - c.price) / c.fairValue;
  const safetyScore = clamp(marginOfSafety / 0.35, 0, 1) * 60;
  const ownerYieldScore = clamp(c.ownerEarningsYield / 8, 0, 1) * 40;
  return { score: safetyScore + ownerYieldScore, marginOfSafety };
}

function scoreForeignLens(c: Company): number {
  const base = avg([
    c.buffettLens.understandableBusiness,
    c.buffettLens.managementTrust,
    c.buffettLens.longTermCapitalAllocation,
    c.buffettLens.foreignShareholderFriendliness,
    c.buffettLens.localCurrencyCashflowMatch,
  ]) * 10;
  const fxPenalty = c.market === 'KR' ? 0 : (10 - c.buffettLens.fxRisk) * 1.8;
  const shareholderBonus = c.dividendYield >= 2.5 && c.payoutRatio <= 65 ? 3 : 0;
  const patientCapitalBonus = c.ownerEarningsYield >= 7 ? 4 : 0;
  return clamp(base - fxPenalty + shareholderBonus + patientCapitalBonus, 0, 100);
}

function gradeBy(total: number, marginOfSafety: number): BuyGrade {
  if (total >= 88 && marginOfSafety >= 0.3) return 'S';
  if (total >= 80 && marginOfSafety >= 0.2) return 'A';
  if (total >= 72 && marginOfSafety >= 0.1) return 'B';
  if (total >= 65) return 'C';
  if (total >= 55) return 'D';
  return 'EXCLUDE';
}

export function scoreCompany(c: Company): ScoreBreakdown {
  const quality = scoreQuality(c);
  const moat = scoreMoat(c);
  const balance = scoreBalance(c);
  const shareholder = scoreShareholder(c);
  const valuation = scoreValuation(c);
  const buffettForeignLens = scoreForeignLens(c);
  const total = quality * 0.25 + moat * 0.2 + balance * 0.15 + shareholder * 0.15 + valuation.score * 0.2 + buffettForeignLens * 0.05;
  return {
    total: Math.round(total),
    quality: Math.round(quality),
    moat: Math.round(moat),
    balance: Math.round(balance),
    shareholder: Math.round(shareholder),
    valuation: Math.round(valuation.score),
    buffettForeignLens: Math.round(buffettForeignLens),
    marginOfSafety: valuation.marginOfSafety,
    buyGrade: gradeBy(total, valuation.marginOfSafety),
  };
}

export function detectSignals(c: Company): Signal[] {
  const q = c.lastQuarter;
  const signals: Signal[] = [];
  if (q.thesisBroken) signals.push({ ticker: c.ticker, title: 'Thesis changed', reason: 'Original long-term case needs review.', severity: 'critical' });
  if (q.accountingIssue) signals.push({ ticker: c.ticker, title: 'Reporting concern', reason: 'Data reliability needs review.', severity: 'critical' });
  if (q.managementIssue) signals.push({ ticker: c.ticker, title: 'Management concern', reason: 'Capital allocation or governance needs review.', severity: 'warning' });
  if (q.opProfitYoY <= -20 || q.marginChangePp <= -3) signals.push({ ticker: c.ticker, title: 'Profitability deterioration', reason: `Operating profit YoY ${q.opProfitYoY}% and margin change ${q.marginChangePp}pp.`, severity: q.opProfitYoY <= -35 ? 'critical' : 'warning' });
  if (q.fcfYoY <= -30 || c.fcfPositiveYears < 5) signals.push({ ticker: c.ticker, title: 'Cash generation weaker', reason: 'Owner earnings stability is weaker.', severity: q.fcfYoY <= -50 ? 'critical' : 'warning' });
  if (c.netDebtToEbitda >= 3 || q.debtYoY >= 30 || c.interestCoverage < 4) signals.push({ ticker: c.ticker, title: 'Leverage risk', reason: `Net debt to EBITDA ${c.netDebtToEbitda}, interest coverage ${c.interestCoverage}.`, severity: c.netDebtToEbitda >= 4 || c.interestCoverage < 2.5 ? 'critical' : 'warning' });
  if (q.dividendCoverage < 1.1 && c.dividendYield >= 3) signals.push({ ticker: c.ticker, title: 'Distribution coverage risk', reason: 'Income payout may not be well covered.', severity: 'warning' });
  return signals;
}

export function sellGrade(c: Company): SellGrade {
  const signals = detectSignals(c);
  const critical = signals.filter((s) => s.severity === 'critical').length;
  const warnings = signals.filter((s) => s.severity === 'warning').length;
  const score = scoreCompany(c);
  if (critical >= 1 || score.total < 55) return 'SELL';
  if (warnings >= 2 || score.total < 65) return 'REVIEW_SELL';
  if (warnings === 1 || score.total < 72) return 'WATCH';
  return 'HOLD';
}
