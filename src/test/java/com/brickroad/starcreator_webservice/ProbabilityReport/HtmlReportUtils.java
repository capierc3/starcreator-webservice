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
        w.println("<h2 id=\"" + toAnchor(title) + "\">" + esc(title) + "</h2>");
    }

    static void printSubSection(PrintWriter w, String title) {
        w.println("<h3>" + esc(title) + "</h3>");
    }

    static void printSubSubSection(PrintWriter w, String title) {
        w.println("<h4>" + esc(title) + "</h4>");
    }

    /**
     * Collapsible section with an id on the details element for TOC navigation.
     * The id is derived from the title so that #anchor links scroll to this section.
     */
    static void beginCollapsible(PrintWriter w, String title, int headingLevel) {
        String id = toAnchor(title);
        w.println("<details class=\"section\" id=\"" + id + "\">");
        w.println("<summary><h" + headingLevel + ">" + esc(title) + "</h" + headingLevel + "></summary>");
        w.println("<div class=\"section-body\">");
    }

    static void beginCollapsibleOpen(PrintWriter w, String title, int headingLevel) {
        String id = toAnchor(title);
        w.println("<details class=\"section\" id=\"" + id + "\" open>");
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

    /** Opens a stat-card grid row. Call statCard() for each card, then endStatGrid(). */
    static void beginStatGrid(PrintWriter w) {
        w.println("<div class=\"stats-grid\">");
    }

    static void endStatGrid(PrintWriter w) {
        w.println("</div>");
    }

    static void statCard(PrintWriter w, String value, String label) {
        w.println("<div class=\"stat-card\">");
        w.println("<span class=\"stat-value\">" + value + "</span>");
        w.println("<span class=\"stat-label\">" + esc(label) + "</span>");
        w.println("</div>");
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

    static void printPageHeader(PrintWriter w, String headerImagePath) {
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

        // Header image banner (full width, above the grid layout)
        if (headerImagePath != null) {
            w.println("<div class=\"hero-banner\">");
            w.println("<img src=\"" + esc(headerImagePath) + "\" alt=\"Star Creator API\">");
            w.println("</div>");
        }

        // Page grid: sidebar TOC + main content
        w.println("<div class=\"page-grid\">");

        // Sidebar TOC (will be populated in the test class)
        w.println("<aside class=\"toc-sidebar\" id=\"toc-sidebar\">");
        // TOC content injected by HtmlProbabilityReportTest.printSidebarToc()
    }

    /** Call after TOC <li> items have been written. Opens the main content area. */
    static void beginMainContent(PrintWriter w) {
        w.println("</aside>"); // close toc-sidebar
        w.println("<main class=\"main-content\">");
    }

    static void printPageFooter(PrintWriter w) {
        w.println("</main>"); // close main-content
        w.println("</div>");  // close page-grid
        w.println("<script>");
        w.println(JS);
        w.println("</script>");
        w.println("</body>");
        w.println("</html>");
    }

    // ═══════════════════════════════════════════════════════════════
    //  CSS
    // ═══════════════════════════════════════════════════════════════

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
              --toc-width: 240px;
            }
            * { margin: 0; padding: 0; box-sizing: border-box; }
            html { scroll-behavior: smooth; scroll-padding-top: 1rem; }
            body {
              font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
              background: var(--bg);
              color: var(--text);
              line-height: 1.6;
              min-height: 100vh;
            }

            /* ── Hero Banner ── */
            .hero-banner {
              width: 100%;
              background: var(--surface);
              border-bottom: 1px solid var(--border);
              text-align: center;
              overflow: hidden;
            }
            .hero-banner img {
              width: 100%;
              max-height: 280px;
              object-fit: cover;
              object-position: center;
              display: block;
            }

            /* ── Page Grid: Sidebar + Content ── */
            .page-grid {
              display: grid;
              grid-template-columns: var(--toc-width) 1fr;
              max-width: 1400px;
              margin: 0 auto;
              gap: 0;
            }

            /* ── Sidebar TOC ── */
            .toc-sidebar {
              position: sticky;
              top: 0;
              height: 100vh;
              overflow-y: auto;
              padding: 1.2rem 0 2rem 1rem;
              background: var(--surface);
              border-right: 1px solid var(--border);
              scrollbar-width: thin;
              scrollbar-color: var(--border) transparent;
              z-index: 50;
            }
            .toc-sidebar::-webkit-scrollbar { width: 5px; }
            .toc-sidebar::-webkit-scrollbar-track { background: transparent; }
            .toc-sidebar::-webkit-scrollbar-thumb { background: var(--border); border-radius: 3px; }
            .toc-sidebar .toc-title {
              font-size: 0.75rem;
              text-transform: uppercase;
              letter-spacing: 1px;
              color: var(--text-muted);
              padding: 0 0.6rem 0.8rem;
              border-bottom: 1px solid var(--border);
              margin-bottom: 0.5rem;
            }
            .toc-sidebar ol {
              list-style: none;
              padding: 0;
              margin: 0;
            }
            .toc-sidebar li { margin: 0; }
            .toc-sidebar a {
              display: block;
              padding: 0.35rem 0.6rem 0.35rem 0.8rem;
              color: var(--text-muted);
              text-decoration: none;
              font-size: 0.82rem;
              border-left: 2px solid transparent;
              transition: all 0.15s ease;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
            }
            .toc-sidebar a:hover {
              color: var(--text);
              background: rgba(78, 140, 255, 0.06);
            }
            .toc-sidebar a.active {
              color: var(--accent);
              border-left-color: var(--accent);
              background: rgba(78, 140, 255, 0.08);
              font-weight: 600;
            }

            /* ── Main Content ── */
            .main-content {
              padding: 2rem 2.5rem;
              min-width: 0;
            }

            /* Header Card */
            .report-header {
              text-align: center;
              padding: 2.5rem 2rem;
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
              font-size: 2rem;
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
              grid-template-columns: repeat(auto-fit, minmax(130px, 1fr));
              gap: 0.8rem;
              margin: 1.5rem 0;
            }
            .stat-card {
              background: var(--surface);
              border: 1px solid var(--border);
              border-radius: 10px;
              padding: 0.8rem 1rem;
              text-align: center;
            }
            .stat-card .stat-value {
              font-size: 1.5rem;
              font-weight: 700;
              color: var(--accent);
              display: block;
            }
            .stat-card .stat-label {
              font-size: 0.72rem;
              color: var(--text-muted);
              text-transform: uppercase;
              letter-spacing: 0.5px;
            }

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
              scroll-margin-top: 1rem;
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

            /* ── Responsive ── */
            @media (max-width: 900px) {
              .page-grid {
                grid-template-columns: 1fr;
              }
              .toc-sidebar {
                position: fixed;
                left: -280px;
                top: 0;
                width: 280px;
                height: 100vh;
                transition: left 0.3s ease;
                box-shadow: 4px 0 20px rgba(0,0,0,0.5);
              }
              .toc-sidebar.open { left: 0; }
              .toc-toggle {
                display: flex !important;
              }
              .main-content { padding: 1.5rem 1rem; }
              .hero-banner img { max-height: 180px; }
            }
            @media (min-width: 901px) {
              .toc-toggle { display: none !important; }
            }
            @media (max-width: 600px) {
              .stats-grid { grid-template-columns: repeat(auto-fit, minmax(100px, 1fr)); gap: 0.5rem; }
              table { font-size: 0.8rem; }
              .bar-col { display: none; }
              .report-header h1 { font-size: 1.4rem; }
              .report-header { padding: 1.5rem 1rem; }
            }
            """;

    // ═══════════════════════════════════════════════════════════════
    //  JS — IntersectionObserver active section tracking + expand/collapse
    // ═══════════════════════════════════════════════════════════════

    static final String JS = """
            document.addEventListener('DOMContentLoaded', () => {
              // ── Expand/Collapse All button ──
              const expandBtn = document.createElement('button');
              expandBtn.textContent = 'Expand All';
              expandBtn.style.cssText = 'position:fixed;bottom:1.5rem;right:1.5rem;padding:0.6rem 1.2rem;' +
                'background:var(--accent);color:#fff;border:none;border-radius:8px;cursor:pointer;' +
                'font-size:0.85rem;z-index:100;box-shadow:0 4px 12px rgba(0,0,0,0.4);transition:background 0.2s;';
              expandBtn.addEventListener('mouseenter', () => expandBtn.style.background = 'var(--accent2)');
              expandBtn.addEventListener('mouseleave', () => expandBtn.style.background = 'var(--accent)');
              let expanded = false;
              expandBtn.addEventListener('click', () => {
                expanded = !expanded;
                document.querySelectorAll('details.section').forEach(d => d.open = expanded);
                expandBtn.textContent = expanded ? 'Collapse All' : 'Expand All';
              });
              document.body.appendChild(expandBtn);

              // ── Mobile TOC toggle ──
              const tocToggle = document.createElement('button');
              tocToggle.className = 'toc-toggle';
              tocToggle.innerHTML = '&#9776;';
              tocToggle.style.cssText = 'position:fixed;top:0.8rem;left:0.8rem;padding:0.4rem 0.7rem;' +
                'background:var(--surface2);color:var(--accent);border:1px solid var(--border);' +
                'border-radius:6px;cursor:pointer;font-size:1.2rem;z-index:200;display:none;';
              const sidebar = document.getElementById('toc-sidebar');
              tocToggle.addEventListener('click', () => sidebar.classList.toggle('open'));
              document.body.appendChild(tocToggle);
              // Close sidebar on link click (mobile)
              sidebar.querySelectorAll('a').forEach(a => {
                a.addEventListener('click', () => sidebar.classList.remove('open'));
              });

              // ── TOC click: auto-open the target details section ──
              sidebar.querySelectorAll('a[href^="#"]').forEach(link => {
                link.addEventListener('click', (e) => {
                  const targetId = link.getAttribute('href').substring(1);
                  const target = document.getElementById(targetId);
                  if (target && target.tagName === 'DETAILS') {
                    target.open = true;
                  }
                });
              });

              // ── IntersectionObserver: highlight active TOC link ──
              const tocLinks = sidebar.querySelectorAll('a[href^="#"]');
              const sectionIds = Array.from(tocLinks).map(a => a.getAttribute('href').substring(1));
              const sections = sectionIds.map(id => document.getElementById(id)).filter(Boolean);

              if (sections.length === 0) return;

              let activeLink = null;
              function setActive(id) {
                if (activeLink) activeLink.classList.remove('active');
                const link = sidebar.querySelector('a[href="#' + id + '"]');
                if (link) {
                  link.classList.add('active');
                  activeLink = link;
                  // Scroll TOC sidebar to keep active link visible
                  const sidebarRect = sidebar.getBoundingClientRect();
                  const linkRect = link.getBoundingClientRect();
                  if (linkRect.top < sidebarRect.top || linkRect.bottom > sidebarRect.bottom) {
                    link.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
                  }
                }
              }

              const observer = new IntersectionObserver((entries) => {
                // Find the topmost visible section
                let best = null;
                let bestTop = Infinity;
                entries.forEach(entry => {
                  if (entry.isIntersecting) {
                    const top = entry.boundingClientRect.top;
                    if (top < bestTop) {
                      bestTop = top;
                      best = entry.target;
                    }
                  }
                });
                if (best) setActive(best.id);
              }, {
                rootMargin: '-10% 0px -70% 0px',
                threshold: 0
              });

              // Also track scroll to handle sections that are already in view
              // when the observer thresholds don't trigger (e.g. large sections)
              let ticking = false;
              window.addEventListener('scroll', () => {
                if (ticking) return;
                ticking = true;
                requestAnimationFrame(() => {
                  let current = null;
                  for (const section of sections) {
                    const rect = section.getBoundingClientRect();
                    if (rect.top <= window.innerHeight * 0.3) {
                      current = section;
                    }
                  }
                  if (current) setActive(current.id);
                  ticking = false;
                });
              });

              sections.forEach(s => observer.observe(s));

              // Set initial active
              if (sections.length > 0) setActive(sections[0].id);
            });
            """;
}
