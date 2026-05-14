export type GradeColor = { label: string; color: string; bg: string; description: string };

export const buyGradeTheme: Record<string, GradeColor> = {
  S: { label: 'S Strong candidate', color: '#FFFFFF', bg: '#0B6B3A', description: 'High quality with wide margin of safety' },
  A: { label: 'A Candidate', color: '#FFFFFF', bg: '#2D9C5A', description: 'Good quality and fair valuation' },
  B: { label: 'B Watch', color: '#FFFFFF', bg: '#2F80ED', description: 'Good quality, wait for better price' },
  C: { label: 'C Interest', color: '#1F2933', bg: '#F2C94C', description: 'Some requirements missing' },
  D: { label: 'D Hold off', color: '#FFFFFF', bg: '#F2994A', description: 'Core requirements weak' },
  EXCLUDE: { label: 'Exclude', color: '#FFFFFF', bg: '#828282', description: 'Not suitable for long-term value screen' }
};

export const sellGradeTheme: Record<string, GradeColor> = {
  HOLD: { label: 'Hold', color: '#FFFFFF', bg: '#2D9C5A', description: 'No major warning' },
  WATCH: { label: 'Watch', color: '#1F2933', bg: '#F2C94C', description: 'Needs monitoring' },
  REVIEW_SELL: { label: 'Review', color: '#FFFFFF', bg: '#F2994A', description: 'Review the thesis' },
  SELL: { label: 'Exit review', color: '#FFFFFF', bg: '#D33F2F', description: 'Major damage signal' }
};
