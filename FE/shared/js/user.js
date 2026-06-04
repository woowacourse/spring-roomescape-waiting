import { api, getThemeThumbnail } from './api.js';

const popularGrid = document.getElementById('popular-theme-grid');
const themeGrid = document.getElementById('theme-grid');
const bookingOverlay = document.getElementById('booking-overlay');
const closeOverlayBtn = document.getElementById('close-overlay');
const dateInput = document.getElementById('reservation-date');
const timeSlotsContainer = document.getElementById('time-slots');
const nextBtn = document.getElementById('next-to-step-2');
const reserveBtn = document.getElementById('reserve-btn');
const finishBtn = document.getElementById('finish-btn');
const structureNextBtn = document.getElementById('structure-next-btn');
const bookingVisual = document.getElementById('booking-visual');
const heroThemeCount = document.getElementById('hero-theme-count');
const heroNextSlot = document.getElementById('hero-next-slot');
const sceneCanvas = document.getElementById('escape-scene');
const bookingRoomCanvas = document.getElementById('booking-room-model');
const roomLayoutName = document.getElementById('room-layout-name');
const structureThemeName = document.getElementById('structure-theme-name');

let selectedTime = null;
let selectedTheme = null;
let themesById = new Map();
let threeModulePromise = null;
let bookingRoomState = null;

const loadThree = () => {
  if (!threeModulePromise) {
    threeModulePromise = import('https://unpkg.com/three@0.160.0/build/three.module.js');
  }
  return threeModulePromise;
};

const escapeHtml = (value) => String(value ?? '')
  .replaceAll('&', '&amp;')
  .replaceAll('<', '&lt;')
  .replaceAll('>', '&gt;')
  .replaceAll('"', '&quot;')
  .replaceAll("'", '&#039;');

const escapeCssUrl = (value) => String(value ?? '').replaceAll("'", '%27');

const revealCards = () => {
  const cards = document.querySelectorAll('.theme-card, .popular-card');
  if (!('IntersectionObserver' in window)) {
    cards.forEach(card => card.classList.add('is-visible'));
    return;
  }

  const observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (!entry.isIntersecting) return;
      entry.target.classList.add('is-visible');
      observer.unobserve(entry.target);
    });
  }, { threshold: 0.2 });

  cards.forEach(card => observer.observe(card));
};

