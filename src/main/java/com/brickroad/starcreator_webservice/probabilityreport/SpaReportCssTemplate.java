package com.brickroad.starcreator_webservice.probabilityreport;

/**
 * Complete CSS design system for the SPA probability report.
 * "Pulp Sci-Fi Meets Hard Sci-Fi" — retro-futuristic warmth with precise engineering layouts.
 */
public final class SpaReportCssTemplate {

    private SpaReportCssTemplate() {}

    public static final String CSS = """
/* ═══════════════════════════════════════════════════════════════
   Design System — "Pulp Sci-Fi Meets Hard Sci-Fi"
   ═══════════════════════════════════════════════════════════════ */
:root {
  --bg: #0a0c10;
  --panel: #0f1117;
  --card: #13161e;
  --card-hover: #171b25;
  --border: #1e2230;
  --border-glow: #2a2a3a;

  --text: #e4e6ed;
  --text-dim: #8890a4;
  --text-muted: #5a6070;

  --accent: #f59e0b;
  --accent-dim: #b47608;
  --accent-glow: rgba(245, 158, 11, 0.15);
  --accent-soft: rgba(245, 158, 11, 0.06);

  --cyan: #40d8d8;
  --cyan-dim: #2a9a9a;
  --cyan-glow: rgba(64, 216, 216, 0.12);

  --copper: #c07040;
  --rust: #8b5e3c;

  --safe: #34d399;
  --warn: #f59e0b;
  --danger: #ef4444;
  --info: #60a5fa;
  --purple: #a78bfa;

  --font-ui: 'Outfit', 'Segoe UI', sans-serif;
  --font-data: 'JetBrains Mono', 'Cascadia Code', monospace;

  --sidebar-width: 240px;
  --header-height: 52px;
}

* { margin: 0; padding: 0; box-sizing: border-box; }

body {
  font-family: var(--font-ui);
  background: var(--bg);
  color: var(--text);
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  line-height: 1.5;
}

/* ─── Header Bar ─── */
.app-header {
  height: var(--header-height);
  background: var(--panel);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  padding: 0 24px;
  flex-shrink: 0;
  gap: 16px;
  background-image: repeating-linear-gradient(
    0deg, transparent, transparent 2px,
    rgba(255,255,255,0.006) 2px, rgba(255,255,255,0.006) 4px
  );
  position: relative;
  z-index: 100;
}
.app-title {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 2.5px;
  color: var(--accent);
  text-shadow: 0 0 12px rgba(245, 158, 11, 0.35);
  font-family: var(--font-data);
}
.header-sep {
  width: 1px;
  height: 24px;
  background: var(--border);
}
.header-sub {
  font-size: 13px;
  color: var(--text-dim);
  font-weight: 400;
}
.header-meta {
  font-family: var(--font-data);
  font-size: 11px;
  color: var(--text-muted);
  margin-left: auto;
}

/* ─── App Body ─── */
.app-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* ─── Sidebar ─── */
.app-sidebar {
  width: var(--sidebar-width);
  background: var(--panel);
  border-right: 1px solid var(--border);
  overflow-y: auto;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}
.nav-section {
  padding: 16px 0 8px;
}
.nav-group-title {
  font-family: var(--font-data);
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 1.5px;
  color: var(--text-muted);
  padding: 8px 20px 6px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 20px;
  color: var(--text-dim);
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all 0.15s ease;
  font-size: 13px;
  font-weight: 400;
  user-select: none;
}
.nav-item:hover {
  color: var(--text);
  background: var(--accent-soft);
}
.nav-item.active {
  color: var(--accent);
  border-left-color: var(--accent);
  background: var(--accent-glow);
  font-weight: 500;
}
.nav-icon {
  font-size: 15px;
  width: 20px;
  text-align: center;
  flex-shrink: 0;
}
.sidebar-footer {
  margin-top: auto;
  padding: 12px 20px;
  border-top: 1px solid var(--border);
  font-family: var(--font-data);
  font-size: 10px;
  color: var(--text-muted);
  letter-spacing: 0.5px;
}

/* ─── Content Area ─── */
.app-content {
  flex: 1;
  overflow-y: auto;
  padding: 28px 36px 60px;
  scroll-behavior: smooth;
}
.app-content::-webkit-scrollbar { width: 6px; }
.app-content::-webkit-scrollbar-track { background: transparent; }
.app-content::-webkit-scrollbar-thumb {
  background: var(--border);
  border-radius: 3px;
}
.app-content::-webkit-scrollbar-thumb:hover { background: var(--text-muted); }

/* ─── Page Transitions ─── */
.page { animation: pageIn 0.25s ease; }
@keyframes pageIn {
  from { opacity: 0; transform: translateY(6px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* ─── Page Header ─── */
.page-header {
  margin-bottom: 24px;
}
.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  letter-spacing: -0.3px;
  margin-bottom: 4px;
}
.page-header .page-desc {
  font-size: 13px;
  color: var(--text-dim);
}

/* ═══════════════════════════════════════════════════════════════
   Reusable Components
   ═══════════════════════════════════════════════════════════════ */

/* ─── Stat Grid ─── */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 1px;
  background: var(--border);
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 24px;
}
.stat-card {
  background: var(--card);
  padding: 16px 14px;
  text-align: center;
  transition: background 0.15s;
}
.stat-card:hover { background: var(--card-hover); }
.stat-value {
  font-family: var(--font-data);
  font-size: 22px;
  font-weight: 700;
  color: var(--accent);
  text-shadow: 0 0 8px rgba(245, 158, 11, 0.2);
  line-height: 1.2;
}
.stat-label {
  font-family: var(--font-data);
  font-size: 9.5px;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: var(--text-muted);
  margin-top: 4px;
}
.stat-card.cyan .stat-value { color: var(--cyan); text-shadow: 0 0 8px var(--cyan-glow); }
.stat-card.safe .stat-value { color: var(--safe); }
.stat-card.danger .stat-value { color: var(--danger); }
.stat-card.info .stat-value { color: var(--info); }
.stat-card.purple .stat-value { color: var(--purple); }

/* ─── Section Cards ─── */
.section-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 8px;
  margin-bottom: 20px;
  overflow: hidden;
}
.section-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 13px 18px;
  border-bottom: 1px solid var(--border);
  cursor: pointer;
  user-select: none;
  transition: background 0.15s;
}
.section-card-header:hover { background: rgba(255,255,255,0.015); }
.section-card-header .dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.section-card-header h3 {
  font-size: 13px;
  font-weight: 600;
  flex: 1;
}
.section-card-header .chevron {
  font-size: 12px;
  color: var(--text-muted);
  transition: transform 0.2s;
}
.section-card-header .chevron.open { transform: rotate(90deg); }
.section-card-body {
  padding: 16px 18px;
}
.section-card-body.collapsed { display: none; }

/* ─── Data Tables ─── */
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 12px;
  margin: 10px 0;
}
.data-table th {
  font-family: var(--font-data);
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: var(--text-muted);
  text-align: left;
  padding: 7px 10px;
  border-bottom: 1px solid var(--border);
  font-weight: 500;
}
.data-table th.num { text-align: right; }
.data-table td {
  padding: 5px 10px;
  border-bottom: 1px solid rgba(30, 34, 48, 0.5);
  font-family: var(--font-data);
  font-size: 11px;
}
.data-table td.num { text-align: right; color: var(--text-dim); }
.data-table td.key-col { color: var(--text); font-weight: 400; }
.data-table tbody tr:hover { background: var(--accent-soft); }
.data-table tr:last-child td { border-bottom: none; }

/* ─── Bar Fills ─── */
.bar-track {
  height: 6px;
  background: rgba(255,255,255,0.04);
  border-radius: 3px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, var(--accent), var(--copper));
  transition: width 0.4s ease;
  min-width: 2px;
}

/* ─── Collapsible Sections ─── */
.collapsible-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 0;
  cursor: pointer;
  user-select: none;
  border-bottom: 1px solid var(--border);
  margin-bottom: 12px;
}
.collapsible-header h4 {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-dim);
  flex: 1;
}
.collapsible-header .chev {
  font-size: 11px;
  color: var(--text-muted);
  transition: transform 0.2s;
}
.collapsible-header .chev.open { transform: rotate(90deg); }
.collapsible-body.hidden { display: none; }

/* ─── Sub Headings ─── */
.sub-heading {
  font-size: 14px;
  font-weight: 600;
  margin: 20px 0 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--border);
}

/* ─── Note Text ─── */
.note-text {
  font-size: 12px;
  color: var(--text-dim);
  font-style: italic;
  margin: 6px 0 14px;
  line-height: 1.6;
}

/* ─── Two-Column Grid ─── */
.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.two-col .full-span { grid-column: 1 / -1; }

/* ─── Cross-Reference Tables ─── */
.xref-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 11px;
  margin: 10px 0;
}
.xref-table th {
  font-family: var(--font-data);
  font-size: 9px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-muted);
  text-align: center;
  padding: 6px 8px;
  border-bottom: 1px solid var(--border);
}
.xref-table th:first-child { text-align: left; }
.xref-table td {
  padding: 4px 8px;
  text-align: center;
  font-family: var(--font-data);
  font-size: 10px;
  border-bottom: 1px solid rgba(30, 34, 48, 0.4);
  color: var(--text-dim);
}
.xref-table td:first-child {
  text-align: left;
  color: var(--text);
  font-weight: 400;
}
.xref-table tbody tr:hover { background: var(--accent-soft); }

/* ─── Readout Badges ─── */
.readout {
  font-family: var(--font-data);
  font-size: 10px;
  display: inline-block;
  padding: 2px 8px;
  border-radius: 3px;
}
.readout-cyan {
  color: var(--cyan);
  background: rgba(64, 216, 216, 0.08);
  border: 1px solid rgba(64, 216, 216, 0.2);
}
.readout-amber {
  color: var(--accent);
  background: var(--accent-soft);
  border: 1px solid rgba(245, 158, 11, 0.2);
}
.readout-safe {
  color: var(--safe);
  background: rgba(52, 211, 153, 0.08);
  border: 1px solid rgba(52, 211, 153, 0.2);
}
.readout-danger {
  color: var(--danger);
  background: rgba(239, 68, 68, 0.08);
  border: 1px solid rgba(239, 68, 68, 0.2);
}
.readout-purple {
  color: #a78bfa;
  background: rgba(167, 139, 250, 0.08);
  border: 1px solid rgba(167, 139, 250, 0.2);
}

/* ─── Dashboard Nav Cards ─── */
.nav-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-top: 20px;
}
.nav-card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: 8px;
  padding: 16px 18px;
  cursor: pointer;
  transition: all 0.2s;
}
.nav-card:hover {
  border-color: var(--accent-dim);
  background: var(--card-hover);
  transform: translateY(-1px);
}
.nav-card .nav-card-icon {
  font-size: 20px;
  margin-bottom: 6px;
}
.nav-card .nav-card-title {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 2px;
}
.nav-card .nav-card-desc {
  font-size: 11px;
  color: var(--text-muted);
}
.nav-card .nav-card-stat {
  font-family: var(--font-data);
  font-size: 18px;
  font-weight: 700;
  color: var(--accent);
  margin-top: 8px;
}

/* ─── Findings / Callouts ─── */
.finding {
  padding: 10px 14px;
  margin-bottom: 8px;
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.6;
  border-left: 3px solid;
}
.finding-critical { border-left-color: var(--danger); background: rgba(239,68,68,0.06); }
.finding-warning  { border-left-color: var(--warn);   background: rgba(245,158,11,0.06); }
.finding-info     { border-left-color: var(--info);    background: rgba(96,165,250,0.06); }
.finding-good     { border-left-color: var(--safe);    background: rgba(52,211,153,0.06); }

/* ─── Detail Lists ─── */
.detail-list {
  max-height: 300px;
  overflow-y: auto;
  font-family: var(--font-data);
  font-size: 11px;
  margin: 8px 0;
  background: rgba(0,0,0,0.2);
  border-radius: 4px;
  padding: 8px 12px;
}
.detail-list-item {
  padding: 3px 0;
  border-bottom: 1px solid rgba(255,255,255,0.03);
  color: var(--text-dim);
}
.detail-list-item:last-child { border-bottom: none; }

/* ═══════════════════════════════════════════════════════════════
   System Viewer
   ═══════════════════════════════════════════════════════════════ */
.viewer-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.viewer-header h2 {
  font-size: 20px;
  font-weight: 600;
}
.btn-generate {
  font-family: var(--font-data);
  font-size: 12px;
  font-weight: 600;
  padding: 8px 20px;
  border: 1px solid var(--accent);
  background: rgba(245, 158, 11, 0.1);
  color: var(--accent);
  border-radius: 6px;
  cursor: pointer;
  letter-spacing: 0.5px;
  transition: all 0.2s;
}
.btn-generate:hover {
  background: rgba(245, 158, 11, 0.2);
  box-shadow: 0 0 16px rgba(245, 158, 11, 0.15);
}
.btn-generate:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.viewer-status {
  font-family: var(--font-data);
  font-size: 11px;
  color: var(--text-muted);
}
.viewer-status.error { color: var(--danger); }
.viewer-body {
  display: flex;
  gap: 0;
  border: 1px solid var(--border);
  border-radius: 8px;
  overflow: hidden;
  height: calc(100vh - 180px);
  min-height: 500px;
}
.viewer-canvas-area {
  flex: 1;
  background: #060810;
  position: relative;
  display: flex;
  flex-direction: column;
}
.viewer-canvas-area canvas {
  width: 100%;
  flex: 1;
}
.viewer-controls {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 14px;
  background: var(--panel);
  border-top: 1px solid var(--border);
  font-family: var(--font-data);
  font-size: 11px;
  color: var(--text-dim);
}
.viewer-controls button {
  font-family: var(--font-data);
  font-size: 11px;
  padding: 4px 10px;
  border: 1px solid var(--border);
  background: var(--card);
  color: var(--text-dim);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s;
}
.viewer-controls button:hover { border-color: var(--accent); color: var(--accent); }
.viewer-controls button.active { border-color: var(--accent); color: var(--accent); background: var(--accent-glow); }
.viewer-controls input[type=range] {
  width: 100px;
  accent-color: var(--accent);
}
.viewer-sidebar {
  width: 280px;
  background: var(--panel);
  border-left: 1px solid var(--border);
  overflow-y: auto;
  padding: 14px;
  flex-shrink: 0;
}
.viewer-sidebar .section { margin-bottom: 16px; }
.viewer-sidebar .section-title {
  font-family: var(--font-data);
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: var(--text-muted);
  margin-bottom: 8px;
  padding-bottom: 4px;
  border-bottom: 1px solid var(--border);
}
.viewer-sidebar .planet-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 0;
  font-size: 11px;
}
.viewer-sidebar .planet-pip {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.viewer-sidebar .planet-name { flex: 1; font-weight: 400; }
.viewer-sidebar .planet-meta {
  font-family: var(--font-data);
  font-size: 10px;
  color: var(--text-muted);
}
.viewer-sidebar .stability-row {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 0;
  font-size: 11px;
}
.viewer-sidebar .badge {
  font-family: var(--font-data);
  font-size: 9px;
  padding: 1px 6px;
  border-radius: 3px;
  font-weight: 600;
}
.viewer-sidebar .badge-safe { background: rgba(52,211,153,0.15); color: var(--safe); }
.viewer-sidebar .badge-warn { background: rgba(245,158,11,0.15); color: var(--warn); }
.viewer-sidebar .badge-danger { background: rgba(239,68,68,0.15); color: var(--danger); }
.viewer-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted);
  font-size: 13px;
  text-align: center;
  padding: 40px;
  line-height: 1.8;
}

/* ─── Responsive ─── */
@media (max-width: 900px) {
  .app-sidebar { display: none; }
  .app-content { padding: 20px 16px; }
  .two-col { grid-template-columns: 1fr; }
  .stat-grid { grid-template-columns: repeat(auto-fit, minmax(110px, 1fr)); }
  .viewer-sidebar { display: none; }
}
""";
}
