package com.brickroad.starcreator_webservice.probabilityreport;

/**
 * Orbital simulation JavaScript extracted and adapted from OrbitalAnalysisHtmlGenerator.
 * <p>
 * Provides {@code initOrbitalSim(systemJson, canvasContainer, sidebar)} which:
 * <ul>
 *   <li>Parses a StarSystem JSON response from the API</li>
 *   <li>Builds canvas panels with Keplerian orbital animation</li>
 *   <li>Populates a sidebar with planet lists, stability analysis, and belt info</li>
 * </ul>
 */
public final class OrbitalSimJsTemplate {

    private OrbitalSimJsTemplate() {}

    public static final String JS = """
'use strict';

/* ═══════════════════════════════════════════════════════════════
   Orbital Simulation Module — adapted from OrbitalAnalysisHtmlGenerator
   ═══════════════════════════════════════════════════════════════ */

let _orbSim = null; // current simulation state

function initOrbitalSim(systemJson, canvasContainer, sidebar) {
  // Clean up previous simulation
  if (_orbSim && _orbSim.animId) cancelAnimationFrame(_orbSim.animId);
  _orbSim = null;
  canvasContainer.innerHTML = '';
  sidebar.innerHTML = '';

  const starMap = buildStarMapFromApi(systemJson);
  const starKeys = Object.keys(starMap);
  if (starKeys.length === 0) {
    canvasContainer.innerHTML = '<div class="viewer-empty">No stars found in system.</div>';
    return;
  }

  // Precompute derived values
  let globalMinPeriod = Infinity;
  for (const key of starKeys) {
    const sys = starMap[key];
    for (const p of sys.planets) {
      p.peri = p.sma * (1 - p.ecc);
      p.apo = p.sma * (1 + p.ecc);
      p.hill = p.sma * Math.pow(p.mass / (3 * sys.starMass * 333000), 1/3);
      p.phase = Math.random() * Math.PI * 2;
      p.trail = [];
      p.renderSize = Math.max(2.5, Math.min(7, 2 + Math.log10(Math.max(0.01, p.mass)) * 2));
      if (p.period < globalMinPeriod && p.period > 0) globalMinPeriod = p.period;
    }
  }
  if (!isFinite(globalMinPeriod)) globalMinPeriod = 365.25;

  // Build canvas panels
  const panelContainer = document.createElement('div');
  panelContainer.style.cssText = 'display:flex;flex:1;overflow:hidden';
  canvasContainer.appendChild(panelContainer);

  // Controls bar
  const controls = document.createElement('div');
  controls.className = 'viewer-controls';
  controls.innerHTML =
    '<button id="orbBtnPlay" class="active">\u25B6 Running</button>' +
    '<button id="orbBtnTrails">Trails</button>' +
    '<button id="orbBtnTop" class="active">Top</button>' +
    '<button id="orbBtnSide">Side</button>' +
    '<span style="margin-left:8px">Speed</span>' +
    '<input type="range" id="orbSpeed" min="0" max="5" step="0.1" value="2">' +
    '<span id="orbSpeedLabel">\u00d71.00</span>';
  canvasContainer.appendChild(controls);

  const canvasData = {};
  let idx = 0;
  for (const key of starKeys) {
    const sys = starMap[key];
    const view = document.createElement('div');
    view.style.cssText = 'flex:1;position:relative;' + (idx < starKeys.length-1 ? 'border-right:1px solid #1a1f2a' : '');

    const canvas = document.createElement('canvas');
    canvas.style.cssText = 'width:100%;height:100%;display:block';
    view.appendChild(canvas);

    // Star label
    const label = document.createElement('div');
    label.style.cssText = "position:absolute;top:10px;left:14px;font-family:'JetBrains Mono',monospace;font-size:11px;font-weight:500;display:flex;align-items:center;gap:6px;pointer-events:none";
    const starColors = starKeys.length === 1 ? ['#f0c850'] : ['#6ea8fe','#ffa94d','#ff6b9d'];
    const sColor = starColors[idx % starColors.length];
    sys.color = sColor;
    label.innerHTML = '<div style="width:8px;height:8px;border-radius:50%;background:' + sColor + '"></div>' +
      (sys.label === 'Primary' ? sys.starMass.toFixed(3) + ' M\u2609' : 'Star ' + sys.label + ' \u00B7 ' + sys.starMass.toFixed(3) + ' M\u2609');
    view.appendChild(label);

    // Time display
    const timeEl = document.createElement('div');
    timeEl.style.cssText = "position:absolute;bottom:10px;left:14px;font-family:'JetBrains Mono',monospace;font-size:10px;color:#5a6070;pointer-events:none";
    timeEl.id = 'orbTime_' + key;
    view.appendChild(timeEl);

    // Tooltip
    const tooltip = document.createElement('div');
    tooltip.style.cssText = "position:absolute;background:#181c24;border:1px solid #1a1f2a;padding:6px 10px;border-radius:4px;font-family:'JetBrains Mono',monospace;font-size:10px;pointer-events:none;z-index:10;display:none;white-space:nowrap;box-shadow:0 4px 16px rgba(0,0,0,.5)";
    tooltip.id = 'orbTip_' + key;
    view.appendChild(tooltip);

    panelContainer.appendChild(view);

    const dpr = window.devicePixelRatio || 1;
    function resizeCanvas() {
      const r = view.getBoundingClientRect();
      canvas.width = r.width * dpr;
      canvas.height = r.height * dpr;
    }
    resizeCanvas();
    canvasData[key] = { canvas, ctx: canvas.getContext('2d'), dpr, resize: resizeCanvas };

    // Hover tooltips
    canvas.addEventListener('mousemove', function(e) {
      const rect = canvas.getBoundingClientRect();
      const mx = e.clientX - rect.left, my = e.clientY - rect.top;
      const sysd = starMap[key];
      if (sysd.planets.length === 0) { tooltip.style.display='none'; return; }
      let maxO = sysd.planets[sysd.planets.length-1].apo;
      for (const b of sysd.belts) { const bo = b.outerSma*(1+b.outerEcc); if (bo>maxO) maxO=bo; }
      if (maxO<=0) maxO=1;
      const sc = Math.min(canvas.width,canvas.height)*0.38/maxO;
      const cxc = canvas.width/(2*dpr), cyc = canvas.height/(2*dpr);
      let found = null;
      for (const p of sysd.planets) {
        const pos = _solveOrbit(p, _orbSim.simTime);
        let sx,sy;
        if (_orbSim.viewMode==='top'){sx=cxc+pos.x*sc;sy=cyc+pos.y*sc}else{sx=cxc+pos.x*sc;sy=cyc+pos.z*sc*3}
        if (Math.sqrt((mx-sx)**2+(my-sy)**2)<15){found={p,pos};break}
      }
      if (found) {
        tooltip.style.display='block'; tooltip.style.left=(mx+14)+'px'; tooltip.style.top=(my-10)+'px';
        tooltip.innerHTML='<b>'+found.p.name+'</b> \u00B7 '+found.p.type+'<br>r = '+found.pos.r.toFixed(4)+' AU \u00B7 e = '+found.p.ecc+'<br>['+found.p.peri.toFixed(3)+' \u2013 '+found.p.apo.toFixed(3)+'] AU';
      } else { tooltip.style.display='none'; }
    });
    canvas.addEventListener('mouseleave', function() { tooltip.style.display='none'; });

    idx++;
  }

  window.addEventListener('resize', function() {
    for (const k of starKeys) canvasData[k].resize();
  });

  // State
  _orbSim = {
    starMap, starKeys, canvasData, globalMinPeriod,
    playing: true, showTrails: false, viewMode: 'top',
    simTime: 0, speedFactor: 1, animId: null, lastTime: performance.now()
  };

  // Wire controls
  document.getElementById('orbBtnPlay').addEventListener('click', function() {
    _orbSim.playing = !_orbSim.playing;
    this.innerHTML = _orbSim.playing ? '\u25B6 Running' : '\u23F8 Paused';
    this.classList.toggle('active', _orbSim.playing);
  });
  document.getElementById('orbBtnTrails').addEventListener('click', function() {
    _orbSim.showTrails = !_orbSim.showTrails;
    this.classList.toggle('active', _orbSim.showTrails);
    if (!_orbSim.showTrails) for (const k of starKeys) for (const p of starMap[k].planets) p.trail=[];
  });
  document.getElementById('orbBtnTop').addEventListener('click', function() {
    _orbSim.viewMode='top';
    document.getElementById('orbBtnTop').classList.add('active');
    document.getElementById('orbBtnSide').classList.remove('active');
  });
  document.getElementById('orbBtnSide').addEventListener('click', function() {
    _orbSim.viewMode='side';
    document.getElementById('orbBtnSide').classList.add('active');
    document.getElementById('orbBtnTop').classList.remove('active');
  });
  document.getElementById('orbSpeed').addEventListener('input', function(e) {
    _orbSim.speedFactor = Math.pow(2, parseFloat(e.target.value) - 2);
    document.getElementById('orbSpeedLabel').textContent = '\u00d7' + _orbSim.speedFactor.toFixed(2);
  });

  // Build sidebar
  buildViewerSidebar(sidebar, starMap, starKeys);

  // Start animation
  function animate(now) {
    if (!_orbSim) return;
    const dt = Math.min(50, now - _orbSim.lastTime);
    _orbSim.lastTime = now;
    const timeRate = Math.min(0.05, _orbSim.globalMinPeriod / 5000);
    if (_orbSim.playing) _orbSim.simTime += dt * timeRate * _orbSim.speedFactor;
    for (const key of starKeys) _drawSystem(canvasData[key], starMap[key], _orbSim.simTime);
    const years = (_orbSim.simTime / 365.25).toFixed(1);
    for (const key of starKeys) {
      const te = document.getElementById('orbTime_' + key);
      if (te) te.textContent = 'T = ' + years + ' yr';
    }
    _orbSim.animId = requestAnimationFrame(animate);
  }
  _orbSim.animId = requestAnimationFrame(animate);
}

/* ── Parse API response into star map ── */
function buildStarMapFromApi(sys) {
  const map = {};
  const stars = sys.stars || [];
  if (stars.length === 0) return map;

  // Sort: PRIMARY first
  stars.sort(function(a, b) {
    const order = {PRIMARY:0, SECONDARY:1, TERTIARY:2};
    return (order[a.starRole]||9) - (order[b.starRole]||9);
  });

  const labels = stars.length === 1 ? ['Primary'] : ['A','B','C','D'];

  for (let i = 0; i < stars.length; i++) {
    const star = stars[i];
    const label = labels[i] || String.fromCharCode(65+i);
    const key = label.replace(/[^a-zA-Z0-9_]/g, '_');

    const planets = (star.planets || [])
      .filter(function(p) { return p.orbit && p.orbit.semiMajorAxis > 0; })
      .sort(function(a, b) { return a.orbit.semiMajorAxis - b.orbit.semiMajorAxis; })
      .map(function(p) {
        const orb = p.orbit || {};
        const phys = p.physicalProperties || {};
        const desig = p.designation || {};
        const name = desig.loggedName || p.name || '?';
        const letter = name.split(/\\s+/).pop() || name;
        return {
          name: letter,
          fullName: name,
          type: desig.objectType || 'Unknown',
          mass: phys.earthMass || 1.0,
          sma: orb.semiMajorAxis || 1.0,
          ecc: orb.eccentricity || 0.0,
          inc: orb.inclination || 0.0,
          period: orb.orbitalPeriodDays || 365.25,
          color: _getPlanetColor(desig.objectType)
        };
      });

    const belts = (star.bands || [])
      .filter(function(b) { return b.innerOrbit && b.outerOrbit; })
      .map(function(b) {
        return {
          name: b.name || b.bandType || 'Belt',
          type: b.bandType || '',
          comp: b.compositionType || '',
          innerSma: (b.innerOrbit.semiMajorAxis || 0),
          outerSma: (b.outerOrbit.semiMajorAxis || 0),
          innerEcc: (b.innerOrbit.eccentricity || 0),
          outerEcc: (b.outerOrbit.eccentricity || 0),
          color: _getBeltColor(b.compositionType)
        };
      });

    map[key] = {
      starMass: star.physicalProperties ? (star.physicalProperties.solarMass || 1.0) : 1.0,
      fullName: star.designation ? (star.designation.loggedName || '') : '',
      label: label,
      color: '#f0c850',
      planets: planets,
      belts: belts
    };
  }
  return map;
}

/* ── Kepler solver ── */
function _solveKepler(M, e) {
  let E = M;
  for (let i = 0; i < 30; i++) {
    const dE = (M - E + e * Math.sin(E)) / (1 - e * Math.cos(E));
    E += dE;
    if (Math.abs(dE) < 1e-10) break;
  }
  return E;
}

function _solveOrbit(p, t) {
  const M = p.phase + (2 * Math.PI / p.period) * t;
  const E = _solveKepler(M % (2*Math.PI), p.ecc);
  const v = 2 * Math.atan2(Math.sqrt(1+p.ecc)*Math.sin(E/2), Math.sqrt(1-p.ecc)*Math.cos(E/2));
  const r = p.sma * (1 - p.ecc * Math.cos(E));
  const x = r * Math.cos(v), y0 = r * Math.sin(v);
  const inc = p.inc * Math.PI / 180;
  return { x: x, y: y0 * Math.cos(inc), z: y0 * Math.sin(inc), r: r };
}

/* ── Draw a single star system ── */
function _drawSystem(cv, sys, t) {
  if (!cv) return;
  const canvas = cv.canvas, ctx = cv.ctx, dpr = cv.dpr;
  const w = canvas.width, h = canvas.height;
  const vm = _orbSim.viewMode;
  ctx.clearRect(0, 0, w, h);

  const bgG = ctx.createRadialGradient(w/2,h/2,0,w/2,h/2,w*.6);
  bgG.addColorStop(0,'#0d0f14'); bgG.addColorStop(1,'#060810');
  ctx.fillStyle = bgG; ctx.fillRect(0,0,w,h);

  if (sys.planets.length === 0 && sys.belts.length === 0) {
    ctx.fillStyle = '#ffffff30';
    ctx.font = 12*dpr + "px 'Outfit'";
    ctx.textAlign = 'center';
    ctx.fillText('No planets or belts', w/2, h/2);
    ctx.textAlign = 'start';
    return;
  }

  let maxOrbit = sys.planets.length > 0 ? sys.planets[sys.planets.length-1].apo : 0;
  for (const b of sys.belts) {
    const bOuter = b.outerSma * (1 + b.outerEcc);
    if (bOuter > maxOrbit) maxOrbit = bOuter;
  }
  if (maxOrbit <= 0) maxOrbit = 1;
  const scale = Math.min(w,h) * 0.38 / maxOrbit * dpr;
  const cx = w/2, cy = h/2;

  function toScreen(x,y,z) {
    return vm === 'top' ? [cx+x*scale, cy+y*scale] : [cx+x*scale, cy+z*scale*3];
  }

  // Belt annuli
  if (vm === 'top') {
    for (const b of sys.belts) {
      const innerR = b.innerSma * scale, outerR = b.outerSma * scale;
      if (outerR > 0) {
        ctx.save();
        ctx.beginPath(); ctx.arc(cx,cy,outerR,0,Math.PI*2); ctx.arc(cx,cy,innerR,0,Math.PI*2,true);
        ctx.fillStyle = b.color + '18'; ctx.fill();
        ctx.strokeStyle = b.color + '35'; ctx.lineWidth = 1*dpr;
        ctx.beginPath(); ctx.arc(cx,cy,outerR,0,Math.PI*2); ctx.stroke();
        ctx.beginPath(); ctx.arc(cx,cy,innerR,0,Math.PI*2); ctx.stroke();
        ctx.restore();
      }
    }
  } else {
    for (const b of sys.belts) {
      const innerR = b.innerSma * scale, outerR = b.outerSma * scale;
      if (outerR > 0) {
        ctx.save(); ctx.fillStyle = b.color + '18';
        ctx.fillRect(cx-outerR,cy-2*dpr,outerR-innerR,4*dpr);
        ctx.fillRect(cx+innerR,cy-2*dpr,outerR-innerR,4*dpr);
        ctx.restore();
      }
    }
  }

  // Orbit ellipses
  for (const p of sys.planets) {
    ctx.save(); ctx.translate(cx,cy); ctx.beginPath();
    if (vm === 'top') {
      const a = p.sma*scale, b = a*Math.sqrt(1-p.ecc*p.ecc), cF = p.sma*p.ecc*scale;
      ctx.ellipse(-cF, 0, a, b, 0, 0, Math.PI*2);
    } else {
      const inc = p.inc*Math.PI/180;
      for (let i = 0; i <= 120; i++) {
        const angle = (i/120)*Math.PI*2;
        const ox = (p.sma*(Math.cos(angle)-p.ecc))*scale;
        const oy = p.sma*Math.sqrt(1-p.ecc*p.ecc)*Math.sin(angle);
        const oz = oy*Math.sin(inc)*scale*3;
        i === 0 ? ctx.moveTo(ox,oz) : ctx.lineTo(ox,oz);
      }
      ctx.closePath();
    }
    ctx.strokeStyle = p.color + '20'; ctx.lineWidth = 1*dpr; ctx.stroke(); ctx.restore();
  }

  // Trails
  if (_orbSim.showTrails) {
    for (const p of sys.planets) {
      if (p.trail.length < 2) continue;
      ctx.beginPath();
      for (let i = 0; i < p.trail.length; i++) {
        const [sx,sy] = toScreen(p.trail[i].x, p.trail[i].y, p.trail[i].z);
        i === 0 ? ctx.moveTo(sx,sy) : ctx.lineTo(sx,sy);
      }
      ctx.strokeStyle = p.color+'66'; ctx.lineWidth = 1.2*dpr; ctx.stroke();
    }
  }

  // Star
  const sG = ctx.createRadialGradient(cx,cy,0,cx,cy,10*dpr);
  sG.addColorStop(0, sys.color); sG.addColorStop(.5, sys.color+'80'); sG.addColorStop(1, sys.color+'00');
  ctx.fillStyle = sG; ctx.beginPath(); ctx.arc(cx,cy,10*dpr,0,Math.PI*2); ctx.fill();
  ctx.fillStyle = '#fff'; ctx.beginPath(); ctx.arc(cx,cy,3*dpr,0,Math.PI*2); ctx.fill();

  // Planets
  const positions = [];
  for (const p of sys.planets) {
    const pos = _solveOrbit(p, t);
    const [sx,sy] = toScreen(pos.x, pos.y, pos.z);
    positions.push({p,sx,sy,pos});

    if (_orbSim.playing) {
      p.trail.push({x:pos.x,y:pos.y,z:pos.z});
      const maxLen = Math.max(200, Math.floor(p.period / (_orbSim.speedFactor||1) * 0.15));
      if (p.trail.length > maxLen) p.trail.shift();
    }

    const glowR = (p.renderSize+4)*dpr;
    const glow = ctx.createRadialGradient(sx,sy,0,sx,sy,glowR);
    glow.addColorStop(0, p.color+'40'); glow.addColorStop(1, p.color+'00');
    ctx.fillStyle = glow; ctx.beginPath(); ctx.arc(sx,sy,glowR,0,Math.PI*2); ctx.fill();
    ctx.fillStyle = p.color; ctx.beginPath(); ctx.arc(sx,sy,p.renderSize*dpr,0,Math.PI*2); ctx.fill();
    ctx.fillStyle = '#ffffff90'; ctx.font = 10*dpr+"px 'JetBrains Mono'";
    ctx.fillText(p.name, sx+(p.renderSize+4)*dpr, sy-4*dpr);
  }

  // Warning lines
  for (let i = 0; i < positions.length-1; i++) {
    const a = positions[i], b = positions[i+1];
    const dx = b.pos.x-a.pos.x, dy = b.pos.y-a.pos.y, dz = b.pos.z-a.pos.z;
    const dist = Math.sqrt(dx*dx+dy*dy+dz*dz);
    const bigHill = Math.max(a.p.hill, b.p.hill);
    if (dist < bigHill*5) {
      ctx.beginPath(); ctx.moveTo(a.sx,a.sy); ctx.lineTo(b.sx,b.sy);
      const alpha = Math.min(1, bigHill*3/dist);
      ctx.strokeStyle = dist < bigHill ? 'rgba(239,68,68,'+alpha+')' : 'rgba(245,158,11,'+alpha*.6+')';
      ctx.lineWidth = 1.5*dpr; ctx.setLineDash([4*dpr,4*dpr]); ctx.stroke(); ctx.setLineDash([]);
    }
  }

  // Belt labels
  if (vm === 'top') {
    ctx.font = 9*dpr+"px 'JetBrains Mono'";
    for (const b of sys.belts) {
      const midR = ((b.innerSma + b.outerSma) / 2) * scale;
      ctx.fillStyle = b.color + '80';
      ctx.fillText(b.type || b.name, cx + midR*0.7 + 4*dpr, cy - midR*0.7 - 4*dpr);
    }
  }
}

/* ── Build viewer sidebar ── */
function buildViewerSidebar(sidebar, starMap, starKeys) {
  const GLADMAN = 3.46;
  for (const key of starKeys) {
    const sys = starMap[key];
    if (sys.planets.length === 0 && sys.belts.length === 0) continue;

    // Planets section
    if (sys.planets.length > 0) {
      const sec = document.createElement('div');
      sec.className = 'section';
      sec.innerHTML = '<div class="section-title">Planets' + (starKeys.length > 1 ? ' \u2014 Star ' + sys.label : '') + '</div>';
      const maxAU = sys.planets[sys.planets.length-1].apo || 1;
      for (const p of sys.planets) {
        const periPct = (p.peri/maxAU*100).toFixed(1);
        const apoPct = (p.apo/maxAU*100).toFixed(1);
        const smaPct = (p.sma/maxAU*100).toFixed(1);
        const row = document.createElement('div');
        row.className = 'planet-row';
        row.innerHTML =
          '<div class="planet-name"><div class="planet-pip" style="background:'+p.color+'"></div>'+_esc(p.name)+'</div>' +
          '<div class="planet-meta">'+p.sma.toFixed(3)+' AU \u00B7 e='+p.ecc.toFixed(3)+'</div>' +
          '<div style="font-size:10px;color:var(--text-muted);grid-column:1/-1">'+_esc(p.type)+' \u00B7 '+p.mass.toFixed(2)+' M\u2295 \u00B7 P='+(p.period/365.25).toFixed(2)+' yr</div>';
        sec.appendChild(row);
      }
      sidebar.appendChild(sec);
    }

    // Stability section
    if (sys.planets.length >= 2) {
      const sec = document.createElement('div');
      sec.className = 'section';
      sec.innerHTML = '<div class="section-title">Stability</div>';
      for (let i = 0; i < sys.planets.length-1; i++) {
        const p1 = sys.planets[i], p2 = sys.planets[i+1];
        const gap = p2.peri - p1.apo;
        const h1 = p1.hill, h2 = p2.hill;
        const mh = GLADMAN * (h1 + h2);
        const ratio = mh > 0 ? gap / mh : 999;
        let badge, bcls;
        if (gap < 0) { badge='CROSSING'; bcls='badge-danger'; }
        else if (ratio < 1) { badge='UNSTABLE'; bcls='badge-warn'; }
        else if (ratio < 2) { badge='MARGINAL'; bcls='badge-warn'; }
        else { badge='STABLE'; bcls='badge-safe'; }
        const row = document.createElement('div');
        row.className = 'stability-row';
        row.innerHTML = '<span class="pair">'+_esc(p1.name)+'\u2013'+_esc(p2.name)+'</span>' +
          '<span class="metric">gap='+gap.toFixed(3)+'AU / '+ratio.toFixed(1)+'RH</span>' +
          '<span class="badge '+bcls+'">'+badge+'</span>';
        sec.appendChild(row);
      }
      sidebar.appendChild(sec);
    }

    // Belts section
    if (sys.belts.length > 0) {
      const sec = document.createElement('div');
      sec.className = 'section';
      sec.innerHTML = '<div class="section-title">Belts' + (starKeys.length > 1 ? ' \u2014 Star ' + sys.label : '') + '</div>';
      for (const b of sys.belts) {
        const row = document.createElement('div');
        row.className = 'stability-row';
        row.innerHTML = '<span class="pair"><span class="planet-pip" style="background:'+b.color+';display:inline-block;width:8px;height:8px;border-radius:50%;margin-right:4px"></span>'+_esc(b.name)+'</span>' +
          '<span class="metric">'+b.innerSma.toFixed(2)+'\u2013'+b.outerSma.toFixed(2)+' AU \u00B7 '+_esc(b.comp)+'</span>';
        sec.appendChild(row);
      }
      sidebar.appendChild(sec);
    }
  }
}

/* ── Color helpers ── */
function _getPlanetColor(type) {
  if (!type) return '#997755';
  const t = type.toLowerCase();
  if (t.includes('gas giant') || t.includes('hot jupiter')) return '#c4956a';
  if (t.includes('super-jupiter')) return '#b08050';
  if (t.includes('ice giant')) return '#4488cc';
  if (t.includes('sub-neptune') || t.includes('mini-neptune')) return '#5b9bd5';
  if (t.includes('warm neptune') || t.includes('hot neptune')) return '#6ea8d4';
  if (t.includes('super-earth')) return '#aa8866';
  if (t.includes('terrestrial')) return '#88aa66';
  if (t.includes('ocean')) return '#3388aa';
  if (t.includes('desert')) return '#cc9955';
  if (t.includes('ice world')) return '#aaccee';
  if (t.includes('lava')) return '#ff4422';
  if (t.includes('iron')) return '#888888';
  if (t.includes('carbon')) return '#555555';
  if (t.includes('dwarf')) return '#8899aa';
  if (t.includes('puffy')) return '#dd8855';
  return '#997755';
}

function _getBeltColor(comp) {
  if (!comp) return '#665544';
  const c = comp.toUpperCase();
  if (c.includes('ICY') || c.includes('ICE')) return '#557799';
  if (c.includes('ROCKY') || c.includes('SILICATE')) return '#887766';
  if (c.includes('METALLIC') || c.includes('IRON')) return '#998877';
  if (c.includes('CARBON')) return '#554433';
  return '#665544';
}

function _esc(s) {
  if (!s) return '';
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}
""";
}