const initEscapeScene = async () => {
  if (!sceneCanvas || window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

  try {
    const THREE = await loadThree();
    const renderer = new THREE.WebGLRenderer({ canvas: sceneCanvas, alpha: true, antialias: true });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    renderer.outputColorSpace = THREE.SRGBColorSpace;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(38, 1, 0.1, 100);
    camera.position.set(0, 1.7, 7.2);

    const group = new THREE.Group();
    scene.add(group);

    const roomMaterial = new THREE.MeshStandardMaterial({
      color: 0x1a1714,
      metalness: 0.28,
      roughness: 0.42,
      emissive: 0x180407,
      emissiveIntensity: 0.32
    });
    const brassMaterial = new THREE.MeshStandardMaterial({
      color: 0xb87935,
      metalness: 0.85,
      roughness: 0.24,
      emissive: 0x2a1304,
      emissiveIntensity: 0.18
    });
    const redMaterial = new THREE.MeshStandardMaterial({
      color: 0xa32737,
      metalness: 0.48,
      roughness: 0.36,
      emissive: 0x4c0711,
      emissiveIntensity: 0.44
    });

    const doorGeometry = new THREE.BoxGeometry(1.1, 1.86, 0.08);
    const frameGeometry = new THREE.BoxGeometry(1.34, 2.08, 0.1);
    for (let index = 0; index < 7; index += 1) {
      const frame = new THREE.Mesh(frameGeometry, index % 3 === 0 ? redMaterial : roomMaterial);
      const door = new THREE.Mesh(doorGeometry, roomMaterial);
      const angle = (index / 7) * Math.PI * 2;
      const radius = index % 2 === 0 ? 2.2 : 3.05;
      frame.position.set(Math.cos(angle) * radius, Math.sin(index * 0.7) * 0.44, Math.sin(angle) * radius);
      frame.rotation.y = -angle + Math.PI / 2;
      door.position.copy(frame.position);
      door.position.z += Math.cos(angle) * 0.08;
      door.rotation.copy(frame.rotation);
      door.scale.set(0.72, 0.84, 1);
      group.add(frame, door);
    }

    const ring = new THREE.Mesh(new THREE.TorusGeometry(2.65, 0.018, 12, 160), brassMaterial);
    ring.rotation.x = Math.PI / 2.2;
    group.add(ring);

    const key = new THREE.Group();
    const keyHead = new THREE.Mesh(new THREE.TorusGeometry(0.22, 0.035, 12, 48), brassMaterial);
    const keyShaft = new THREE.Mesh(new THREE.BoxGeometry(0.72, 0.07, 0.07), brassMaterial);
    const keyTooth = new THREE.Mesh(new THREE.BoxGeometry(0.12, 0.22, 0.07), brassMaterial);
    keyShaft.position.x = 0.46;
    keyTooth.position.set(0.76, -0.08, 0);
    key.add(keyHead, keyShaft, keyTooth);
    key.position.set(1.4, 0.28, 1.2);
    key.rotation.set(0.35, -0.7, 0.15);
    group.add(key);

    const dotGeometry = new THREE.SphereGeometry(0.018, 10, 10);
    for (let index = 0; index < 90; index += 1) {
      const dot = new THREE.Mesh(dotGeometry, index % 6 === 0 ? redMaterial : brassMaterial);
      dot.position.set(
        (Math.random() - 0.5) * 8,
        (Math.random() - 0.5) * 4.4,
        (Math.random() - 0.5) * 5
      );
      dot.userData.speed = 0.24 + Math.random() * 0.7;
      group.add(dot);
    }

    scene.add(new THREE.AmbientLight(0xffeee0, 0.7));
    const redLight = new THREE.PointLight(0xb02030, 3.2, 9);
    redLight.position.set(-3.2, 1.8, 3);
    scene.add(redLight);
    const warmLight = new THREE.PointLight(0xffbd75, 2.7, 8);
    warmLight.position.set(2.8, -0.8, 2.2);
    scene.add(warmLight);

    const pointer = { x: 0, y: 0 };
    window.addEventListener('pointermove', (event) => {
      pointer.x = (event.clientX / window.innerWidth - 0.5) * 2;
      pointer.y = (event.clientY / window.innerHeight - 0.5) * 2;
    }, { passive: true });

    const resize = () => {
      const rect = sceneCanvas.getBoundingClientRect();
      renderer.setSize(rect.width, rect.height, false);
      camera.aspect = rect.width / Math.max(rect.height, 1);
      camera.updateProjectionMatrix();
    };

    window.addEventListener('resize', resize);
    resize();

    const clock = new THREE.Clock();
    const animate = () => {
      const elapsed = clock.getElapsedTime();
      group.rotation.y = elapsed * 0.12 + pointer.x * 0.18;
      group.rotation.x = -0.08 + pointer.y * 0.08;
      ring.rotation.z = elapsed * 0.18;
      key.rotation.y = -0.7 + Math.sin(elapsed * 1.1) * 0.24;
      key.position.y = 0.28 + Math.sin(elapsed * 1.4) * 0.12;
      renderer.render(scene, camera);
      window.__escapeSceneReady = true;
      requestAnimationFrame(animate);
    };

    animate();
  } catch (error) {
    sceneCanvas.classList.add('scene-fallback');
  }
};

const getMonthRange = () => {
  const now = new Date();
  const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
  const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);

  return {
    from: firstDay.toISOString().split('T')[0],
    to: lastDay.toISOString().split('T')[0]
  };
};

const loadHeroSlot = async () => {
  if (!heroNextSlot) return;

  try {
    const times = await api.getTimes();
    heroNextSlot.textContent = times[0]?.startAt || '대기';
  } catch (error) {
    heroNextSlot.textContent = '대기';
  }
};

const seededRandom = (seed) => {
  let value = seed % 2147483647;
  if (value <= 0) value += 2147483646;
  return () => {
    value = value * 16807 % 2147483647;
    return (value - 1) / 2147483646;
  };
};

