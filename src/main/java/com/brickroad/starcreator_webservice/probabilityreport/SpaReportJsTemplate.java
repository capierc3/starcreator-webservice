package com.brickroad.starcreator_webservice.probabilityreport;

/**
 * Complete JavaScript SPA engine for the probability report.
 * Contains: router, reusable components, and all page renderers.
 * Split into two parts to stay under Java constant pool 65535-byte limit.
 */
public final class SpaReportJsTemplate {

    private SpaReportJsTemplate() {}

    /** Returns the full JS as a single string (split across constants to stay under 65535 byte limit). */
    public static String js() {
        // Use StringBuilder to prevent compile-time constant folding
        return new StringBuilder(JS_PART1).append(JS_PART2).toString();
    }

    static final String JS_PART1 = """
'use strict';

/* ═══════════════════════════════════════════════════════════════
   SPA Router & Navigation
   ═══════════════════════════════════════════════════════════════ */

const PAGES = {
  dashboard:  { label: 'Dashboard',          icon: '\u2302',  render: renderDashboard },
  stars:      { label: 'Stars',              icon: '\u2605',  render: renderStars },
  planets:    { label: 'Planets',            icon: '\u25CF',  render: renderPlanets },
  moons:      { label: 'Moons',             icon: '\u263E',  render: renderMoons },
  rings:      { label: 'Rings',             icon: '\u25EF',  render: renderRings },
  trojans:    { label: 'Trojans',          icon: '\u25C7',  render: renderTrojans },
  asteroids:  { label: 'Asteroids',        icon: '\u2B25',  render: renderNotableAsteroids },
  belts:      { label: 'Belts',             icon: '\u2058',  render: renderBelts },
  climate:    { label: 'Climate',           icon: '\u2602',  render: renderClimate },
  stability:  { label: 'Orbital Stability', icon: '\u2300',  render: renderStability },
  viewer:     { label: 'System Viewer',     icon: '\u269B',  render: renderViewer }
};

let currentPage = 'dashboard';

function navigateTo(page) {
  if (!PAGES[page]) return;
  window.location.hash = page;
}

function handleRoute() {
  const hash = window.location.hash.slice(1) || 'dashboard';
  if (!PAGES[hash]) { navigateTo('dashboard'); return; }
  currentPage = hash;
  document.querySelectorAll('.nav-item').forEach(el => {
    el.classList.toggle('active', el.dataset.page === hash);
  });
  const content = document.getElementById('app-content');
  content.innerHTML = '';
  content.scrollTop = 0;
  PAGES[hash].render(content);
}

window.addEventListener('hashchange', handleRoute);

/* ═══════════════════════════════════════════════════════════════
   App Initialization
   ═══════════════════════════════════════════════════════════════ */

function initApp() {
  const body = document.body;
  const R = REPORT_DATA;

  // Header
  const header = el('div', 'app-header');
  header.innerHTML =
    '<span class="app-title">STARCREATOR</span>' +
    '<span class="header-sep"></span>' +
    '<span class="header-sub">Probability Report</span>' +
    '<span class="header-meta">' + fmt(R.metadata.systemCount) + ' systems \\u00B7 ' +
    esc(R.metadata.generationTime) + '</span>';
  body.appendChild(header);

  // Body
  const appBody = el('div', 'app-body');

  // Sidebar
  const sidebar = el('div', 'app-sidebar');
  const nav = el('div', 'nav-section');
  const title1 = el('div', 'nav-group-title');
  title1.textContent = 'REPORT';
  nav.appendChild(title1);

  const reportPages = ['dashboard','stars','planets','moons','rings','trojans','asteroids','belts','climate','stability'];
  const viewerPages = ['viewer'];

  reportPages.forEach(key => {
    nav.appendChild(makeNavItem(key));
  });

  const title2 = el('div', 'nav-group-title');
  title2.textContent = 'TOOLS';
  title2.style.marginTop = '12px';
  nav.appendChild(title2);

  viewerPages.forEach(key => {
    nav.appendChild(makeNavItem(key));
  });

  sidebar.appendChild(nav);

  const footer = el('div', 'sidebar-footer');
  footer.textContent = 'STARCREATOR v0.1';
  sidebar.appendChild(footer);

  appBody.appendChild(sidebar);

  // Content
  const content = el('div', 'app-content');
  content.id = 'app-content';
  appBody.appendChild(content);

  body.appendChild(appBody);
  handleRoute();
}

function makeNavItem(key) {
  const page = PAGES[key];
  const item = el('div', 'nav-item');
  item.dataset.page = key;
  item.innerHTML = '<span class="nav-icon">' + page.icon + '</span>' + esc(page.label);
  item.addEventListener('click', () => navigateTo(key));
  return item;
}

document.addEventListener('DOMContentLoaded', initApp);

/* ═══════════════════════════════════════════════════════════════
   Utility Functions
   ═══════════════════════════════════════════════════════════════ */

function el(tag, cls) {
  const e = document.createElement(tag);
  if (cls) e.className = cls;
  return e;
}

function esc(s) {
  if (s == null) return '';
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function fmt(n) {
  if (n == null) return '0';
  return Number(n).toLocaleString();
}

function pct(n, total) {
  if (!total) return '0.0';
  return (n * 100 / total).toFixed(1);
}

function round2(n) {
  return Math.round(n * 100) / 100;
}

function fmtShort(n) {
  if (n == null || n === 0) return '0';
  if (n >= 1e12) return (n / 1e12).toFixed(1) + 'T';
  if (n >= 1e9) return (n / 1e9).toFixed(1) + 'B';
  if (n >= 1e6) return (n / 1e6).toFixed(1) + 'M';
  if (n >= 1e3) return (n / 1e3).toFixed(1) + 'K';
  return String(n);
}

function pctBar(val) {
  var p = Math.min(100, Math.max(0, val || 0));
  var color = p >= 50 ? 'var(--accent)' : p >= 20 ? 'var(--copper)' : 'var(--text-muted)';
  return '<div style="display:flex;align-items:center;gap:6px;min-width:100px">' +
    '<div class="bar-track" style="flex:1"><div class="bar-fill" style="width:' + p +
    '%;background:' + color + '"></div></div>' +
    '<span style="font-family:var(--font-data);font-size:10px;color:var(--text-dim);min-width:38px;text-align:right">' +
    p.toFixed(1) + '%</span></div>';
}

function sumValues(obj) {
  if (!obj) return 0;
  return Object.values(obj).reduce((a, b) => a + (typeof b === 'number' ? b : 0), 0);
}

function topEntry(obj) {
  if (!obj) return ['N/A', 0];
  let best = null;
  for (const [k, v] of Object.entries(obj)) {
    if (!best || v > best[1]) best = [k, v];
  }
  return best || ['N/A', 0];
}

/** Strip alphabetic prefix used for sort ordering (e.g., "a: < 0.5 AU" → "< 0.5 AU") */
function stripPrefix(s) {
  if (!s) return '';
  return s.replace(/^[a-z]:\\s*/i, '');
}

/* ═══════════════════════════════════════════════════════════════
   Reusable Components
   ═══════════════════════════════════════════════════════════════ */

/** Page header with title and optional description */
function pageHeader(container, title, desc) {
  const hdr = el('div', 'page-header');
  const h2 = el('h2');
  h2.textContent = title;
  hdr.appendChild(h2);
  if (desc) {
    const p = el('p', 'page-desc');
    p.textContent = desc;
    hdr.appendChild(p);
  }
  container.appendChild(hdr);
}

/** Stat card grid. cards = [{value, label, cls?}] */
function statGrid(container, cards) {
  const grid = el('div', 'stat-grid');
  for (const c of cards) {
    const card = el('div', 'stat-card' + (c.cls ? ' ' + c.cls : ''));
    card.innerHTML = '<div class="stat-value">' + esc(String(c.value)) + '</div>' +
                     '<div class="stat-label">' + esc(c.label) + '</div>';
    grid.appendChild(card);
  }
  container.appendChild(grid);
}

/**
 * Distribution table with bar chart.
 * data = {key: count} or null. total = denominator for %. col1 = column header.
 * opts: {sortByKey, stripPrefixes, maxRows}
 */
function distTable(container, data, total, col1, opts) {
  if (!data || Object.keys(data).length === 0) return;
  opts = opts || {};
  let entries = Object.entries(data);
  if (opts.sortByKey) {
    entries.sort((a, b) => a[0].localeCompare(b[0]));
  } else {
    entries.sort((a, b) => b[1] - a[1]);
  }
  if (opts.maxRows && entries.length > opts.maxRows) {
    entries = entries.slice(0, opts.maxRows);
  }
  const tbl = el('table', 'data-table');
  tbl.innerHTML = '<thead><tr><th>' + esc(col1) + '</th>' +
    '<th class="num">Count</th><th class="num">%</th>' +
    '<th style="width:28%">Distribution</th></tr></thead>';
  const tbody = el('tbody');
  for (const [key, count] of entries) {
    const p = pct(count, total);
    const label = opts.stripPrefixes ? stripPrefix(key) : key;
    const tr = el('tr');
    tr.innerHTML = '<td class="key-col">' + esc(label) + '</td>' +
      '<td class="num">' + fmt(count) + '</td>' +
      '<td class="num">' + p + '%</td>' +
      '<td><div class="bar-track"><div class="bar-fill" style="width:' +
      Math.min(100, parseFloat(p)) + '%"></div></div></td>';
    tbody.appendChild(tr);
  }
  tbl.appendChild(tbody);
  container.appendChild(tbl);
}

/** Section card with collapsible body. buildFn(body) populates the body. */
function sectionCard(container, title, dotColor, buildFn, startOpen) {
  const card = el('div', 'section-card');
  const hdr = el('div', 'section-card-header');
  hdr.innerHTML = '<div class="dot" style="background:' + dotColor + '"></div>' +
    '<h3>' + esc(title) + '</h3>' +
    '<span class="chevron' + (startOpen !== false ? ' open' : '') + '">\u25B6</span>';
  card.appendChild(hdr);

  const body = el('div', 'section-card-body' + (startOpen === false ? ' collapsed' : ''));
  buildFn(body);
  card.appendChild(body);

  hdr.addEventListener('click', () => {
    const isOpen = !body.classList.contains('collapsed');
    body.classList.toggle('collapsed', isOpen);
    hdr.querySelector('.chevron').classList.toggle('open', !isOpen);
  });

  container.appendChild(card);
}

/** Collapsible subsection (lighter weight than section card) */
function collapsible(container, title, buildFn, startOpen) {
  const hdr = el('div', 'collapsible-header');
  hdr.innerHTML = '<span class="chev' + (startOpen ? ' open' : '') + '">\u25B6</span>' +
    '<h4>' + esc(title) + '</h4>';
  container.appendChild(hdr);

  const body = el('div', 'collapsible-body' + (startOpen ? '' : ' hidden'));
  buildFn(body);
  container.appendChild(body);

  hdr.addEventListener('click', () => {
    const isOpen = !body.classList.contains('hidden');
    body.classList.toggle('hidden', isOpen);
    hdr.querySelector('.chev').classList.toggle('open', !isOpen);
  });
}

/** Simple sub-heading */
function subHeading(container, text) {
  const h = el('h3', 'sub-heading');
  h.textContent = text;
  container.appendChild(h);
}

/** Italic note text */
function note(container, text) {
  const p = el('p', 'note-text');
  p.innerHTML = text;
  container.appendChild(p);
}

/** Two-column layout wrapper */
function twoCol(container, leftFn, rightFn) {
  const grid = el('div', 'two-col');
  const left = el('div');
  leftFn(left);
  grid.appendChild(left);
  const right = el('div');
  rightFn(right);
  grid.appendChild(right);
  container.appendChild(grid);
}

/** Cross-reference table: rowData = {rowKey: {colKey: value}} */
function xrefTable(container, rowData, rowLabel, colKeys) {
  if (!rowData || Object.keys(rowData).length === 0) return;
  const tbl = el('table', 'xref-table');
  let hdr = '<thead><tr><th>' + esc(rowLabel) + '</th>';
  for (const ck of colKeys) hdr += '<th>' + esc(ck) + '</th>';
  hdr += '</tr></thead>';
  tbl.innerHTML = hdr;
  const tbody = el('tbody');
  for (const [rk, row] of Object.entries(rowData)) {
    const tr = el('tr');
    let html = '<td>' + esc(rk) + '</td>';
    for (const ck of colKeys) {
      const v = row[ck];
      html += '<td>' + (v != null ? (typeof v === 'number' ? fmt(v) : esc(String(v))) : '-') + '</td>';
    }
    tr.innerHTML = html;
    tbody.appendChild(tr);
  }
  tbl.appendChild(tbody);
  container.appendChild(tbl);
}

/** Cross-reference table from stability format: {rowKey: {colKey: count}} */
function stabilityCrossRef(container, data, rowLabel) {
  if (!data || Object.keys(data).length === 0) return;
  const colSet = new Set();
  for (const row of Object.values(data)) {
    for (const k of Object.keys(row)) colSet.add(k);
  }
  const cols = Array.from(colSet);
  xrefTable(container, data, rowLabel, cols);
}

/** Finding callout */
function finding(container, type, text) {
  const div = el('div', 'finding finding-' + type);
  div.innerHTML = text;
  container.appendChild(div);
}

/** Detail list (monospace, scrollable) */
function detailList(container, items) {
  if (!items || items.length === 0) return;
  const list = el('div', 'detail-list');
  for (const item of items) {
    const row = el('div', 'detail-list-item');
    row.textContent = item;
    list.appendChild(row);
  }
  container.appendChild(list);
}

/* ═══════════════════════════════════════════════════════════════
   PAGE: Dashboard
   ═══════════════════════════════════════════════════════════════ */

function renderDashboard(c) {
  const R = REPORT_DATA;
  const S = R.summary;
  const M = R.metadata;

  pageHeader(c, 'Dashboard', 'Executive summary across ' + fmt(M.systemCount) + ' generated star systems');

  // Exec summary stats
  statGrid(c, [
    { value: fmt(S.systems),   label: 'Systems' },
    { value: fmt(S.stars),     label: 'Stars', cls: 'cyan' },
    { value: fmt(S.planets),   label: 'Planets' },
    { value: fmt(S.moons),     label: 'Moons' },
    { value: fmt(S.moonlets),  label: 'Moonlets', cls: 'purple' },
    { value: fmt(S.rings),     label: 'Rings', cls: 'cyan' },
    { value: fmt(S.trojans || 0), label: 'Trojans' },
    { value: fmt(S.belts),     label: 'Belts' },
    { value: fmt(S.asteroids), label: 'Asteroids', cls: 'purple' }
  ]);

  // Generation metadata
  sectionCard(c, 'Generation Metrics', '#f59e0b', function(body) {
    statGrid(body, [
      { value: esc(M.generationTime), label: 'Total Time' },
      { value: round2(M.averageTimePerSystemMs) + ' ms', label: 'Avg / System', cls: 'cyan' },
      { value: fmt(M.totalTimeMs) + ' ms', label: 'Total (ms)' },
      { value: esc(M.timestamp ? M.timestamp.replace('T', ' ').substring(0, 19) : ''), label: 'Generated At', cls: 'info' }
    ]);
  });

  // Quick Insights
  sectionCard(c, 'Quick Insights', '#40d8d8', function(body) {
    const grid = el('div', 'two-col');

    // Most common star type
    const starTop = topEntry(R.stars ? R.stars.types : null);
    const starCard = el('div');
    const starTotalStars = S.stars || 0;
    starCard.innerHTML = '<div style="margin-bottom:12px">' +
      '<div style="font-size:11px;color:var(--text-muted);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px">Most Common Star Type</div>' +
      '<div style="font-family:var(--font-data);font-size:18px;color:var(--accent)">' + esc(starTop[0]) + '</div>' +
      '<div style="font-family:var(--font-data);font-size:11px;color:var(--text-dim)">' + fmt(starTop[1]) + ' / ' + fmt(starTotalStars) + ' (' + pct(starTop[1], starTotalStars) + '%)</div>' +
      '</div>';
    grid.appendChild(starCard);

    // Most common planet type
    const planetTop = topEntry(R.planets ? R.planets.types : null);
    const totalPlanets = S.planets || 0;
    const pCard = el('div');
    pCard.innerHTML = '<div style="margin-bottom:12px">' +
      '<div style="font-size:11px;color:var(--text-muted);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px">Most Common Planet Type</div>' +
      '<div style="font-family:var(--font-data);font-size:18px;color:var(--cyan)">' + esc(planetTop[0]) + '</div>' +
      '<div style="font-family:var(--font-data);font-size:11px;color:var(--text-dim)">' + fmt(planetTop[1]) + ' / ' + fmt(totalPlanets) + ' (' + pct(planetTop[1], totalPlanets) + '%)</div>' +
      '</div>';
    grid.appendChild(pCard);

    // Stability overview
    const stab = R.orbitalStability ? R.orbitalStability.summary : {};
    const allStable = stab.systemsAllStable || 0;
    const multiPlanet = stab.multiPlanetSystems || 1;
    const sCard = el('div');
    sCard.innerHTML = '<div style="margin-bottom:12px">' +
      '<div style="font-size:11px;color:var(--text-muted);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px">Systems All Stable</div>' +
      '<div style="font-family:var(--font-data);font-size:18px;color:var(--safe)">' + pct(allStable, multiPlanet) + '%</div>' +
      '<div style="font-family:var(--font-data);font-size:11px;color:var(--text-dim)">' + fmt(allStable) + ' / ' + fmt(multiPlanet) + ' multi-planet</div>' +
      '</div>';
    grid.appendChild(sCard);

    // Habitability highlights
    const pSum = R.planets ? R.planets.summary : {};
    const hCard = el('div');
    hCard.innerHTML = '<div style="margin-bottom:12px">' +
      '<div style="font-size:11px;color:var(--text-muted);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px">Habitability</div>' +
      '<div style="font-family:var(--font-data);font-size:18px;color:var(--safe)">' + fmt(pSum.breathablePlanets || 0) + '</div>' +
      '<div style="font-family:var(--font-data);font-size:11px;color:var(--text-dim)">breathable atmospheres &middot; ' + fmt(pSum.planetsWithLiquidWater || 0) + ' with liquid water</div>' +
      '</div>';
    grid.appendChild(hCard);

    body.appendChild(grid);
  });

  // Navigation cards
  const navSection = el('div');
  subHeading(navSection, 'Explore Report');
  const cards = el('div', 'nav-cards');

  const navItems = [
    { key: 'stars', title: 'Stars', desc: 'Star types, activity, binary configs', stat: fmt(S.stars) },
    { key: 'planets', title: 'Planets', desc: 'Types, atmospheres, habitability, geology', stat: fmt(S.planets) },
    { key: 'moons', title: 'Moons', desc: 'Tidal heating, oceans, habitability', stat: fmt(S.moons) },
    { key: 'rings', title: 'Rings', desc: 'Optical depth, color, origins', stat: fmt(S.rings) },
    { key: 'trojans', title: 'Trojans', desc: 'Lagrange points, tiers, per planet type', stat: fmt(S.trojans || 0) },
    { key: 'asteroids', title: 'Notable Asteroids', desc: 'Spectral types, size, density, source breakdown', stat: fmt(S.asteroids) },
    { key: 'belts', title: 'Belts', desc: 'Mass, width, gaps, families', stat: fmt(S.belts) },
    { key: 'climate', title: 'Climate', desc: 'Weather, storms, sky colors', stat: fmt(S.planets) + ' planets' },
    { key: 'stability', title: 'Orbital Stability', desc: 'Gladman delta, crossings, timescales', stat: fmt(stab.totalAdjacentPairs || 0) + ' pairs' },
    { key: 'viewer', title: 'System Viewer', desc: 'Generate & visualize a system', stat: '\u269B' }
  ];

  for (const ni of navItems) {
    const card = el('div', 'nav-card');
    card.innerHTML = '<div class="nav-card-icon">' + (PAGES[ni.key] ? PAGES[ni.key].icon : '') + '</div>' +
      '<div class="nav-card-title">' + esc(ni.title) + '</div>' +
      '<div class="nav-card-desc">' + esc(ni.desc) + '</div>' +
      '<div class="nav-card-stat">' + esc(ni.stat) + '</div>';
    card.addEventListener('click', () => navigateTo(ni.key));
    cards.appendChild(card);
  }
  navSection.appendChild(cards);
  c.appendChild(navSection);
}

/* ═══════════════════════════════════════════════════════════════
   PAGE: Stars
   ═══════════════════════════════════════════════════════════════ */

function renderStars(c) {
  const R = REPORT_DATA;
  const stars = R.stars || {};
  const totalStars = R.summary.stars || 0;
  const totalSystems = R.summary.systems || 1;

  pageHeader(c, 'Stars', 'Star type distributions, activity, and binary configurations');

  statGrid(c, [
    { value: fmt(totalStars), label: 'Total Stars' },
    { value: round2(totalStars / totalSystems), label: 'Avg / System', cls: 'cyan' },
    { value: fmt(Object.keys(stars.types || {}).length), label: 'Unique Types' },
    { value: fmt(Object.keys(stars.binaryConfigurations || {}).length), label: 'Binary Configs' }
  ]);

  twoCol(c,
    function(left) {
      sectionCard(left, 'Star Amounts per System', '#f59e0b', function(body) {
        distTable(body, stars.amounts, totalSystems, 'Star Count', { sortByKey: true });
      });
      sectionCard(left, 'Star Types', '#40d8d8', function(body) {
        distTable(body, stars.types, totalStars, 'Type');
      });
    },
    function(right) {
      sectionCard(right, 'Binary Configurations', '#a78bfa', function(body) {
        distTable(body, stars.binaryConfigurations, totalSystems, 'Configuration');
      });
      sectionCard(right, 'Star Roles', '#c07040', function(body) {
        distTable(body, stars.roles, totalStars, 'Role');
      });
    }
  );

  // Per-type breakdown
  const perType = stars.perType;
  if (perType && Object.keys(perType).length > 0) {
    subHeading(c, 'Per Star Type Breakdown');
    note(c, 'Detailed analysis for each star type — click to expand.');

    for (const [typeName, data] of Object.entries(perType)) {
      sectionCard(c, typeName + ' (' + fmt(data.count || 0) + ')', '#f59e0b', function(body) {
        const count = data.count || 1;
        if (data.activity) {
          collapsible(body, 'Activity Levels', function(cb) { distTable(cb, data.activity, count, 'Activity'); });
        }
        if (data.flareClass) {
          collapsible(body, 'Flare Class', function(cb) { distTable(cb, data.flareClass, count, 'Flare Class'); });
        }
        if (data.spotCoverage) {
          collapsible(body, 'Starspot Coverage', function(cb) { distTable(cb, data.spotCoverage, count, 'Spot %', { sortByKey: true }); });
        }
        if (data.xrayLuminosity) {
          collapsible(body, 'X-ray Luminosity', function(cb) { distTable(cb, data.xrayLuminosity, count, 'Luminosity', { sortByKey: true }); });
        }
        if (data.evolutionaryStage) {
          collapsible(body, 'Evolutionary Stage', function(cb) { distTable(cb, data.evolutionaryStage, count, 'Stage'); });
        }
        if (data.planetsPerSystem) {
          collapsible(body, 'Planets per System', function(cb) { distTable(cb, data.planetsPerSystem, count, 'Planet Count', { sortByKey: true }); });
        }
        if (data.hzInnerAU) {
          collapsible(body, 'HZ Inner Edge (AU)', function(cb) { distTable(cb, data.hzInnerAU, count, 'AU Bin', { sortByKey: true }); });
        }
        if (data.hzOuterAU) {
          collapsible(body, 'HZ Outer Edge (AU)', function(cb) { distTable(cb, data.hzOuterAU, count, 'AU Bin', { sortByKey: true }); });
        }
        if (data.planetFormations) {
          collapsible(body, 'Planet Formations', function(cb) {
            const pf = data.planetFormations;
            note(cb, 'Total planets formed around this star type: ' + fmt(pf.totalPlanets));
            if (pf.planetTypes) {
              const tbl = el('table', 'data-table');
              tbl.innerHTML = '<thead><tr><th>Planet Type</th><th class="num">Count</th><th class="num">%</th><th class="num">Min AU</th><th class="num">Max AU</th></tr></thead>';
              const tbody = el('tbody');
              for (const [pt, pData] of Object.entries(pf.planetTypes)) {
                const tr = el('tr');
                tr.innerHTML = '<td class="key-col">' + esc(pt) + '</td>' +
                  '<td class="num">' + fmt(pData.count) + '</td>' +
                  '<td class="num">' + (pData.percent || 0) + '%</td>' +
                  '<td class="num">' + (pData.minDistanceAU != null ? pData.minDistanceAU : '-') + '</td>' +
                  '<td class="num">' + (pData.maxDistanceAU != null ? pData.maxDistanceAU : '-') + '</td>';
                tbody.appendChild(tr);
              }
              tbl.appendChild(tbody);
              cb.appendChild(tbl);
            }
          });
        }
      }, false);
    }
  }
}
""";

