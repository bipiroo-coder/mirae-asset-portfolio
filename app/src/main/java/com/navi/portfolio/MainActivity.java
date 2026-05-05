package com.navi.portfolio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int REQ_IMPORT = 1001;
    private static final int REQ_EXPORT = 1002;
    private static final int PROFIT_RED = Color.rgb(211, 47, 47);
    private static final int LOSS_BLUE = Color.rgb(25, 118, 210);
    private static final int DARK = Color.rgb(33, 33, 33);
    private static final int BG = Color.rgb(247, 249, 252);

    private final ArrayList<Holding> holdings = new ArrayList<>();
    private final DecimalFormat won = new DecimalFormat("#,###");
    private Spinner accountSpinner;
    private LinearLayout root;
    private TextView valueView, costView, pnlView, rateView;
    private PortfolioChartView chartView;
    private TableLayout table;

    static class Holding {
        String account, code, name;
        double qty, buy, now;

        Holding(String account, String code, String name, double qty, double buy, double now) {
            this.account = account;
            this.code = code;
            this.name = name;
            this.qty = qty;
            this.buy = buy;
            this.now = now;
        }

        double cost() { return qty * buy; }
        double value() { return qty * now; }
        double pnl() { return value() - cost(); }
        double rate() { return cost() == 0 ? 0 : pnl() / cost() * 100.0; }
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        loadData();
        if (holdings.isEmpty()) addSampleData();
        buildLayout();
        refreshAccountsAndDashboard();
    }

    private void buildLayout() {
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(14), dp(14), dp(24));
        root.setBackgroundColor(BG);
        scroll.addView(root);
        setContentView(scroll);

        TextView title = text("전체 자산 포트폴리오", 25, DARK, true);
        root.addView(title);

        TextView desc = text("미래에셋 보유자산 CSV 또는 직접 입력으로 계좌별 손익을 확인합니다. 손실은 파란색, 이익은 빨간색입니다.", 13, Color.rgb(80,80,80), false);
        desc.setPadding(0, dp(6), 0, dp(10));
        root.addView(desc);

        accountSpinner = new Spinner(this);
        root.addView(accountSpinner, new LinearLayout.LayoutParams(-1, dp(48)));

        valueView = addCard("평가금액", "-");
        costView = addCard("매입금액", "-");
        pnlView = addCard("손익", "-");
        rateView = addCard("수익률", "-");

        chartView = new PortfolioChartView(this);
        chartView.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams chartLp = new LinearLayout.LayoutParams(-1, dp(390));
        chartLp.setMargins(0, dp(10), 0, dp(10));
        root.addView(chartView, chartLp);

        addButton("미래에셋 CSV 가져오기", v -> importCsv());
        addButton("종목 직접 추가", v -> showAddDialog());
        addButton("CSV 내보내기", v -> exportCsv());
        addButton("전체 초기화", v -> confirmReset());

        TextView guide = text("CSV 형식: 계좌, 종목코드, 종목명, 수량, 평균매입가, 현재가\n미래에셋에서 받은 파일의 열 이름이 조금 달라도 계좌/종목/수량/평균/현재가 단어가 있으면 자동 인식합니다.", 12, Color.rgb(95,95,95), false);
        guide.setPadding(0, dp(10), 0, dp(10));
        root.addView(guide);

        TextView listTitle = text("보유 종목", 20, DARK, true);
        listTitle.setPadding(0, dp(8), 0, dp(4));
        root.addView(listTitle);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        table = new TableLayout(this);
        hsv.addView(table);
        root.addView(hsv);
    }

    private TextView text(String s, int size, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(size);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(null, 1);
        return tv;
    }

    private TextView addCard(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(10), dp(14), dp(10));
        card.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(8), 0, 0);
        root.addView(card, lp);
        TextView l = text(label, 12, Color.rgb(90,90,90), false);
        TextView v = text(value, 23, DARK, true);
        card.addView(l);
        card.addView(v);
        return v;
    }

    private void addButton(String label, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(48));
        lp.setMargins(0, dp(6), 0, 0);
        root.addView(b, lp);
    }

    private void refreshAccountsAndDashboard() {
        String selected = accountSpinner.getSelectedItem() == null ? "전체" : accountSpinner.getSelectedItem().toString();
        Set<String> accounts = new LinkedHashSet<>();
        accounts.add("전체");
        for (Holding h : holdings) accounts.add(cleanAccount(h.account));
        ArrayList<String> list = new ArrayList<>(accounts);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);
        accountSpinner.setAdapter(adapter);
        int idx = list.indexOf(selected);
        accountSpinner.setSelection(idx >= 0 ? idx : 0);
        accountSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) { renderDashboard(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) { renderDashboard(); }
        });
        renderDashboard();
    }

    private List<Holding> filtered() {
        String selected = accountSpinner.getSelectedItem() == null ? "전체" : accountSpinner.getSelectedItem().toString();
        ArrayList<Holding> out = new ArrayList<>();
        for (Holding h : holdings) if ("전체".equals(selected) || selected.equals(cleanAccount(h.account))) out.add(h);
        return out;
    }

    private void renderDashboard() {
        List<Holding> list = filtered();
        double cost = 0, value = 0;
        for (Holding h : list) { cost += h.cost(); value += h.value(); }
        double pnl = value - cost;
        double rate = cost == 0 ? 0 : pnl / cost * 100.0;
        valueView.setText(won.format(value) + "원");
        costView.setText(won.format(cost) + "원");
        pnlView.setText((pnl >= 0 ? "+" : "") + won.format(pnl) + "원");
        pnlView.setTextColor(pnl >= 0 ? PROFIT_RED : LOSS_BLUE);
        rateView.setText(String.format(Locale.KOREA, "%+.2f%%", rate));
        rateView.setTextColor(rate >= 0 ? PROFIT_RED : LOSS_BLUE);
        chartView.setData(list);
        renderTable(list);
    }

    private void renderTable(List<Holding> list) {
        table.removeAllViews();
        TableRow head = new TableRow(this);
        String[] hs = {"계좌", "코드", "종목", "수량", "평단", "현재가", "평가", "손익", "수익률", "삭제"};
        for (String h : hs) addCell(head, h, true, DARK);
        table.addView(head);
        for (Holding h : list) {
            TableRow r = new TableRow(this);
            addCell(r, cleanAccount(h.account), false, DARK);
            addCell(r, h.code, false, DARK);
            addCell(r, h.name, false, DARK);
            addCell(r, trimQty(h.qty), false, DARK);
            addCell(r, won.format(h.buy), false, DARK);
            addCell(r, won.format(h.now), false, DARK);
            addCell(r, won.format(h.value()), false, DARK);
            int c = h.pnl() >= 0 ? PROFIT_RED : LOSS_BLUE;
            addCell(r, (h.pnl() >= 0 ? "+" : "") + won.format(h.pnl()), false, c);
            addCell(r, String.format(Locale.KOREA, "%+.2f%%", h.rate()), false, c);
            Button del = new Button(this);
            del.setText("삭제");
            del.setOnClickListener(v -> { holdings.remove(h); saveData(); refreshAccountsAndDashboard(); });
            r.addView(del);
            table.addView(r);
        }
    }

    private void addCell(TableRow row, String s, boolean bold, int color) {
        TextView tv = text(s == null ? "" : s, bold ? 13 : 12, color, bold);
        tv.setPadding(dp(7), dp(8), dp(7), dp(8));
        tv.setMinWidth(dp(76));
        row.addView(tv);
    }

    private void showAddDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), 0, dp(12), 0);
        EditText account = input("계좌명 예: ISA", false);
        EditText code = input("종목코드 예: 005930.KS", false);
        EditText name = input("종목명", false);
        EditText qty = input("수량", true);
        EditText buy = input("평균매입가", true);
        EditText now = input("현재가", true);
        box.addView(account); box.addView(code); box.addView(name); box.addView(qty); box.addView(buy); box.addView(now);
        new AlertDialog.Builder(this).setTitle("종목 직접 추가").setView(box)
            .setPositiveButton("저장", (d, w) -> {
                String c = val(code, "");
                holdings.add(new Holding(val(account, "기본계좌"), c, val(name, c), num(qty), num(buy), num(now)));
                saveData(); refreshAccountsAndDashboard();
            }).setNegativeButton("취소", null).show();
    }

    private EditText input(String hint, boolean number) {
        EditText e = new EditText(this);
        e.setHint(hint);
        if (number) e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        return e;
    }

    private String val(EditText e, String fallback) {
        String s = e.getText().toString().trim();
        return s.length() == 0 ? fallback : s;
    }

    private void importCsv() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(i, REQ_IMPORT);
    }

    private void exportCsv() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("text/csv");
        i.putExtra(Intent.EXTRA_TITLE, "portfolio.csv");
        startActivityForResult(i, REQ_EXPORT);
    }

    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (res != RESULT_OK || data == null || data.getData() == null) return;
        try {
            if (req == REQ_IMPORT) {
                parseCsv(data.getData());
                saveData(); refreshAccountsAndDashboard();
                Toast.makeText(this, "CSV 가져오기 완료", Toast.LENGTH_LONG).show();
            } else if (req == REQ_EXPORT) {
                OutputStream out = getContentResolver().openOutputStream(data.getData());
                out.write(makeCsv().getBytes("UTF-8"));
                out.close();
                Toast.makeText(this, "CSV 저장 완료", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            if (req == REQ_EXPORT) copyCsv();
            Toast.makeText(this, "처리 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void parseCsv(Uri uri) throws Exception {
        InputStream in = getContentResolver().openInputStream(uri);
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        ArrayList<String[]> rows = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) if (line.trim().length() > 0) rows.add(splitCsv(line));
        br.close();
        if (rows.isEmpty()) return;
        int start = 0, acc = 0, code = 1, name = 2, qty = 3, buy = 4, now = 5;
        if (looksHeader(rows.get(0))) {
            String[] h = rows.get(0);
            acc = find(h, "계좌", "account");
            code = find(h, "종목코드", "코드", "symbol");
            name = find(h, "종목명", "이름", "name");
            qty = find(h, "수량", "보유", "qty", "quantity");
            buy = find(h, "평균", "평단", "매입", "buy", "avg");
            now = find(h, "현재", "평가단가", "price", "now");
            start = 1;
        }
        ArrayList<Holding> imported = new ArrayList<>();
        for (int i = start; i < rows.size(); i++) {
            String[] r = rows.get(i);
            String c = get(r, code, "");
            String n = get(r, name, c);
            if (c.length() == 0 && n.length() == 0) continue;
            double b = parseNumber(get(r, buy, "0"));
            double p = parseNumber(get(r, now, String.valueOf(b)));
            imported.add(new Holding(get(r, acc, "기본계좌"), c, n, parseNumber(get(r, qty, "0")), b, p));
        }
        holdings.clear();
        holdings.addAll(imported);
    }

    private String makeCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("계좌,종목코드,종목명,수량,평균매입가,현재가,평가금액,손익,수익률\n");
        for (Holding h : holdings) {
            sb.append(csv(h.account)).append(',').append(csv(h.code)).append(',').append(csv(h.name)).append(',')
              .append(h.qty).append(',').append(h.buy).append(',').append(h.now).append(',')
              .append(h.value()).append(',').append(h.pnl()).append(',').append(h.rate()).append('\n');
        }
        return sb.toString();
    }

    private void copyCsv() {
        ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("portfolio.csv", makeCsv()));
    }

    private String csv(String s) { return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\""; }
    private String[] splitCsv(String line) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean q = false;
        for (int i=0;i<line.length();i++) {
            char c = line.charAt(i);
            if (c == '"') q = !q;
            else if (c == ',' && !q) { out.add(sb.toString().trim()); sb.setLength(0); }
            else sb.append(c);
        }
        out.add(sb.toString().trim());
        return out.toArray(new String[0]);
    }
    private boolean looksHeader(String[] r) { String s = join(r); return s.contains("계좌") || s.contains("종목") || s.toLowerCase(Locale.ROOT).contains("account"); }
    private int find(String[] h, String... keys) {
        for (int i=0;i<h.length;i++) {
            String s = h[i].replace(" ", "").toLowerCase(Locale.ROOT);
            for (String k: keys) if (s.contains(k.toLowerCase(Locale.ROOT))) return i;
        }
        return 0;
    }
    private String get(String[] a, int i, String f) { return i >= 0 && i < a.length && a[i].trim().length() > 0 ? a[i].trim() : f; }
    private String join(String[] a) { StringBuilder b = new StringBuilder(); for (String s:a) b.append(s); return b.toString(); }

    private void saveData() {
        try {
            JSONArray arr = new JSONArray();
            for (Holding h : holdings) {
                JSONObject o = new JSONObject();
                o.put("account", h.account); o.put("code", h.code); o.put("name", h.name);
                o.put("qty", h.qty); o.put("buy", h.buy); o.put("now", h.now); arr.put(o);
            }
            getSharedPreferences("data", MODE_PRIVATE).edit().putString("holdings", arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private void loadData() {
        try {
            SharedPreferences sp = getSharedPreferences("data", MODE_PRIVATE);
            JSONArray arr = new JSONArray(sp.getString("holdings", "[]"));
            for (int i=0;i<arr.length();i++) {
                JSONObject o = arr.getJSONObject(i);
                holdings.add(new Holding(o.optString("account"), o.optString("code"), o.optString("name"), o.optDouble("qty"), o.optDouble("buy"), o.optDouble("now")));
            }
        } catch (Exception ignored) {}
    }

    private void addSampleData() {
        holdings.add(new Holding("ISA", "005930.KS", "삼성전자", 10, 70000, 72500));
        holdings.add(new Holding("연금", "035420.KS", "NAVER", 3, 180000, 174000));
        holdings.add(new Holding("해외", "AAPL", "Apple", 2, 180, 192));
    }

    private void confirmReset() {
        new AlertDialog.Builder(this).setTitle("전체 초기화").setMessage("모든 보유종목을 삭제할까요?")
            .setPositiveButton("삭제", (d,w) -> { holdings.clear(); saveData(); refreshAccountsAndDashboard(); })
            .setNegativeButton("취소", null).show();
    }

    private double num(EditText e) { return parseNumber(e.getText().toString()); }
    private double parseNumber(String s) { try { return Double.parseDouble(s.replace(",", "").replace("원", "").replace("₩", "").replace("$", "").trim()); } catch(Exception e) { return 0; } }
    private String cleanAccount(String s) { return s == null || s.trim().length() == 0 ? "기본계좌" : s.trim(); }
    private String trimQty(double d) { return d == Math.rint(d) ? String.valueOf((long)d) : String.valueOf(d); }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    public static class PortfolioChartView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final ArrayList<Holding> data = new ArrayList<>();
        private final int[] colors = { Color.rgb(66,133,244), Color.rgb(251,188,5), Color.rgb(52,168,83), Color.rgb(156,39,176), Color.rgb(0,150,136), Color.rgb(255,112,67) };
        public PortfolioChartView(Context c) { super(c); }
        public void setData(List<Holding> list) { data.clear(); data.addAll(list); invalidate(); }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            int w = getWidth(), h = getHeight();
            p.setColor(Color.WHITE); p.setStyle(Paint.Style.FILL); c.drawRect(0,0,w,h,p);
            p.setTextSize(30); p.setColor(DARK); p.setFakeBoldText(true); c.drawText("자산 비중", 24, 42, p); p.setFakeBoldText(false);
            drawDonut(c, w, h);
            drawBars(c, w, h);
        }
        private void drawDonut(Canvas c, int w, int h) {
            double total = 0; for (Holding x:data) total += Math.max(0, x.value());
            float cx = w * 0.28f, cy = h * 0.28f, r = Math.min(w,h) * 0.18f;
            if (total <= 0) { p.setTextSize(22); p.setColor(Color.GRAY); c.drawText("데이터 없음", 40, 90, p); return; }
            RectF oval = new RectF(cx-r, cy-r, cx+r, cy+r);
            float start = -90;
            for (int i=0;i<data.size();i++) {
                float sweep = (float)(Math.max(0, data.get(i).value()) / total * 360.0);
                p.setColor(colors[i % colors.length]); c.drawArc(oval, start, sweep, true, p); start += sweep;
            }
            p.setColor(Color.WHITE); c.drawCircle(cx, cy, r*0.58f, p);
            p.setTextSize(19); int y = 76;
            for (int i=0;i<Math.min(data.size(),6);i++) {
                Holding x = data.get(i); p.setColor(colors[i%colors.length]); c.drawRect(w*0.56f, y-17, w*0.56f+22, y+5, p);
                p.setColor(Color.rgb(60,60,60)); c.drawText(shortName(x.name) + " " + String.format(Locale.KOREA,"%.1f%%", x.value()/total*100), w*0.56f+30, y+3, p); y += 30;
            }
        }
        private void drawBars(Canvas c, int w, int h) {
            float top = h * 0.58f, base = h - 52, left = 34, right = w - 34;
            p.setTextSize(30); p.setColor(DARK); p.setFakeBoldText(true); c.drawText("종목별 손익", 24, top - 20, p); p.setFakeBoldText(false);
            double max = 1; for (Holding x:data) max = Math.max(max, Math.abs(x.pnl()));
            int n = Math.min(data.size(), 8); if (n == 0) return;
            float gap = 8, bw = (right-left-gap*(n-1))/n;
            p.setColor(Color.rgb(220,220,220)); c.drawLine(left, base, right, base, p);
            for (int i=0;i<n;i++) {
                Holding x = data.get(i); float bh = (float)(Math.abs(x.pnl())/max*(h*0.23f)); float x0 = left + i*(bw+gap);
                p.setColor(x.pnl()>=0 ? PROFIT_RED : LOSS_BLUE); c.drawRect(x0, base-bh, x0+bw, base, p);
                p.setColor(Color.rgb(70,70,70)); p.setTextSize(17); c.drawText(shortName(x.name), x0, base+24, p);
            }
        }
        private static String shortName(String s) { if (s == null) return ""; return s.length() > 5 ? s.substring(0,5) : s; }
    }
}