const createRoomLayouts = () => Array.from({ length: 20 }, (_, index) => {
  const random = seededRandom(1409 + index * 97);
  const rooms = [];
  const roomCount = 4 + (index % 4);

  for (let roomIndex = 0; roomIndex < roomCount; roomIndex += 1) {
    const angle = (roomIndex / roomCount) * Math.PI * 2 + random() * 0.45;
    const radius = 0.45 + random() * 1.45;
    rooms.push({
      x: Math.cos(angle) * radius,
      z: Math.sin(angle) * radius,
      w: 0.72 + random() * 0.7,
      d: 0.62 + random() * 0.86,
      h: 0.34 + random() * 0.46,
      red: (roomIndex + index) % 5 === 0
    });
  }

  return {
    name: `Layout ${String(index + 1).padStart(2, '0')}`,
    rooms,
    doorAngle: random() * Math.PI * 2,
    clueCount: 3 + (index % 5)
  };
});

const ROOM_LAYOUTS = createRoomLayouts();

const getThemeLayout = (theme = {}) => {
  const numericId = Number(theme.id);
  const seed = Number.isFinite(numericId)
    ? numericId
    : [...String(theme.name || '')].reduce((total, char) => total + char.charCodeAt(0), 0);
  return ROOM_LAYOUTS[Math.abs(seed) % ROOM_LAYOUTS.length];
};

const disposeBookingRoomModel = () => {
  if (!bookingRoomState) return;
  cancelAnimationFrame(bookingRoomState.animationFrame);
  bookingRoomState.renderer.dispose();
  bookingRoomState = null;
};

const addWall = (THREE, group, material, x, z, width, depth, height = 0.42) => {
  const wall = new THREE.Mesh(new THREE.BoxGeometry(width, height, depth), material);
  wall.position.set(x, height / 2, z);
  group.add(wall);
};