    public static final String JS_PART2 = """
/* ═══════════════════════════════════════════════════════════════
   PAGE: Planets
   ═══════════════════════════════════════════════════════════════ */

function renderPlanets(c) {
  const R = REPORT_DATA;
  const P = R.planets || {};
  const S = P.summary || {};
  const totalPlanets = R.summary.planets || 0;
  const totalSystems = R.summary.systems || 1;

  pageHeader(c, 'Planets', 'Planetary types, composition, atmospheres, habitability, and geology');

  statGrid(c, [
    { value: fmt(totalPlanets), label: 'Total Planets' },
    { value: round2(totalPlanets / totalSystems), label: 'Avg / System', cls: 'cyan' },
    { value: fmt(S.totalRockyPlanets || 0), label: 'Rocky Planets' },
    { value: fmt(S.breathablePlanets || 0), label: 'Breathable', cls: 'safe' },
    { value: fmt(S.planetsWithRings || 0), label: 'With Rings', cls: 'purple' },
    { value: fmt(S.planetsWithTrojans || 0), label: 'With Trojans' },
    { value: S.avgMass != null ? round2(S.avgMass) + ' M\u2295' : '-', label: 'Avg Mass' },
    { value: S.avgRadius != null ? round2(S.avgRadius) + ' R\u2295' : '-', label: 'Avg Radius', cls: 'cyan' },
    { value: S.avgEsi != null ? round2(S.avgEsi) : '-', label: 'Avg ESI', cls: 'safe' }
  ]);

  // Types & Composition
  twoCol(c,
    function(left) {
      sectionCard(left, 'Planet Types', '#f59e0b', function(body) {
        distTable(body, P.types, totalPlanets, 'Type');
      });
      sectionCard(left, 'Surface Temperature', '#ef4444', function(body) {
        distTable(body, P.surfaceTempBins, totalPlanets, 'Temperature', { sortByKey: true, stripPrefixes: true });
      });
    },
    function(right) {
      sectionCard(right, 'Composition Classes', '#40d8d8', function(body) {
        distTable(body, P.compositionClasses, totalPlanets, 'Composition');
      });
      sectionCard(right, 'Habitable Zone Position', '#34d399', function(body) {
        distTable(body, P.habitableZonePositions, totalPlanets, 'Position');
      });
    }
  );

  // Atmosphere & Magnetics
  sectionCard(c, 'Atmosphere & Magnetic Fields', '#60a5fa', function(body) {
    twoCol(body,
      function(left) {
        collapsible(left, 'Atmosphere Classifications', function(cb) {
          distTable(cb, P.atmosphereClassifications, totalPlanets, 'Classification');
        }, true);
        collapsible(left, 'Tidal Locking', function(cb) {
          distTable(cb, P.tidalLocking, totalPlanets, 'Status');
        });
        collapsible(left, 'Auroral Frequencies', function(cb) {
          distTable(cb, P.auroralFrequencies, totalPlanets, 'Frequency');
        });
        collapsible(left, 'Atmosphere Loss Rate', function(cb) {
          distTable(cb, P.atmLossRateBins, totalPlanets, 'Loss Rate', { sortByKey: true, stripPrefixes: true });
        });
      },
      function(right) {
        collapsible(right, 'Magnetic Protection Levels', function(cb) {
          distTable(cb, P.magneticProtectionLevels, totalPlanets, 'Protection');
        }, true);
        collapsible(right, 'Magnetopause Distance', function(cb) {
          distTable(cb, P.magnetopauseBins, totalPlanets, 'Distance', { sortByKey: true, stripPrefixes: true });
        });
        collapsible(right, 'Auroral Intensities', function(cb) {
          distTable(cb, P.auroralIntensities, totalPlanets, 'Intensity');
        });
      }
    );
    // Radiation belts
    if (P.beltIntensityInner || P.beltIntensityOuter) {
      twoCol(body,
        function(left) {
          if (P.beltIntensityInner) {
            collapsible(left, 'Inner Radiation Belt Intensity', function(cb) {
              distTable(cb, P.beltIntensityInner, totalPlanets, 'Intensity');
            });
          }
        },
        function(right) {
          if (P.beltIntensityOuter) {
            collapsible(right, 'Outer Radiation Belt Intensity', function(cb) {
              distTable(cb, P.beltIntensityOuter, totalPlanets, 'Intensity');
            });
          }
        }
      );
    }
    // Cross-reference tables
    const xref = P.crossReference || {};
    if (xref.activityVsAtmosphere) {
      collapsible(body, 'Cross-Reference: Star Activity vs Atmosphere Stripping', function(cb) {
        const tbl = el('table', 'xref-table');
        tbl.innerHTML = '<thead><tr><th>Star Activity</th><th>Rocky Planets</th><th>Stripped (None)</th><th>Strip Rate %</th></tr></thead>';
        const tbody = el('tbody');
        for (const [activity, d] of Object.entries(xref.activityVsAtmosphere)) {
          const tr = el('tr');
          tr.innerHTML = '<td>' + esc(activity) + '</td><td>' + fmt(d.totalRocky) + '</td>' +
            '<td>' + fmt(d.strippedNone) + '</td><td>' + (d.stripRate || 0) + '%</td>';
          tbody.appendChild(tr);
        }
        tbl.appendChild(tbody);
        cb.appendChild(tbl);
      });
    }
    if (xref.activityVsProtection) {
      collapsible(body, 'Cross-Reference: Star Activity vs Protection Level', function(cb) {
        const cols = ['total', 'NONE', 'MINIMAL', 'MODERATE', 'STRONG', 'EXCEPTIONAL'];
        xrefTable(cb, xref.activityVsProtection, 'Star Activity', cols);
      });
    }
    if (xref.distanceVsMagnetopause) {
      collapsible(body, 'Cross-Reference: Distance vs Magnetopause', function(cb) {
        xrefTable(cb, xref.distanceVsMagnetopause, 'Distance Bin', ['avgMagnetopause', 'sampleCount']);
      });
    }
  });

  // Geology
  sectionCard(c, 'Geology', '#c07040', function(body) {
    twoCol(body,
      function(left) {
        distTable(left, P.geologicalActivity, S.planetsWithGeology || totalPlanets, 'Geological Activity');
      },
      function(right) {
        distTable(right, P.tectonicLevels, S.planetsWithGeology || totalPlanets, 'Tectonic Level');
      }
    );
    distTable(body, P.volcanismTypes, S.planetsWithGeology || totalPlanets, 'Volcanism Type');
  });

  // Water & Habitability
  sectionCard(c, 'Water & Habitability', '#34d399', function(body) {
    statGrid(body, [
      { value: fmt(S.planetsWithLiquidWater || 0), label: 'Liquid Water', cls: 'info' },
      { value: fmt(S.planetsWithIce || 0), label: 'With Ice', cls: 'cyan' },
      { value: fmt(S.planetsWithSubsurfaceWater || 0), label: 'Subsurface Water' },
      { value: S.avgHabScore != null ? round2(S.avgHabScore) : '-', label: 'Avg Hab Score', cls: 'safe' }
    ]);
    twoCol(body,
      function(left) {
        collapsible(left, 'Water Inventories', function(cb) {
          distTable(cb, P.waterInventories, totalPlanets, 'Inventory');
        }, true);
        collapsible(left, 'Habitability Classes', function(cb) {
          distTable(cb, P.habitabilityClasses, S.habAssessmentCount || totalPlanets, 'Class');
        });
        collapsible(left, 'Biosignature Potentials', function(cb) {
          distTable(cb, P.biosignaturePotentials, S.habAssessmentCount || totalPlanets, 'Potential');
        });
      },
      function(right) {
        collapsible(right, 'Water Phases', function(cb) {
          distTable(cb, P.waterPhases, totalPlanets, 'Phase');
        }, true);
        collapsible(right, 'Colonization Suitability', function(cb) {
          distTable(cb, P.colonizationSuitabilities, S.habAssessmentCount || totalPlanets, 'Suitability');
        });
        collapsible(right, 'Life Complexity Potentials', function(cb) {
          distTable(cb, P.lifeComplexityPotentials, S.habAssessmentCount || totalPlanets, 'Complexity');
        });
      }
    );
    collapsible(body, 'Terraforming Potentials', function(cb) {
      distTable(cb, P.terraformingPotentials, S.habAssessmentCount || totalPlanets, 'Potential');
    });
  });

  // Per-type breakdown (Single Star)
  renderPlanetPerType(c, P.perType, 'Per Planet Type — Single Star Systems', totalPlanets);
  renderPlanetPerType(c, P.perTypePType, 'Per Planet Type — P-Type Binary Systems', totalPlanets);
  renderPlanetPerType(c, P.perTypeTrinary, 'Per Planet Type — Trinary Systems', totalPlanets);
}

function renderPlanetPerType(c, data, heading, totalPlanets) {
  if (!data || Object.keys(data).length === 0) return;
  subHeading(c, heading);
  note(c, 'Click to expand detailed analysis for each planet type.');

  for (const [typeName, td] of Object.entries(data)) {
    sectionCard(c, typeName + ' (' + fmt(td.count || 0) + ')', '#f59e0b', function(body) {
      const count = td.count || 1;

      // ── Summary stats ──
      const summaryCards = [];
      if (td.averages) {
        summaryCards.push({ value: td.averages.avgMass + ' M\u2295', label: 'Avg Mass' });
        summaryCards.push({ value: td.averages.avgRadius + ' R\u2295', label: 'Avg Radius', cls: 'cyan' });
        summaryCards.push({ value: td.averages.avgGravity + ' g', label: 'Avg Gravity' });
        summaryCards.push({ value: round2(td.averages.avgTemp) + ' K', label: 'Avg Temp', cls: 'cyan' });
      }
      summaryCards.push({ value: fmt(td.tidallyLocked || 0), label: 'Tidally Locked' });
      summaryCards.push({ value: fmt(td.withRings || 0), label: 'With Rings', cls: 'purple' });
      summaryCards.push({ value: fmt(td.withTrojans || 0), label: 'With Trojans' });
      if (summaryCards.length > 0) statGrid(body, summaryCards);

      // ── Placement & Distance ──
      collapsible(body, 'Orbital Placement', function(cb) {
        // Distance statistics summary
        if (td.distanceStats) {
          const ds = td.distanceStats;
          const dsCards = [
            { value: ds.min + ' AU', label: 'Closest' },
            { value: ds.max + ' AU', label: 'Farthest', cls: 'cyan' },
            { value: ds.median + ' AU', label: 'Median' }
          ];
          if (ds.iqrMean != null) {
            dsCards.push({ value: ds.iqrMean + ' AU', label: 'IQR Mean*', cls: 'safe' });
          } else {
            dsCards.push({ value: ds.mean + ' AU', label: 'Mean', cls: 'safe' });
          }
          statGrid(cb, dsCards);
          if (ds.outliersExcluded != null && ds.outliersExcluded > 0) {
            note(cb, '<span style="font-size:10px;color:var(--text-muted)">* IQR Mean excludes ' + ds.outliersExcluded + ' outlier(s) outside Q1\u2013Q3 \u00B1 1.5\u00D7IQR (Q1=' + ds.q1 + ' AU, Q3=' + ds.q3 + ' AU)</span>');
          } else if (ds.iqrMean != null) {
            note(cb, '<span style="font-size:10px;color:var(--text-muted)">* IQR Mean \u2014 no outliers detected (Q1=' + ds.q1 + ' AU, Q3=' + ds.q3 + ' AU)</span>');
          }
        }

        // HZ region distribution
        if (td.hzPositions) distTable(cb, td.hzPositions, count, 'HZ Region');

        // Semi-major axis bin distribution
        if (td.semiMajorAxisAU) distTable(cb, td.semiMajorAxisAU, count, 'Distance (AU)', { sortByKey: true, stripPrefixes: true });

        // Tidal lock by distance
        if (td.tidalLockByDistance) {
          const tbl = el('table', 'data-table');
          tbl.innerHTML = '<thead><tr><th>Distance Bin</th><th class="num">Total</th><th class="num">Locked</th><th class="num">Lock Rate</th></tr></thead>';
          const tbody = el('tbody');
          for (const [bin, vals] of Object.entries(td.tidalLockByDistance).sort((a,b) => a[0].localeCompare(b[0]))) {
            const tr = el('tr');
            tr.innerHTML = '<td class="key-col">' + esc(stripPrefix(bin)) + '</td>' +
              '<td class="num">' + fmt(vals.total) + '</td>' +
              '<td class="num">' + fmt(vals.locked) + '</td>' +
              '<td class="num">' + vals.lockRate + '%</td>';
            tbody.appendChild(tr);
          }
          tbl.appendChild(tbody);
          const h4 = el('h4');
          h4.textContent = 'Tidal Locking by Distance';
          h4.style.cssText = 'font-size:12px;color:var(--text-dim);margin:12px 0 4px';
          cb.appendChild(h4);
          cb.appendChild(tbl);
        }
      }, true);

      // ── Composition & Atmosphere ──
      if (td.compositionClasses) collapsible(body, 'Composition', function(cb) { distTable(cb, td.compositionClasses, count, 'Composition'); });
      if (td.surfaceTempBins) collapsible(body, 'Surface Temperature', function(cb) { distTable(cb, td.surfaceTempBins, count, 'Temperature', { sortByKey: true, stripPrefixes: true }); });
      if (td.atmosphereClasses) collapsible(body, 'Atmosphere', function(cb) { distTable(cb, td.atmosphereClasses, count, 'Classification'); });
      if (td.protectionLevels) collapsible(body, 'Magnetic Protection', function(cb) { distTable(cb, td.protectionLevels, count, 'Protection'); });

      // ── Mass & Moons ──
      if (td.massBins) collapsible(body, 'Mass Distribution', function(cb) { distTable(cb, td.massBins, count, 'Mass Range', { sortByKey: true, stripPrefixes: true }); });
      if (td.moonCountBins) collapsible(body, 'Moon Counts', function(cb) { distTable(cb, td.moonCountBins, count, 'Moon Count', { sortByKey: true }); });
      if (td.moonletBins) collapsible(body, 'Additional Moonlets', function(cb) { distTable(cb, td.moonletBins, count, 'Moonlets', { sortByKey: true }); });

      // ── Habitability & Geology ──
      if (td.waterInventories) collapsible(body, 'Water Inventory', function(cb) { distTable(cb, td.waterInventories, count, 'Inventory'); });
      if (td.habitabilityClasses) collapsible(body, 'Habitability', function(cb) { distTable(cb, td.habitabilityClasses, count, 'Class'); });
      if (td.geologicalActivity) collapsible(body, 'Geological Activity', function(cb) { distTable(cb, td.geologicalActivity, count, 'Activity'); });
    }, false);
  }
}

/* ═══════════════════════════════════════════════════════════════
   PAGE: Moons
   ═══════════════════════════════════════════════════════════════ */

function renderMoons(c) {
  const R = REPORT_DATA;
  const M = R.moons || {};
  const totalMoons = R.summary.moons || 0;

  pageHeader(c, 'Moons', 'Satellite properties, tidal heating, subsurface oceans, and habitability');

  statGrid(c, [
    { value: fmt(totalMoons), label: 'Total Moons' },
    { value: fmt(M.moonsWithSubsurfaceOcean || 0), label: 'Subsurface Oceans', cls: 'info' },
    { value: fmt(M.moonsAssessed || 0), label: 'Assessed' },
    { value: fmt((M.magneticFields || {}).moonsWithMagField || 0), label: 'Magnetic Fields', cls: 'cyan' }
  ]);

  twoCol(c,
    function(left) {
      sectionCard(left, 'Moon Types', '#a78bfa', function(body) {
        distTable(body, M.types, totalMoons, 'Type');
      });
      sectionCard(left, 'Tidal Heating Levels', '#ef4444', function(body) {
        distTable(body, M.tidalHeatingLevels, totalMoons, 'Level');
      });
      sectionCard(left, 'Orbit Distance', '#f59e0b', function(body) {
        distTable(body, M.orbitDistanceBins, totalMoons, 'Distance', { sortByKey: true, stripPrefixes: true });
      });
    },
    function(right) {
      sectionCard(right, 'Composition Types', '#40d8d8', function(body) {
        distTable(body, M.compositionTypes, totalMoons, 'Composition');
      });
      sectionCard(right, 'Geological Activity', '#c07040', function(body) {
        distTable(body, M.geologicalActivity, totalMoons, 'Activity');
      });
      sectionCard(right, 'Eccentricity', '#60a5fa', function(body) {
        distTable(body, M.eccentricityBins, totalMoons, 'Eccentricity', { sortByKey: true, stripPrefixes: true });
      });
    }
  );

  // Tidal heating cross-references
  sectionCard(c, 'Tidal Heating Analysis', '#ef4444', function(body) {
    if (M.tidalHeatingByPlanetType) {
      collapsible(body, 'Tidal Heating by Planet Type', function(cb) {
        stabilityCrossRef(cb, M.tidalHeatingByPlanetType, 'Planet Type');
      }, true);
    }
    if (M.tidalHeatingByMoonType) {
      collapsible(body, 'Tidal Heating by Moon Type', function(cb) {
        stabilityCrossRef(cb, M.tidalHeatingByMoonType, 'Moon Type');
      });
    }
  });

  // Atmosphere
  sectionCard(c, 'Atmosphere Classifications', '#60a5fa', function(body) {
    distTable(body, M.atmosphereClassifications, totalMoons, 'Classification');
  });

  // Magnetic fields
  if (M.magneticFields) {
    sectionCard(c, 'Magnetic Fields', '#40d8d8', function(body) {
      const mf = M.magneticFields;
      twoCol(body,
        function(left) { distTable(left, mf.dynamoTypes, mf.moonsWithMagField || totalMoons, 'Dynamo Type'); },
        function(right) { distTable(right, mf.protectionLevels, totalMoons, 'Protection Level'); }
      );
    });
  }

  // Water
  if (M.water) {
    sectionCard(c, 'Water', '#34d399', function(body) {
      const w = M.water;
      statGrid(body, [
        { value: fmt(w.moonsWithLiquidWater || 0), label: 'Liquid Water', cls: 'info' },
        { value: fmt(w.moonsWithIce || 0), label: 'With Ice', cls: 'cyan' },
        { value: fmt(w.moonsWithSubsurfaceWater || 0), label: 'Subsurface Water' }
      ]);
      distTable(body, w.inventories, totalMoons, 'Inventory');
    });
  }

  // Habitability
  if (M.habitability) {
    sectionCard(c, 'Habitability', '#34d399', function(body) {
      const h = M.habitability;
      statGrid(body, [
        { value: fmt(h.moonHabCount || 0), label: 'Assessed' },
        { value: h.avgEsi != null ? round2(h.avgEsi) : '-', label: 'Avg ESI', cls: 'safe' },
        { value: h.avgHabScore != null ? round2(h.avgHabScore) : '-', label: 'Avg Hab Score', cls: 'cyan' }
      ]);
      twoCol(body,
        function(left) {
          if (h.habitabilityClasses) distTable(left, h.habitabilityClasses, h.moonHabCount || 1, 'Class');
          if (h.biosignaturePotentials) distTable(left, h.biosignaturePotentials, h.moonHabCount || 1, 'Biosignature');
        },
        function(right) {
          if (h.colonizationSuitabilities) distTable(right, h.colonizationSuitabilities, h.moonHabCount || 1, 'Colonization');
          if (h.lifeComplexity) distTable(right, h.lifeComplexity, h.moonHabCount || 1, 'Life Complexity');
        }
      );
      if (h.radiationBeltDose) collapsible(body, 'Radiation Belt Dose', function(cb) { distTable(cb, h.radiationBeltDose, h.moonHabCount || 1, 'Dose'); });
      if (h.tidalContribution) collapsible(body, 'Tidal Contribution', function(cb) { distTable(cb, h.tidalContribution, h.moonHabCount || 1, 'Contribution'); });
    });
  }

  // Climate
  if (M.climate) {
    sectionCard(c, 'Moon Climate', '#60a5fa', function(body) {
      const cl = M.climate;
      statGrid(body, [
        { value: fmt(cl.moonsWithClimate || 0), label: 'With Climate' },
        { value: fmt(cl.moonsWithPrecipitation || 0), label: 'Precipitation', cls: 'info' },
        { value: fmt(cl.moonsWithLightning || 0), label: 'Lightning', cls: 'cyan' },
        { value: fmt(cl.moonExtremeEventTotal || 0), label: 'Extreme Events', cls: 'danger' }
      ]);
      twoCol(body,
        function(left) {
          if (cl.skyColors) distTable(left, cl.skyColors, cl.moonsWithClimate || 1, 'Sky Color');
          if (cl.windIntensity) distTable(left, cl.windIntensity, cl.moonsWithClimate || 1, 'Wind Intensity');
        },
        function(right) {
          if (cl.cloudCoverage) distTable(right, cl.cloudCoverage, cl.moonsWithClimate || 1, 'Cloud Coverage');
          if (cl.severity) distTable(right, cl.severity, cl.moonsWithClimate || 1, 'Severity');
        }
      );
      if (cl.exposureRating) collapsible(body, 'Exposure Rating', function(cb) { distTable(cb, cl.exposureRating, cl.moonsWithClimate || 1, 'Rating'); });
      if (cl.tidalRangeBins) collapsible(body, 'Tidal Range', function(cb) { distTable(cb, cl.tidalRangeBins, cl.moonsWithClimate || 1, 'Range', { sortByKey: true, stripPrefixes: true }); });
    });
  }
}

/* ═══════════════════════════════════════════════════════════════
   PAGE: Rings
   ═══════════════════════════════════════════════════════════════ */

function renderRings(c) {
  const R = REPORT_DATA;
  const rings = R.rings || {};
  const summary = rings.summary || {};
  const totalRings = summary.totalRings || R.summary.rings || 0;

  pageHeader(c, 'Rings', 'Ring types, optical properties, origins, and per-type analysis');

  statGrid(c, [
    { value: fmt(totalRings), label: 'Total Rings' },
    { value: fmt(summary.withShepherdMoons || 0), label: 'Shepherd Moons', cls: 'cyan' },
    { value: fmt(summary.withGaps || 0), label: 'With Gaps' }
  ]);

  twoCol(c,
    function(left) {
      sectionCard(left, 'Ring Types', '#a78bfa', function(body) { distTable(body, rings.types, totalRings, 'Type'); });
      sectionCard(left, 'Optical Depth', '#f59e0b', function(body) { distTable(body, rings.opticalDepthBins, totalRings, 'Depth', { sortByKey: true, stripPrefixes: true }); });
      sectionCard(left, 'Visibility', '#40d8d8', function(body) { distTable(body, rings.visibilityDistribution, totalRings, 'Visibility'); });
      sectionCard(left, 'Thickness', '#c07040', function(body) { distTable(body, rings.thicknessBins, totalRings, 'Thickness', { sortByKey: true, stripPrefixes: true }); });
    },
    function(right) {
      sectionCard(right, 'Color Distribution', '#60a5fa', function(body) { distTable(body, rings.colorDistribution, totalRings, 'Color'); });
      sectionCard(right, 'Stability', '#34d399', function(body) { distTable(body, rings.stabilityDistribution, totalRings, 'Stability'); });
      sectionCard(right, 'Origin Types', '#ef4444', function(body) { distTable(body, rings.originTypes, totalRings, 'Origin'); });
      sectionCard(right, 'Particle Size', '#a78bfa', function(body) { distTable(body, rings.particleSizeBins, totalRings, 'Size', { sortByKey: true, stripPrefixes: true }); });
    }
  );

  if (rings.ageBins) {
    sectionCard(c, 'Ring Age Distribution', '#f59e0b', function(body) {
      distTable(body, rings.ageBins, totalRings, 'Age', { sortByKey: true, stripPrefixes: true });
    });
  }
  if (rings.parentPlanetTypes) {
    sectionCard(c, 'Parent Planet Types', '#40d8d8', function(body) {
      distTable(body, rings.parentPlanetTypes, totalRings, 'Planet Type');
    });
  }

  // Per-type breakdown
  if (rings.perType && Object.keys(rings.perType).length > 0) {
    subHeading(c, 'Per Ring Type Breakdown');
    for (const [typeName, td] of Object.entries(rings.perType)) {
      sectionCard(c, typeName + ' (' + fmt(td.count || 0) + ')', '#a78bfa', function(body) {
        const count = td.count || 1;
        if (td.colors) distTable(body, td.colors, count, 'Color');
        if (td.visibilities) distTable(body, td.visibilities, count, 'Visibility');
        if (td.stabilities) distTable(body, td.stabilities, count, 'Stability');
        if (td.origins) distTable(body, td.origins, count, 'Origin');
        if (td.avgOpticalDepth != null) note(body, 'Avg optical depth: <span class="readout readout-cyan">' + round2(td.avgOpticalDepth) + '</span>');
        if (td.avgThickness != null) note(body, 'Avg thickness: <span class="readout readout-amber">' + round2(td.avgThickness) + ' km</span>');
        if (td.shepherdCount != null) note(body, 'With shepherd moons: ' + fmt(td.shepherdCount));
        if (td.gapsCount != null) note(body, 'With gaps: ' + fmt(td.gapsCount));
      }, false);
    }
  }
}

/* ═══════════════════════════════════════════════════════════════
   PAGE: Trojans
   ═══════════════════════════════════════════════════════════════ */

function renderTrojans(c) {
  const R = REPORT_DATA;
  const trojans = R.trojans || {};
  const summary = trojans.summary || {};
  const totalTrojans = summary.totalTrojanSwarms || R.summary.trojans || 0;

  pageHeader(c, 'Trojan Swarms', 'Lagrange point swarms: mass, tiers, object counts, and per planet type analysis');

  statGrid(c, [
    { value: fmt(totalTrojans), label: 'Total Swarms' },
    { value: fmt(summary.planetsWithTrojans || 0), label: 'Planets With Trojans', cls: 'cyan' },
    { value: fmt(summary.trojansWithNotableAsteroids || 0), label: 'With Asteroids' },
    { value: fmt(summary.totalNotableAsteroids || 0), label: 'Notable Asteroids', cls: 'purple' },
    { value: fmt(summary.trojansWithMoons || 0), label: 'With Trojan Moons', cls: 'cyan' },
    { value: summary.avgMassEarth != null ? summary.avgMassEarth.toExponential(2) + ' M\u2295' : '-', label: 'Avg Mass' },
    { value: fmt(summary.avgObjectCount || 0), label: 'Avg Objects' },
    { value: summary.avgLibrationAmplitudeDeg != null ? round2(summary.avgLibrationAmplitudeDeg) + '\u00B0' : '-', label: 'Avg Libration', cls: 'cyan' }
  ]);

  // Main distribution grids
  twoCol(c,
    function(left) {
      sectionCard(left, 'Lagrange Points', '#f59e0b', function(body) { distTable(body, trojans.lagrangePoints, totalTrojans, 'Point'); });
      sectionCard(left, 'Tier Classification', '#a78bfa', function(body) { distTable(body, trojans.tierClassification, totalTrojans, 'Tier'); });
      sectionCard(left, 'Mass Distribution', '#ef4444', function(body) { distTable(body, trojans.massBins, totalTrojans, 'Mass', { sortByKey: true, stripPrefixes: true }); });
      sectionCard(left, 'Swarm Width', '#40d8d8', function(body) { distTable(body, trojans.widthBins, totalTrojans, 'Width', { sortByKey: true, stripPrefixes: true }); });
    },
    function(right) {
      sectionCard(right, 'Parent Planet Types', '#60a5fa', function(body) { distTable(body, trojans.parentPlanetTypes, totalTrojans, 'Planet Type'); });
      sectionCard(right, 'Object Count', '#34d399', function(body) { distTable(body, trojans.objectCountBins, totalTrojans, 'Count', { sortByKey: true, stripPrefixes: true }); });
      sectionCard(right, 'Libration Amplitude', '#c07040', function(body) { distTable(body, trojans.librationAmplitudeBins, totalTrojans, 'Amplitude', { sortByKey: true, stripPrefixes: true }); });
    }
  );

  // Planet type summary table
  if (trojans.planetTypeSummary && Object.keys(trojans.planetTypeSummary).length > 0) {
    sectionCard(c, 'Trojan Formation by Planet Type', '#60a5fa', function(body) {
      var tbl = el('table', 'xref-table');
      tbl.innerHTML = '<thead><tr>' +
        '<th>Planet Type</th><th>Planets</th><th style="min-width:130px">With Swarms</th>' +
        '<th>Swarms</th><th style="min-width:130px">With Asteroids</th>' +
        '<th style="min-width:130px">With Moons</th><th>Total Objects</th>' +
        '</tr></thead>';
      var tbody = el('tbody');
      var sorted = Object.entries(trojans.planetTypeSummary)
        .sort(function(a, b) { return (b[1].pctWithSwarms || 0) - (a[1].pctWithSwarms || 0); });
      for (var si = 0; si < sorted.length; si++) {
        var typeName = sorted[si][0];
        var row = sorted[si][1];
        var tr = el('tr');
        tr.innerHTML = '<td>' + esc(typeName) + '</td>' +
          '<td>' + fmt(row.totalPlanets) + '</td>' +
          '<td>' + pctBar(row.pctWithSwarms) + '</td>' +
          '<td>' + fmt(row.swarmCount || 0) + '</td>' +
          '<td>' + pctBar(row.pctSwarmsWithAsteroids) + '</td>' +
          '<td>' + pctBar(row.pctPlanetsWithMoons || 0) + '</td>' +
          '<td>' + fmtShort(row.totalObjectCount || 0) + '</td>';
        tbody.appendChild(tr);
      }
      tbl.appendChild(tbody);
      body.appendChild(tbl);
    });
  }

  // Per planet type breakdown
  if (trojans.perPlanetType && Object.keys(trojans.perPlanetType).length > 0) {
    sectionCard(c, 'Per Planet Type Breakdown', '#a78bfa', function(body) {
      for (const [typeName, td] of Object.entries(trojans.perPlanetType)) {
        const count = td.swarmCount || 0;
        collapsible(body, typeName + ' (' + fmt(count) + ' swarms)', function(inner) {
          const cards = [];
          cards.push({ value: fmt(count), label: 'Swarms' });
          if (td.avgMassEarth != null) cards.push({ value: td.avgMassEarth.toExponential(2) + ' M\u2295', label: 'Avg Mass', cls: 'cyan' });
          if (td.avgObjectCount != null) cards.push({ value: fmt(td.avgObjectCount), label: 'Avg Objects' });
          cards.push({ value: fmt(td.withNotableAsteroids || 0), label: 'With Asteroids', cls: 'purple' });
          if (cards.length > 0) statGrid(inner, cards);
          if (td.lagrangePoints) distTable(inner, td.lagrangePoints, count, 'Lagrange Point');
          if (td.tiers) distTable(inner, td.tiers, count, 'Tier');
        }, false);
      }
    });
  }
}

/* ═══════════════════════════════════════════════════════════════
   PAGE: Notable Asteroids
   ═══════════════════════════════════════════════════════════════ */

function renderNotableAsteroids(c) {
  const R = REPORT_DATA;
  const ast = R.notableAsteroids || {};
  const summary = ast.summary || {};
  const total = summary.totalNotableAsteroids || 0;

  pageHeader(c, 'Notable Asteroids', 'Unified notable asteroid statistics from belts and trojan swarms');

  statGrid(c, [
    { value: fmt(total), label: 'Total Notable' },
    { value: fmt(summary.fromBelts || 0), label: 'From Belts', cls: 'cyan' },
    { value: fmt(summary.fromTrojans || 0), label: 'From Trojans', cls: 'purple' },
    { value: fmt(summary.differentiated || 0), label: 'Differentiated', cls: 'cyan' },
    { value: fmt(summary.withRegolith || 0), label: 'With Regolith' },
    { value: summary.avgDiameterKm != null ? round2(summary.avgDiameterKm) + ' km' : '-', label: 'Avg Diameter', cls: 'purple' },
    { value: summary.avgDensity != null ? round2(summary.avgDensity) + ' g/cm\u00B3' : '-', label: 'Avg Density' }
  ]);

  if (total > 0) {
    const grid = el('div', 'two-col');
    const left = el('div');
    const right = el('div');
    grid.appendChild(left);
    grid.appendChild(right);

    sectionCard(left, 'Spectral Type Distribution', '#f59e0b', function(body) { distTable(body, ast.spectralTypes, total, 'Type'); });
    sectionCard(left, 'Diameter Distribution', '#60a5fa', function(body) { distTable(body, ast.diameterBins, total, 'Diameter', { sortByKey: true, stripPrefixes: true }); });
    sectionCard(left, 'Density Distribution', '#a78bfa', function(body) { distTable(body, ast.densityBins, total, 'Density', { sortByKey: true, stripPrefixes: true }); });
    sectionCard(left, 'Cratering Levels', '#ef4444', function(body) { distTable(body, ast.crateringLevels, total, 'Level'); });

    sectionCard(right, 'Source Breakdown', '#34d399', function(body) { distTable(body, ast.sourceCounts, total, 'Source'); });
    sectionCard(right, 'Mass Distribution', '#40d8d8', function(body) { distTable(body, ast.massBins, total, 'Mass', { sortByKey: true, stripPrefixes: true }); });
    sectionCard(right, 'Albedo Distribution', '#c07040', function(body) { distTable(body, ast.albedoBins, total, 'Albedo', { sortByKey: true, stripPrefixes: true }); });
    sectionCard(right, 'Orbital Distance', '#f59e0b', function(body) { distTable(body, ast.orbitalDistanceBins, total, 'Distance', { sortByKey: true, stripPrefixes: true }); });

    c.appendChild(grid);
  }

  // Per spectral-type breakdown
  if (ast.perSpectralType && Object.keys(ast.perSpectralType).length > 0) {
    subHeading(c, 'Per Spectral Type Breakdown');
    const sorted = Object.entries(ast.perSpectralType).sort((a, b) => (b[1].count || 0) - (a[1].count || 0));
    for (const [typeName, td] of sorted) {
      const count = td.count || 0;
      collapsible(c, typeName + ' (' + fmt(count) + ' asteroids)', function(inner) {
        const cards = [];
        cards.push({ value: fmt(count), label: 'Count' });
        if (td.avgDiameterKm != null) cards.push({ value: round2(td.avgDiameterKm) + ' km', label: 'Avg Diameter', cls: 'cyan' });
        if (td.avgDensity != null) cards.push({ value: round2(td.avgDensity) + ' g/cm\u00B3', label: 'Avg Density' });
        if (td.avgAlbedo != null) cards.push({ value: td.avgAlbedo.toFixed(3), label: 'Avg Albedo', cls: 'purple' });
        cards.push({ value: fmt(td.differentiated || 0), label: 'Differentiated' });
        if (cards.length > 0) statGrid(inner, cards);
        if (td.sources) distTable(inner, td.sources, count, 'Source');
      }, false);
    }
  }
}

/* ═══════════════════════════════════════════════════════════════
   PAGE: Belts
   ═══════════════════════════════════════════════════════════════ */

function renderBelts(c) {
  const R = REPORT_DATA;
  const belts = R.belts || {};
  const summary = belts.summary || {};
  const totalBelts = summary.totalBelts || R.summary.belts || 0;

  pageHeader(c, 'Belts', 'Asteroid belt types, mass, width, composition, and formation features');

  statGrid(c, [
    { value: fmt(totalBelts), label: 'Total Belts' },
    { value: summary.avgMassEarth != null ? round2(summary.avgMassEarth) + ' M\u2295' : '-', label: 'Avg Mass' },
    { value: summary.avgWidthAU != null ? round2(summary.avgWidthAU) + ' AU' : '-', label: 'Avg Width', cls: 'cyan' },
    { value: fmt(summary.dwarfPlanetsInBelts || 0), label: 'Dwarf Planets', cls: 'purple' },
    { value: fmt(summary.withGaps || 0), label: 'With Gaps' },
    { value: fmt(summary.withCollisionalFamilies || 0), label: 'Collisional Families' },
    { value: fmt(summary.withDwarfPlanets || 0), label: 'Belts w/ Dwarfs', cls: 'cyan' }
  ]);

  twoCol(c,
    function(left) {
      sectionCard(left, 'Belt Types', '#f59e0b', function(body) { distTable(body, belts.types, totalBelts, 'Type'); });
      sectionCard(left, 'Composition Types', '#c07040', function(body) { distTable(body, belts.compositionTypes, totalBelts, 'Composition'); });
      sectionCard(left, 'Width Distribution', '#40d8d8', function(body) { distTable(body, belts.widthBins, totalBelts, 'Width', { sortByKey: true, stripPrefixes: true }); });
      sectionCard(left, 'Mass Distribution', '#a78bfa', function(body) { distTable(body, belts.massBins, totalBelts, 'Mass', { sortByKey: true, stripPrefixes: true }); });
      sectionCard(left, 'Eccentricity', '#ef4444', function(body) { distTable(body, belts.eccentricityBins, totalBelts, 'Eccentricity', { sortByKey: true, stripPrefixes: true }); });
    },
    function(right) {
      sectionCard(right, 'Asteroid Types', '#60a5fa', function(body) { distTable(body, belts.asteroidTypes, totalBelts, 'Type'); });
      sectionCard(right, 'Inner Edge Distance', '#34d399', function(body) { distTable(body, belts.innerEdgeBins, totalBelts, 'Distance', { sortByKey: true, stripPrefixes: true }); });
      sectionCard(right, 'Outer Edge Distance', '#34d399', function(body) { distTable(body, belts.outerEdgeBins, totalBelts, 'Distance', { sortByKey: true, stripPrefixes: true }); });
      sectionCard(right, 'Inclination', '#f59e0b', function(body) { distTable(body, belts.inclinationBins, totalBelts, 'Inclination', { sortByKey: true, stripPrefixes: true }); });
      sectionCard(right, 'Object Count', '#40d8d8', function(body) { distTable(body, belts.objectCountBins, totalBelts, 'Count', { sortByKey: true, stripPrefixes: true }); });
    }
  );

  if (belts.parentStarTypes) {
    sectionCard(c, 'Parent Star Types', '#f59e0b', function(body) { distTable(body, belts.parentStarTypes, totalBelts, 'Star Type'); });
  }
  if (belts.binaryConfigBelts) {
    sectionCard(c, 'Belt Binary Configurations', '#a78bfa', function(body) { distTable(body, belts.binaryConfigBelts, totalBelts, 'Configuration'); });
  }

  // Per-type breakdown
  if (belts.perType && Object.keys(belts.perType).length > 0) {
    subHeading(c, 'Per Belt Type Breakdown');
    for (const [typeName, td] of Object.entries(belts.perType)) {
      sectionCard(c, typeName + ' (' + fmt(td.count || 0) + ')', '#c07040', function(body) {
        const count = td.count || 1;
        if (td.compositionTypes) distTable(body, td.compositionTypes, count, 'Composition');
        if (td.avgWidthAU != null) note(body, 'Avg width: <span class="readout readout-cyan">' + round2(td.avgWidthAU) + ' AU</span>');
        if (td.avgMassEarth != null) note(body, 'Avg mass: <span class="readout readout-amber">' + round2(td.avgMassEarth) + ' M\u2295</span>');
        if (td.avgInnerEdgeAU != null) note(body, 'Avg inner edge: <span class="readout">' + round2(td.avgInnerEdgeAU) + ' AU</span>');
        if (td.avgOuterEdgeAU != null) note(body, 'Avg outer edge: <span class="readout">' + round2(td.avgOuterEdgeAU) + ' AU</span>');
        if (td.avgEccentricity != null) note(body, 'Avg eccentricity: <span class="readout">' + td.avgEccentricity + '</span>');
        if (td.avgInclinationDeg != null) note(body, 'Avg inclination: <span class="readout">' + round2(td.avgInclinationDeg) + '\u00B0</span>');
        if (td.avgObjectCount != null) note(body, 'Avg object count: <span class="readout">' + fmt(td.avgObjectCount) + '</span>');
        if (td.withGaps != null) note(body, 'With gaps: ' + fmt(td.withGaps) + ' / ' + fmt(count));
        if (td.withCollisionalFamilies != null) note(body, 'With collisional families: ' + fmt(td.withCollisionalFamilies));
        note(body, 'Dwarf planets found: <span class="readout readout-purple">' + fmt(td.totalDwarfPlanets || 0) + '</span> (in ' + fmt(td.withDwarfPlanets || 0) + ' belts)');
      }, false);
    }
  }
}

/* ═══════════════════════════════════════════════════════════════
   PAGE: Climate
   ═══════════════════════════════════════════════════════════════ */

function renderClimate(c) {
  const R = REPORT_DATA;
  const climate = R.climate || {};

  pageHeader(c, 'Climate', 'Weather patterns, storm systems, sky colors, and atmospheric phenomena');

  renderWeatherBucket(c, climate.all, 'All Planets');
  renderWeatherBucket(c, climate.surface, 'Surface Planets (Rocky/Terrestrial)');
  renderWeatherBucket(c, climate.gasIce, 'Gas & Ice Giants');
}

function renderWeatherBucket(c, bucket, title) {
  if (!bucket || !bucket.count) return;
  const count = bucket.count;
  const stats = bucket.stats || {};

  sectionCard(c, title + ' (' + fmt(count) + ' planets)', '#60a5fa', function(body) {
    statGrid(body, [
      { value: fmt(count), label: 'Planets' },
      { value: fmt(stats.withPrecipitation || 0), label: 'Precipitation', cls: 'info' },
      { value: fmt(stats.withLightning || 0), label: 'Lightning', cls: 'cyan' },
      { value: fmt(stats.withDustStorms || 0), label: 'Dust Storms' },
      { value: fmt(stats.withSuperRotation || 0), label: 'Super Rotation', cls: 'purple' },
      { value: fmt(stats.extremeEventTotal || 0), label: 'Extreme Events', cls: 'danger' },
      { value: stats.avgCloudCoverage != null ? round2(stats.avgCloudCoverage) + '%' : '-', label: 'Avg Cloud Cover' },
      { value: stats.avgWindSpeed != null ? round2(stats.avgWindSpeed) + ' m/s' : '-', label: 'Avg Wind Speed', cls: 'cyan' }
    ]);

    twoCol(body,
      function(left) {
        if (bucket.skyColors) collapsible(left, 'Sky Colors', function(cb) { distTable(cb, bucket.skyColors, count, 'Color'); }, true);
        if (bucket.windIntensity) collapsible(left, 'Wind Intensity', function(cb) { distTable(cb, bucket.windIntensity, count, 'Intensity'); });
        if (bucket.stormFrequency) collapsible(left, 'Storm Frequency', function(cb) { distTable(cb, bucket.stormFrequency, count, 'Frequency'); });
        if (bucket.lightningType) collapsible(left, 'Lightning Types', function(cb) { distTable(cb, bucket.lightningType, count, 'Type'); });
      },
      function(right) {
        if (bucket.cloudCoverageClass) collapsible(right, 'Cloud Coverage Class', function(cb) { distTable(cb, bucket.cloudCoverageClass, count, 'Class'); }, true);
        if (bucket.severity) collapsible(right, 'Weather Severity', function(cb) { distTable(cb, bucket.severity, count, 'Severity'); });
        if (bucket.exposureRating) collapsible(right, 'Exposure Rating', function(cb) { distTable(cb, bucket.exposureRating, count, 'Rating'); });
        if (bucket.circulationPattern) collapsible(right, 'Circulation Pattern', function(cb) { distTable(cb, bucket.circulationPattern, count, 'Pattern'); });
      }
    );
    if (bucket.tidalRangeBins) {
      collapsible(body, 'Tidal Range', function(cb) { distTable(cb, bucket.tidalRangeBins, count, 'Range', { sortByKey: true, stripPrefixes: true }); });
    }
  });
}

/* ═══════════════════════════════════════════════════════════════
   PAGE: Orbital Stability
   ═══════════════════════════════════════════════════════════════ */

function renderStability(c) {
  const R = REPORT_DATA;
  const OS = R.orbitalStability || {};
  const S = OS.summary || {};

  pageHeader(c, 'Orbital Stability', 'Gladman stability analysis, orbit crossings, timescales, and belt interactions');

  statGrid(c, [
    { value: fmt(S.totalAdjacentPairs || 0), label: 'Adjacent Pairs' },
    { value: fmt(S.multiPlanetSystems || 0), label: 'Multi-Planet Sys', cls: 'cyan' },
    { value: pct(S.systemsAllStable || 0, S.multiPlanetSystems || 1) + '%', label: 'All Stable', cls: 'safe' },
    { value: fmt(S.crossingPairCount || 0), label: 'Crossing Pairs', cls: 'danger' },
    { value: S.meanGladmanDelta != null ? round2(S.meanGladmanDelta) : '-', label: 'Mean \u0394 Gladman' },
    { value: S.medianGladmanDelta != null ? round2(S.medianGladmanDelta) : '-', label: 'Median \u0394 Gladman', cls: 'cyan' },
    { value: fmt(S.doomedButAliveCount || 0), label: 'Doomed-but-Alive', cls: 'danger' },
    { value: S.gladmanFloorOverridePercent != null ? round2(S.gladmanFloorOverridePercent) + '%' : '-', label: 'Floor Override %' }
  ]);

  // Stability classification
  sectionCard(c, 'Stability Classification', '#34d399', function(body) {
    distTable(body, OS.stabilityClassification, S.totalAdjacentPairs || 0, 'Classification');
  });

  // Cross-reference tables
  sectionCard(c, 'Stability Cross-References', '#40d8d8', function(body) {
    if (OS.stabilityByStarType) collapsible(body, 'By Star Type', function(cb) { stabilityCrossRef(cb, OS.stabilityByStarType, 'Star Type'); }, true);
    if (OS.stabilityByOrbitalPosition) collapsible(body, 'By Orbital Position', function(cb) { stabilityCrossRef(cb, OS.stabilityByOrbitalPosition, 'Position'); });
    if (OS.stabilityByBinaryConfig) collapsible(body, 'By Binary Configuration', function(cb) { stabilityCrossRef(cb, OS.stabilityByBinaryConfig, 'Configuration'); });
  });

  twoCol(c,
    function(left) {
      // Gladman delta
      if (OS.gladmanDelta) {
        sectionCard(left, 'Gladman \u0394 Distribution', '#f59e0b', function(body) {
          distTable(body, OS.gladmanDelta.deltaBins, S.totalAdjacentPairs || 0, '\u0394 Bin', { sortByKey: true, stripPrefixes: true });
          if (OS.gladmanDelta.systemsWithAnyPairBelowCritical != null) {
            note(body, 'Systems with any pair below critical: <span class="readout readout-danger">' + fmt(OS.gladmanDelta.systemsWithAnyPairBelowCritical) + '</span>');
          }
        });
      }
      // Eccentricity
      if (OS.eccentricity) {
        sectionCard(left, 'Eccentricity', '#a78bfa', function(body) {
          distTable(body, OS.eccentricity.eccentricityBins, S.totalAdjacentPairs || 0, 'Eccentricity', { sortByKey: true, stripPrefixes: true });
          if (OS.eccentricity.eccentricityByPosition) {
            collapsible(body, 'By Orbital Position', function(cb) {
              xrefTable(cb, OS.eccentricity.eccentricityByPosition, 'Position', ['meanEccentricity', 'sampleCount']);
            });
          }
        });
      }
      // Spacing
      if (OS.spacing) {
        sectionCard(left, 'SMA Ratio & Spacing', '#c07040', function(body) {
          distTable(body, OS.spacing.smaRatioBins, OS.spacing.totalPairsChecked || S.totalAdjacentPairs || 0, 'SMA Ratio', { sortByKey: true, stripPrefixes: true });
          if (OS.spacing.pairsNeedingWiderSpacing != null) {
            note(body, 'Pairs needing wider spacing: <span class="readout readout-amber">' + fmt(OS.spacing.pairsNeedingWiderSpacing) + '</span>');
          }
        });
      }
    },
    function(right) {
      // Orbit crossing
      if (OS.orbitCrossing) {
        sectionCard(right, 'Orbit Crossings', '#ef4444', function(body) {
          note(body, 'Total crossing pairs: <span class="readout readout-danger">' + fmt(OS.orbitCrossing.crossingPairCount || 0) + '</span>');
          if (OS.orbitCrossing.crossingByPlanetTypePair) {
            distTable(body, OS.orbitCrossing.crossingByPlanetTypePair, OS.orbitCrossing.crossingPairCount || 0, 'Type Pair');
          }
        });
      }
      // Timescale
      if (OS.timescale) {
        sectionCard(right, 'Stability Timescales', '#60a5fa', function(body) {
          distTable(body, OS.timescale.timescaleBins, S.totalAdjacentPairs || 0, 'Timescale', { sortByKey: true, stripPrefixes: true });
          if (OS.timescale.timescaleVsAge) {
            collapsible(body, 'Timescale vs System Age', function(cb) {
              distTable(cb, OS.timescale.timescaleVsAge, S.totalAdjacentPairs || 0, 'Category');
            });
          }
        });
      }
      // System-level
      if (OS.systemLevel) {
        sectionCard(right, 'System-Level Metrics', '#f59e0b', function(body) {
          if (OS.systemLevel.planetsPerSystem) distTable(body, OS.systemLevel.planetsPerSystem, S.multiPlanetSystems || R.summary.systems || 0, 'Planets / System', { sortByKey: true, stripPrefixes: true });
          if (OS.systemLevel.systemOuterExtent) distTable(body, OS.systemLevel.systemOuterExtent, R.summary.systems || 0, 'Outer Extent', { sortByKey: true, stripPrefixes: true });
        });
      }
    }
  );

  // Belt stability
  if (OS.beltStability) {
    const BS = OS.beltStability;
    sectionCard(c, 'Belt Stability', '#c07040', function(body) {
      statGrid(body, [
        { value: fmt(BS.totalBeltsAnalyzed || 0), label: 'Belts Analyzed' },
        { value: fmt(BS.beltOverlapCount || 0), label: 'Belt-Belt Overlaps', cls: (BS.beltOverlapCount > 0 ? 'danger' : '') },
        { value: fmt(BS.beltPlanetOverlapCount || 0), label: 'Belt-Planet Overlaps', cls: (BS.beltPlanetOverlapCount > 0 ? 'danger' : '') },
        { value: fmt(BS.beltsExceedingStabilityLimit || 0), label: 'Exceed S-Type Limit', cls: (BS.beltsExceedingStabilityLimit > 0 ? 'danger' : '') },
        { value: fmt(BS.beltsBelowCavityLimit || 0), label: 'Below P-Type Cavity', cls: (BS.beltsBelowCavityLimit > 0 ? 'danger' : '') }
      ]);
      if (BS.beltOverlapDetails && BS.beltOverlapDetails.length > 0) {
        collapsible(body, 'Belt-Belt Overlap Details', function(cb) { detailList(cb, BS.beltOverlapDetails); });
      }
      if (BS.beltPlanetOverlapDetails && BS.beltPlanetOverlapDetails.length > 0) {
        collapsible(body, 'Belt-Planet Overlap Details', function(cb) { detailList(cb, BS.beltPlanetOverlapDetails); });
      }
    });
  }
}

/* ═══════════════════════════════════════════════════════════════
   PAGE: System Viewer (placeholder — full orbital sim in Phase 5)
   ═══════════════════════════════════════════════════════════════ */

function renderViewer(c) {
  pageHeader(c, 'System Viewer', 'Generate a star system and visualize its orbital mechanics');

  const header = el('div', 'viewer-header');
  const btn = el('button', 'btn-generate');
  btn.textContent = 'Generate System';
  btn.id = 'btnGenerate';
  header.appendChild(btn);
  const status = el('span', 'viewer-status');
  status.id = 'viewerStatus';
  header.appendChild(status);
  c.appendChild(header);

  const body = el('div', 'viewer-body');
  const canvasArea = el('div', 'viewer-canvas-area');
  canvasArea.id = 'viewerCanvasContainer';
  const empty = el('div', 'viewer-empty');
  empty.innerHTML = 'Click <strong>Generate System</strong> to create a new<br>star system and visualize its orbits.<br><br>' +
    '<span style="font-size:11px;color:var(--text-muted)">Requires a running server at the current host.</span>';
  canvasArea.appendChild(empty);
  body.appendChild(canvasArea);

  const sidebar = el('div', 'viewer-sidebar');
  sidebar.id = 'viewerSidebar';
  body.appendChild(sidebar);

  c.appendChild(body);

  btn.addEventListener('click', async function() {
    btn.disabled = true;
    status.textContent = 'Generating...';
    status.className = 'viewer-status';
    try {
      const resp = await fetch('/api/v1/solarsystem');
      if (!resp.ok) throw new Error('Server returned ' + resp.status);
      const system = await resp.json();
      status.textContent = (system.designation ? system.designation.catalogId || '' : '') || 'System generated';
      if (typeof initOrbitalSim === 'function') {
        initOrbitalSim(system, canvasArea, sidebar);
      } else {
        canvasArea.innerHTML = '<div class="viewer-empty">Orbital simulation module not loaded.<br>System data received successfully.</div>';
        sidebar.innerHTML = '<pre style="font-size:10px;color:var(--text-dim);white-space:pre-wrap">' + JSON.stringify(system, null, 2).substring(0, 2000) + '...</pre>';
      }
    } catch (e) {
      status.textContent = 'Error: ' + e.message;
      status.className = 'viewer-status error';
    }
    btn.disabled = false;
  });
}
""";
}
