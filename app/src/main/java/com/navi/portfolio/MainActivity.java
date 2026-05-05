package com.navi.portfolio;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import java.text.DecimalFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final ArrayList<Holding> holdings = new ArrayList<>();
    private final DecimalFormat won = new DecimalFormat("#,###");

    static class Holding {
        String account, code, name;
        double qty, buy, now;
        Holding(String a, String c, String n, double q, double b, double p) {
            account=a; code=c; name=n; qty=q; buy=b; now=p;
        }
        double cost(){ return qty * buy; }
        double value(){ return qty * now; }
        double pnl(){ return value() - cost(); }
        double rate(){ return cost()==0 ? 0 : pnl()/cost()*100.0; }
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        holdings.add(new Holding("ISA", "005930.KS", "삼성전자", 10, 70000, 72500));
        holdings.add(new Holding("연금", "035420.KS", "NAVER", 3, 180000, 174000));
        holdings.add(new Holding("해외", "AAPL", "Apple", 2, 180, 192));
        build();
    }

    private void build() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 28, 28, 28);
        root.setBackgroundColor(Color.rgb(247,249,252));
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("전체 자산 포트폴리오");
        title.setTextSize(25);
        title.setTextColor(Color.rgb(30,30,30));
        title.setTypeface(null, 1);
        root.addView(title);

        TextView desc = new TextView(this);
        desc.setText("미래에셋 보유자산을 기준으로 계좌별 손익을 확인하는 앱입니다. 손실은 파란색, 이익은 빨간색으로 표시합니다.");
        desc.setTextSize(14);
        desc.setPadding(0, 12, 0, 20);
        root.addView(desc);

        double cost=0, value=0;
        for(Holding h: holdings){ cost += h.cost(); value += h.value(); }
        double pnl = value - cost;
        addCard(root, "전체 평가금액", won.format(value) + "원", Color.rgb(30,30,30));
        addCard(root, "전체 매입금액", won.format(cost) + "원", Color.rgb(30,30,30));
        addCard(root, "전체 손익", (pnl>=0?"+":"") + won.format(pnl) + "원", pnl>=0?Color.rgb(211,47,47):Color.rgb(25,118,210));
        addCard(root, "전체 수익률", String.format(Locale.KOREA, "%+.2f%%", cost==0?0:pnl/cost*100), pnl>=0?Color.rgb(211,47,47):Color.rgb(25,118,210));

        TextView chart = new TextView(this);
        chart.setText("\n자산 비중 / 손익 다이어그램 영역\n\n빨강 = 이익\n파랑 = 손실\n");
        chart.setGravity(Gravity.CENTER);
        chart.setTextSize(18);
        chart.setBackgroundColor(Color.WHITE);
        root.addView(chart, new LinearLayout.LayoutParams(-1, 260));

        TextView listTitle = new TextView(this);
        listTitle.setText("\n보유 종목");
        listTitle.setTextSize(20);
        listTitle.setTypeface(null, 1);
        root.addView(listTitle);

        TableLayout table = new TableLayout(this);
        table.setStretchAllColumns(true);
        root.addView(table);
        TableRow head = new TableRow(this);
        addCell(head, "계좌", true, Color.DKGRAY);
        addCell(head, "종목", true, Color.DKGRAY);
        addCell(head, "손익", true, Color.DKGRAY);
        addCell(head, "수익률", true, Color.DKGRAY);
        table.addView(head);
        for(Holding h: holdings){
            TableRow r = new TableRow(this);
            addCell(r, h.account, false, Color.DKGRAY);
            addCell(r, h.name, false, Color.DKGRAY);
            int c = h.pnl()>=0?Color.rgb(211,47,47):Color.rgb(25,118,210);
            addCell(r, (h.pnl()>=0?"+":"") + won.format(h.pnl()), false, c);
            addCell(r, String.format(Locale.KOREA, "%+.2f%%", h.rate()), false, c);
            table.addView(r);
        }

        TextView bottom = new TextView(this);
        bottom.setText("\n다음 업데이트 예정: CSV 가져오기, 종목 추가, 현재가 조회, 실제 차트 그리기 기능.");
        bottom.setTextSize(13);
        root.addView(bottom);
        setContentView(scroll);
    }

    private void addCard(LinearLayout root, String label, String value, int color){
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(24,18,24,18);
        card.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 8, 0, 8);
        root.addView(card, lp);
        TextView l = new TextView(this); l.setText(label); l.setTextSize(13); card.addView(l);
        TextView v = new TextView(this); v.setText(value); v.setTextSize(23); v.setTypeface(null,1); v.setTextColor(color); card.addView(v);
    }

    private void addCell(TableRow row, String text, boolean bold, int color){
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setTextColor(color);
        tv.setPadding(8, 12, 8, 12);
        if(bold) tv.setTypeface(null, 1);
        row.addView(tv);
    }
}