const renderBookingRoomModel = async (theme) => {
  if (!bookingRoomCanvas) return;

  try {
    const THREE = await loadThree();
    const layout = getThemeLayout(theme);
    if (roomLayoutName) roomLayoutName.textContent = layout.name;
    disposeBookingRoomModel();

    const renderer = new THREE.WebGLRenderer({ canvas: bookingRoomCanvas, alpha: true, antialias: true });
    renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
    renderer.outputColorSpace = THREE.SRGBColorSpace;

    const scene = new THREE.Scene();
    const camera = new THREE.PerspectiveCamera(42, 1, 0.1, 80);
    camera.position.set(0, 4.2, 5.2);
    camera.lookAt(0, 0, 0);

    const group = new THREE.Group();
    scene.add(group);

    const floorMaterial = new THREE.MeshStandardMaterial({ color: 0x191411, roughness: 0.64, metalness: 0.08 });
    const wallMaterial = new THREE.MeshStandardMaterial({ color: 0x2a201b, roughness: 0.52, metalness: 0.18 });
    const redMaterial = new THREE.MeshStandardMaterial({
      color: 0x8f1f2f,
      roughness: 0.42,
      metalness: 0.28,
      emissive: 0x3e0710,
      emissiveIntensity: 0.35
    });
    const brassMaterial = new THREE.MeshStandardMaterial({
      color: 0xd7a768,
      roughness: 0.24,
      metalness: 0.82,
      emissive: 0x2b1605,
      emissiveIntensity: 0.18
    });

    layout.rooms.forEach((room, roomIndex) => {
      const floor = new THREE.Mesh(new THREE.BoxGeometry(room.w, 0.045, room.d), floorMaterial);
      floor.position.set(room.x, 0, room.z);
      group.add(floor);

      const material = room.red ? redMaterial : wallMaterial;
      addWall(THREE, group, material, room.x, room.z - room.d / 2, room.w, 0.045, room.h);
      addWall(THREE, group, material, room.x, room.z + room.d / 2, room.w, 0.045, room.h);
      addWall(THREE, group, material, room.x - room.w / 2, room.z, 0.045, room.d, room.h);
      addWall(THREE, group, material, room.x + room.w / 2, room.z, 0.045, room.d, room.h);

      const clue = new THREE.Mesh(new THREE.SphereGeometry(0.045, 16, 16), roomIndex % 2 === 0 ? brassMaterial : redMaterial);
      clue.position.set(room.x, room.h + 0.06, room.z);
      group.add(clue);
    });

    const pathMaterial = new THREE.MeshStandardMaterial({ color: 0xd7a768, roughness: 0.3, metalness: 0.7 });
    layout.rooms.slice(1).forEach((room, index) => {
      const previous = layout.rooms[index];
      const dx = room.x - previous.x;
      const dz = room.z - previous.z;
      const length = Math.hypot(dx, dz);
      const corridor = new THREE.Mesh(new THREE.BoxGeometry(0.08, 0.035, length), pathMaterial);
      corridor.position.set((room.x + previous.x) / 2, 0.04, (room.z + previous.z) / 2);
      corridor.rotation.y = Math.atan2(dx, dz);
      group.add(corridor);
    });

    const door = new THREE.Mesh(new THREE.BoxGeometry(0.16, 0.7, 0.08), redMaterial);
    door.position.set(Math.cos(layout.doorAngle) * 2.25, 0.38, Math.sin(layout.doorAngle) * 2.25);
    door.rotation.y = -layout.doorAngle;
    group.add(door);

    for (let index = 0; index < layout.clueCount; index += 1) {
      const angle = (index / layout.clueCount) * Math.PI * 2;
      const marker = new THREE.Mesh(new THREE.TorusGeometry(0.07, 0.008, 8, 28), brassMaterial);
      marker.position.set(Math.cos(angle) * 1.9, 0.14, Math.sin(angle) * 1.9);
      marker.rotation.x = Math.PI / 2;
      group.add(marker);
    }

    scene.add(new THREE.AmbientLight(0xfff0d8, 0.78));
    const warmLight = new THREE.PointLight(0xd7a768, 2.3, 8);
    warmLight.position.set(1.8, 3.4, 2.2);
    scene.add(warmLight);
    const redLight = new THREE.PointLight(0x9f1f2f, 1.6, 7);
    redLight.position.set(-2.3, 1.6, 2);
    scene.add(redLight);

    const resize = () => {
      const rect = bookingRoomCanvas.getBoundingClientRect();
      renderer.setSize(rect.width, rect.height, false);
      camera.aspect = rect.width / Math.max(rect.height, 1);
      camera.updateProjectionMatrix();
    };

    const animate = () => {
      group.rotation.y += 0.006;
      group.rotation.x = -0.08 + Math.sin(performance.now() / 1800) * 0.035;
      renderer.render(scene, camera);
      window.__bookingRoomModelReady = true;
      bookingRoomState.animationFrame = requestAnimationFrame(animate);
    };

    bookingRoomState = { renderer, animationFrame: 0 };
    resize();
    animate();
  } catch (error) {
    if (bookingVisual) bookingVisual.classList.add('model-fallback');
  }
};

const renderThemeCard = (theme, index, className = 'theme-card') => {
  const thumbnail = getThemeThumbnail(theme, index);
  const rank = className === 'popular-card' ? `<span class="theme-rank">0${index + 1}</span>` : '';
  return `
    <div class="${className}" data-id="${theme.id}">
      <div class="theme-image" style="background-image: linear-gradient(135deg, rgba(6, 5, 5, 0.16), rgba(6, 5, 5, 0.58)), url('${escapeCssUrl(thumbnail)}')">
        ${rank}
        <span class="theme-action">예약 진입</span>
      </div>
      <div class="theme-content">
        <h3 class="theme-title">${escapeHtml(theme.name)}</h3>
        <p class="theme-description">${escapeHtml(theme.description)}</p>
      </div>
    </div>
  `;
};

// --- Navigation ---
const goToStep = (stepNumber) => {
  document.querySelectorAll('.step-pane').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.step').forEach(s => s.classList.remove('active'));
  
  document.getElementById(`step-${stepNumber}-content`).classList.add('active');
  document.querySelector(`.step[data-step="${stepNumber}"]`).classList.add('active');
  bookingOverlay?.classList.toggle('structure-mode', stepNumber === 1);
};

