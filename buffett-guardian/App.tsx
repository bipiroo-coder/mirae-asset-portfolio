import React, { useMemo } from 'react';
import { SafeAreaView, ScrollView, Text, View } from 'react-native';
import { companies } from './src/data/sampleData';
import { scoreCompany, sellGrade } from './src/engine/scoring';
import { buyGradeTheme, sellGradeTheme } from './src/theme/grades';

function Badge({ text, bg, color }: { text: string; bg: string; color: string }) {
  return <View style={{ backgroundColor: bg, borderRadius: 999, paddingHorizontal: 10, paddingVertical: 6 }}><Text style={{ color, fontWeight: '800' }}>{text}</Text></View>;
}

export default function App() {
  const ranked = useMemo(() => [...companies].sort((a, b) => scoreCompany(b).total - scoreCompany(a).total), []);
  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: '#F6F3EC' }}>
      <ScrollView style={{ padding: 16 }}>
        <Text style={{ fontSize: 30, fontWeight: '900', color: '#1D2A22' }}>Buffett Guardian</Text>
        <Text style={{ marginTop: 6, marginBottom: 18, color: '#59645B' }}>Color coded long term value screen</Text>
        {ranked.map((company) => {
          const score = scoreCompany(company);
          const risk = sellGrade(company);
          const buyTheme = buyGradeTheme[score.buyGrade];
          const riskTheme = sellGradeTheme[risk];
          return (
            <View key={company.ticker} style={{ backgroundColor: '#FFFFFF', borderRadius: 20, padding: 16, marginBottom: 12 }}>
              <Text style={{ fontSize: 18, fontWeight: '900', color: '#1D2A22' }}>{company.name}</Text>
              <Text style={{ marginTop: 3, color: '#69756C' }}>{company.ticker} · {company.market} · {company.sector}</Text>
              <View style={{ flexDirection: 'row', gap: 8, marginTop: 12, flexWrap: 'wrap' }}>
                <Badge text={buyTheme.label} bg={buyTheme.bg} color={buyTheme.color} />
                <Badge text={riskTheme.label} bg={riskTheme.bg} color={riskTheme.color} />
                <Badge text={score.total + ' pts'} bg={'#EEF2EA'} color={'#1D2A22'} />
              </View>
              <Text style={{ marginTop: 12, color: '#3F4B42' }}>Quality {score.quality} · Moat {score.moat} · Valuation {score.valuation}</Text>
              <Text style={{ marginTop: 4, color: '#3F4B42' }}>Margin of safety {(score.marginOfSafety * 100).toFixed(1)}%</Text>
            </View>
          );
        })}
      </ScrollView>
    </SafeAreaView>
  );
}
