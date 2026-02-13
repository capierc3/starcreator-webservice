package com.brickroad.starcreator_webservice.ProbabilityReport;

import java.io.PrintWriter;
import java.util.Map;

public class HtmlReportUtils {

    static String pct(int count, int total) {
        return String.format("%.1f", count * 100.0 / Math.max(1, total));
    }

    static String pct(double count, int total) {
        return String.format("%.1f", count * 100.0 / Math.max(1, total));
    }

    // ── Table Methods ──

    static void printSortedTable(PrintWriter w, Map<String, Integer> data, int total, String col1Name) {
        w.println("<table>");
        w.println("<thead><tr><th>" + col1Name + "</th><th>Count</th><th>%</th><th class=\"bar-col\">Distribution</th></tr></thead>");
        w.println("<tbody>");
        data.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> {
                    double pctVal = e.getValue() * 100.0 / Math.max(1, total);
                    w.println("<tr><td>" + esc(e.getKey()) + "</td><td>" + e.getValue()
                            + "</td><td>" + String.format("%.1f", pctVal) + "%</td><td>"
                            + bar(pctVal) + "</td></tr>");
                });
        w.println("</tbody></table>");
    }

    static void printSortedTableByKey(PrintWriter w, Map<String, Integer> data, int total, String col1Name) {
        w.println("<table>");
        w.println("<thead><tr><th>" + col1Name + "</th><th>Count</th><th>%</th><th class=\"bar-col\">Distribution</th></tr></thead>");
        w.println("<tbody>");
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    double pctVal = e.getValue() * 100.0 / Math.max(1, total);
                    w.println("<tr><td>" + esc(e.getKey()) + "</td><td>" + e.getValue()
                            + "</td><td>" + String.format("%.1f", pctVal) + "%</td><td>"
                            + bar(pctVal) + "</td></tr>");
                });
        w.println("</tbody></table>");
    }

    static void printLinkedTable(PrintWriter w, Map<String, Integer> data, int total, String col1Name) {
        w.println("<table>");
        w.println("<thead><tr><th>" + col1Name + "</th><th>Count</th><th>%</th><th class=\"bar-col\">Distribution</th></tr></thead>");
        w.println("<tbody>");
        data.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> {
                    String anchor = toAnchor(e.getKey());
                    double pctVal = e.getValue() * 100.0 / Math.max(1, total);
                    w.println("<tr><td><a href=\"#" + anchor + "\">" + esc(e.getKey()) + "</a></td><td>"
                            + e.getValue() + "</td><td>" + String.format("%.1f", pctVal) + "%</td><td>"
                            + bar(pctVal) + "</td></tr>");
                });
        w.println("</tbody></table>");
    }

    // ── Structural Methods ──

    static String toAnchor(String text) {
        return text.toLowerCase().replace(" ", "-").replaceAll("[^a-z0-9\\-]", "");
    }

    static void printAnchor(PrintWriter w, String text) {
        w.println("<span id=\"" + toAnchor(text) + "\"></span>");
    }

    static void printSection(PrintWriter w, String title) {
        w.println("<hr>");
        w.println("<h2>" + esc(title) + "</h2>");
    }

    static void printSubSection(PrintWriter w, String title) {
        w.println("<h3>" + esc(title) + "</h3>");
    }

    static void printSubSubSection(PrintWriter w, String title) {
        w.println("<h4>" + esc(title) + "</h4>");
    }

    static void beginCollapsible(PrintWriter w, String title, int headingLevel) {
        w.println("<details class=\"section\">");
        w.println("<summary><h" + headingLevel + ">" + esc(title) + "</h" + headingLevel + "></summary>");
        w.println("<div class=\"section-body\">");
    }

    static void beginCollapsibleOpen(PrintWriter w, String title, int headingLevel) {
        w.println("<details class=\"section\" open>");
        w.println("<summary><h" + headingLevel + ">" + esc(title) + "</h" + headingLevel + "></summary>");
        w.println("<div class=\"section-body\">");
    }

    static void endCollapsible(PrintWriter w) {
        w.println("</div>");
        w.println("</details>");
    }

    static void p(PrintWriter w, String text) {
        w.println("<p>" + text + "</p>");
    }

    static void stat(PrintWriter w, String label, String value) {
        w.println("<div class=\"stat\"><span class=\"stat-label\">" + esc(label) + "</span><span class=\"stat-value\">" + value + "</span></div>");
    }

    // ── Helpers ──

    private static String bar(double pctVal) {
        double clamped = Math.min(100, Math.max(0, pctVal));
        return "<div class=\"bar\"><div class=\"bar-fill\" style=\"width:" + String.format("%.1f", clamped) + "%\"></div></div>";
    }

    static String esc(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // ── Page Template ──

    static void printPageHeader(PrintWriter w) {
        w.println("<!DOCTYPE html>");
        w.println("<html lang=\"en\">");
        w.println("<head>");
        w.println("<meta charset=\"UTF-8\">");
        w.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        w.println("<title>Star System Probability Report</title>");
        w.println("<style>");
        w.println(CSS);
        w.println("</style>");
        w.println("</head>");
        w.println("<body>");
        w.println("<div class=\"container\">");
    }

    static void printPageFooter(PrintWriter w) {
        w.println("</div>"); // container
        w.println("<script>");
        w.println(JS);
        w.println("</script>");
        w.println("</body>");
        w.println("</html>");
    }

    // ── CSS ──

    static final String CSS = """
            :root {
              --bg: #0b0e17;
              --surface: #131825;
              --surface2: #1a2035;
              --border: #2a3050;
              --text: #c8d0e0;
              --text-muted: #6b7394;
              --accent: #4e8cff;
              --accent2: #7b61ff;
              --gold: #f0b860;
              --green: #4cd080;
              --red: #f06060;
              --cyan: #40d8d8;
            }
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body {
              font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
              background: var(--bg);
              color: var(--text);
              line-height: 1.6;
              min-height: 100vh;
            }
            .container { max-width: 1100px; margin: 0 auto; padding: 2rem 1.5rem; }

            /* Header */
            .report-header {
              text-align: center;
              padding: 3rem 2rem;
              margin-bottom: 2rem;
              background: linear-gradient(135deg, var(--surface) 0%, var(--surface2) 100%);
              border: 1px solid var(--border);
              border-radius: 16px;
              position: relative;
              overflow: hidden;
            }
            .report-header::before {
              content: '';
              position: absolute;
              top: 0; left: 0; right: 0; height: 3px;
              background: linear-gradient(90deg, var(--accent), var(--accent2), var(--cyan));
            }
            .report-header h1 {
              font-size: 2.2rem;
              font-weight: 700;
              background: linear-gradient(135deg, var(--accent), var(--cyan));
              -webkit-background-clip: text;
              -webkit-text-fill-color: transparent;
              background-clip: text;
              margin-bottom: 0.5rem;
            }
            .report-header .subtitle {
              color: var(--text-muted);
              font-size: 0.95rem;
            }

            /* Stats Grid */
            .stats-grid {
              display: grid;
              grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
              gap: 1rem;
              margin: 1.5rem 0;
            }
            .stat-card {
              background: var(--surface);
              border: 1px solid var(--border);
              border-radius: 10px;
              padding: 1rem 1.2rem;
              text-align: center;
            }
            .stat-card .stat-value {
              font-size: 1.6rem;
              font-weight: 700;
              color: var(--accent);
              display: block;
            }
            .stat-card .stat-label {
              font-size: 0.78rem;
              color: var(--text-muted);
              text-transform: uppercase;
              letter-spacing: 0.5px;
            }

            /* TOC */
            .toc {
              background: var(--surface);
              border: 1px solid var(--border);
              border-radius: 12px;
              padding: 1.5rem 2rem;
              margin-bottom: 2rem;
            }
            .toc h2 { font-size: 1.1rem; color: var(--accent); margin-bottom: 0.8rem; }
            .toc ol { padding-left: 1.5rem; }
            .toc li { margin: 0.3rem 0; }
            .toc a { color: var(--text); text-decoration: none; transition: color 0.2s; }
            .toc a:hover { color: var(--accent); }

            /* Sections */
            hr {
              border: none;
              height: 1px;
              background: var(--border);
              margin: 2rem 0;
            }
            h2 { font-size: 1.5rem; color: var(--text); margin: 0.5rem 0; }
            h3 { font-size: 1.15rem; color: var(--text); margin: 1.2rem 0 0.5rem; }
            h4 { font-size: 1rem; color: var(--text-muted); margin: 1rem 0 0.4rem; }

            /* Collapsible Details */
            details.section {
              background: var(--surface);
              border: 1px solid var(--border);
              border-radius: 12px;
              margin: 1rem 0;
              overflow: hidden;
            }
            details.section > summary {
              cursor: pointer;
              padding: 0.8rem 1.2rem;
              background: var(--surface2);
              border-bottom: 1px solid var(--border);
              list-style: none;
              display: flex;
              align-items: center;
              gap: 0.5rem;
              user-select: none;
              transition: background 0.2s;
            }
            details.section > summary:hover { background: #1e2845; }
            details.section > summary::before {
              content: '\\25B6';
              font-size: 0.7rem;
              color: var(--accent);
              transition: transform 0.2s;
              flex-shrink: 0;
            }
            details.section[open] > summary::before { transform: rotate(90deg); }
            details.section > summary::-webkit-details-marker { display: none; }
            details.section > summary h2,
            details.section > summary h3,
            details.section > summary h4 {
              margin: 0;
              display: inline;
              font-size: inherit;
            }
            details.section > summary h2 { font-size: 1.3rem; }
            details.section > summary h3 { font-size: 1.05rem; }
            details.section > summary h4 { font-size: 0.95rem; }
            .section-body { padding: 1.2rem; }

            /* Nested details */
            details.section details.section {
              border-radius: 8px;
              margin: 0.6rem 0;
            }
            details.section details.section > summary {
              padding: 0.6rem 1rem;
            }

            /* Tables */
            table {
              width: 100%;
              border-collapse: collapse;
              margin: 0.8rem 0 1.2rem;
              font-size: 0.88rem;
            }
            thead th {
              text-align: left;
              padding: 0.6rem 0.8rem;
              background: var(--surface2);
              color: var(--text-muted);
              font-weight: 600;
              text-transform: uppercase;
              font-size: 0.75rem;
              letter-spacing: 0.5px;
              border-bottom: 2px solid var(--border);
            }
            tbody td {
              padding: 0.5rem 0.8rem;
              border-bottom: 1px solid #1a2035;
            }
            tbody tr:hover { background: rgba(78, 140, 255, 0.04); }
            td a { color: var(--accent); text-decoration: none; }
            td a:hover { text-decoration: underline; }
            .bar-col { width: 30%; }

            /* Distribution Bars */
            .bar {
              width: 100%;
              height: 8px;
              background: var(--surface);
              border-radius: 4px;
              overflow: hidden;
            }
            .bar-fill {
              height: 100%;
              background: linear-gradient(90deg, var(--accent), var(--accent2));
              border-radius: 4px;
              min-width: 2px;
              transition: width 0.3s ease;
            }

            /* Inline Stats */
            .stat {
              display: inline-flex;
              gap: 0.4rem;
              margin-right: 1.5rem;
              margin-bottom: 0.4rem;
            }
            .stat .stat-label { color: var(--text-muted); }
            .stat .stat-value { color: var(--text); font-weight: 600; }

            /* Paragraphs */
            p { margin: 0.5rem 0; }
            .summary-text { color: var(--text-muted); font-size: 0.9rem; margin: 0.5rem 0 1rem; }
            .note { color: var(--text-muted); font-style: italic; font-size: 0.88rem; }
            strong { color: var(--text); }

            /* Custom Table for Cross-Reference */
            .xref-table { font-size: 0.85rem; }
            .xref-table th { font-size: 0.72rem; }
            .xref-table td { text-align: center; }
            .xref-table td:first-child { text-align: left; }

            /* Responsive */
            @media (max-width: 768px) {
              .container { padding: 1rem; }
              .report-header { padding: 2rem 1rem; }
              .report-header h1 { font-size: 1.6rem; }
              .stats-grid { grid-template-columns: repeat(auto-fit, minmax(120px, 1fr)); gap: 0.6rem; }
              table { font-size: 0.8rem; }
              .bar-col { display: none; }
            }
            """;

    // ── JS (expand/collapse all) ──

    static final String JS = """
            document.addEventListener('DOMContentLoaded', () => {
              const container = document.querySelector('.container');
              const btn = document.createElement('button');
              btn.textContent = 'Expand All';
              btn.style.cssText = 'position:fixed;bottom:1.5rem;right:1.5rem;padding:0.6rem 1.2rem;' +
                'background:var(--accent);color:#fff;border:none;border-radius:8px;cursor:pointer;' +
                'font-size:0.85rem;z-index:100;box-shadow:0 4px 12px rgba(0,0,0,0.4);transition:background 0.2s;';
              btn.addEventListener('mouseenter', () => btn.style.background = 'var(--accent2)');
              btn.addEventListener('mouseleave', () => btn.style.background = 'var(--accent)');
              let expanded = false;
              btn.addEventListener('click', () => {
                expanded = !expanded;
                document.querySelectorAll('details.section').forEach(d => d.open = expanded);
                btn.textContent = expanded ? 'Collapse All' : 'Expand All';
              });
              document.body.appendChild(btn);
            });
            """;
}