const openBooking = (theme) => {
  selectedTheme = theme;
  selectedTime = null;
  reserveBtn.disabled = false;
  reserveBtn.textContent = '예약하기';
  document.querySelector('#step-4-content h2').textContent = '예약이 완료되었습니다';
  document.getElementById('selected-theme-name').textContent = theme.name;
  document.getElementById('selected-theme-desc').textContent = theme.description;
  if (structureThemeName) structureThemeName.textContent = theme.name;
  if (bookingVisual) {
    bookingVisual.style.backgroundImage = '';
    bookingVisual.classList.remove('model-fallback');
  }
  
  // Set default date
  const today = new Date().toISOString().split('T')[0];
  dateInput.value = today;
  dateInput.min = today;
  
  bookingOverlay.style.display = 'flex';
  document.body.style.overflow = 'hidden';
  
  goToStep(1);
  updateTimes();
  renderBookingRoomModel(theme);
};

const closeBooking = () => {
  bookingOverlay.style.display = 'none';
  document.body.style.overflow = 'auto';
  selectedTime = null;
  selectedTheme = null;
  disposeBookingRoomModel();
  goToStep(1);
};

// --- Data Loading ---
const loadThemes = async () => {
  themeGrid.innerHTML = '<p class="empty-state">테마를 불러오는 중입니다...</p>';

  try {
    const themes = await api.getThemes();
    themesById = new Map(themes.map(theme => [String(theme.id), theme]));
    if (heroThemeCount) heroThemeCount.textContent = `${themes.length} rooms`;
    await loadPopularThemes(themes);

    if (themes.length === 0) {
      themeGrid.innerHTML = `
        <div class="empty-state">
          <strong>등록된 테마가 없습니다.</strong>
          <span>관리자 화면에서 테마를 먼저 추가해주세요.</span>
        </div>
      `;
      return;
    }

    themeGrid.innerHTML = themes.map((theme, index) => renderThemeCard(theme, index)).join('');
    revealCards();
  } catch (error) {
    themeGrid.innerHTML = '<p class="empty-state">테마를 불러오지 못했습니다.</p>';
    popularGrid.innerHTML = '<p class="empty-state">인기 테마를 불러오지 못했습니다.</p>';
  }
};

const loadPopularThemes = async (fallbackThemes = []) => {
  popularGrid.innerHTML = '<p class="empty-state">인기 테마를 불러오는 중입니다...</p>';

  try {
    const range = getMonthRange();
    const popularThemes = await api.getPopularThemes({ ...range, limit: 3 });
    const themes = popularThemes.length > 0 ? popularThemes : fallbackThemes.slice(0, 3);

    if (themes.length === 0) {
      popularGrid.innerHTML = '<p class="empty-state">아직 인기 테마 집계가 없습니다.</p>';
      return;
    }

    popularGrid.innerHTML = themes.map((theme, index) => renderThemeCard(theme, index, 'popular-card')).join('');
    revealCards();
  } catch (error) {
    const themes = fallbackThemes.slice(0, 3);
    popularGrid.innerHTML = themes.length > 0
      ? themes.map((theme, index) => renderThemeCard(theme, index, 'popular-card')).join('')
      : '<p class="empty-state">인기 테마를 불러오지 못했습니다.</p>';
    revealCards();
  }
};

const updateTimes = async () => {
  if (!selectedTheme || !dateInput.value) return;

  nextBtn.disabled = true;
  selectedTime = null;
  timeSlotsContainer.innerHTML = '<p style="grid-column: 1/-1; text-align: center; color: #8e8e93;">예약 가능 시간을 확인하는 중입니다...</p>';

  try {
    const times = await api.getReservableTimes(dateInput.value, selectedTheme.id);
    if (times.length === 0) {
      timeSlotsContainer.innerHTML = '<p style="grid-column: 1/-1; text-align: center;">선택한 날짜에 등록된 시간이 없습니다.</p>';
      return;
    }
    
    timeSlotsContainer.innerHTML = times.map(time => `
      <div class="time-slot ${time.available ? '' : 'waiting'}" 
           data-id="${time.timeId}" 
           data-available="${time.available}"
           data-mode="${time.available ? 'reservation' : 'waiting'}">
        <strong>${escapeHtml(time.startAt)}</strong>
        <span>${time.available ? '예약 가능' : '대기 신청'}</span>
      </div>
    `).join('');
    const nextAvailable = times.find(time => time.available);
    if (heroNextSlot && nextAvailable) heroNextSlot.textContent = nextAvailable.startAt;
  } catch (error) {
    timeSlotsContainer.innerHTML = '<p style="grid-column: 1/-1; color: #ff3b30;">예약 가능 시간을 불러오지 못했습니다.</p>';
  }
};

// --- Event Listeners ---
themeGrid.addEventListener('click', async (e) => {
  const card = e.target.closest('.theme-card');
  if (!card) return;

  const theme = themesById.get(String(card.dataset.id));
  if (!theme) return;

  openBooking(theme);
});

popularGrid.addEventListener('click', async (e) => {
  const card = e.target.closest('.popular-card');
  if (!card) return;

  const theme = themesById.get(String(card.dataset.id));
  if (!theme) return;

  openBooking(theme);
});

dateInput.addEventListener('change', updateTimes);

timeSlotsContainer.addEventListener('click', (e) => {
  const slot = e.target.closest('.time-slot');
  if (!slot) return;

  document.querySelectorAll('.time-slot').forEach(s => s.classList.remove('selected'));
  slot.classList.add('selected');
  
  selectedTime = {
    id: slot.dataset.id,
    startAt: slot.querySelector('strong')?.textContent.trim() || slot.textContent.trim(),
    mode: slot.dataset.mode
  };
  reserveBtn.textContent = selectedTime.mode === 'waiting' ? '대기 신청하기' : '예약하기';
  
  nextBtn.disabled = false;
});

structureNextBtn.addEventListener('click', () => goToStep(2));
nextBtn.addEventListener('click', () => goToStep(3));
document.getElementById('back-to-structure').addEventListener('click', () => goToStep(1));
document.getElementById('back-to-step-1').addEventListener('click', () => goToStep(2));

reserveBtn.addEventListener('click', async () => {
  const name = document.getElementById('reservation-name').value;
  if (!name) {
    alert('예약자 이름을 입력해주세요.');
    return;
  }

  reserveBtn.disabled = true;
  reserveBtn.textContent = '예약 처리 중...';

  try {
    const reservation = {
      name,
      date: dateInput.value,
      timeId: selectedTime.id,
      themeId: selectedTheme.id
    };

    const result = selectedTime.mode === 'waiting'
      ? await api.createWaiting(reservation)
      : await api.createReservation(reservation);
    const modeText = selectedTime.mode === 'waiting' ? '대기 신청' : '예약';
    
    document.getElementById('reservation-summary').innerHTML = `
      <p><strong>테마:</strong> ${escapeHtml(selectedTheme.name)}</p>
      <p><strong>날짜:</strong> ${dateInput.value}</p>
      <p><strong>시간:</strong> ${selectedTime.startAt}</p>
      <p><strong>예약자:</strong> ${escapeHtml(name)}</p>
      <p><strong>상태:</strong> ${modeText}${result?.rank ? ` · ${result.rank}순위` : ''}</p>
    `;
    document.querySelector('#step-4-content h2').textContent = selectedTime.mode === 'waiting'
      ? '대기 신청이 완료되었습니다'
      : '예약이 완료되었습니다';
    
    goToStep(4);
  } catch (error) {
    alert(error.message || '예약에 실패했습니다. 다시 시도해주세요.');
    reserveBtn.disabled = false;
    reserveBtn.textContent = '예약하기';
  }
});

finishBtn.addEventListener('click', closeBooking);
closeOverlayBtn.addEventListener('click', () => {
  if (confirm('예약 창을 닫을까요? 입력한 내용은 저장되지 않습니다.')) closeBooking();
});

// Initial load
initEscapeScene();
loadThemes();
loadHeroSlot();
