// SmartSpend TFG — Vanilla JS Slider
(function () {
  'use strict';

  const slides   = document.querySelectorAll('.slide');
  const dots     = document.querySelectorAll('.dot');
  const counter  = document.getElementById('counter');
  const bar      = document.getElementById('progress-bar');
  const notesBox = document.getElementById('notes');
  const notesText= document.getElementById('notes-text');
  const btnPrev  = document.getElementById('btn-prev');
  const btnNext  = document.getElementById('btn-next');

  let cur = 0;
  const total = slides.length;

  /* ── Collect speaker notes per slide ── */
  const notesData = Array.from(slides).map(s => {
    const el = s.querySelector('.sp-notes');
    return el ? el.innerText.trim() : '';
  });

  function goTo(idx, dir = 1) {
    if (idx < 0 || idx >= total || idx === cur) return;

    // exit current
    slides[cur].classList.remove('active');
    slides[cur].classList.add(dir > 0 ? 'exit-left' : 'exit-right');
    setTimeout(() => slides[cur < 0 ? 0 : cur].classList.remove('exit-left', 'exit-right'), 460);

    cur = idx;

    // enter new
    slides[cur].style.transform = dir > 0 ? 'translateX(60px)' : 'translateX(-60px)';
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        slides[cur].style.transform = '';
        slides[cur].classList.add('active');
      });
    });

    updateUI();
  }

  function updateUI() {
    // dots
    dots.forEach((d, i) => d.classList.toggle('on', i === cur));
    // counter
    counter.textContent = `${cur + 1} / ${total}`;
    // progress bar
    bar.style.width = `${((cur + 1) / total) * 100}%`;
    // notes
    notesText.textContent = notesData[cur] || '—';
    // nav btn state
    btnPrev.style.opacity = cur === 0 ? '.3' : '1';
    btnNext.style.opacity = cur === total - 1 ? '.3' : '1';
  }

  /* ── Keyboard ── */
  document.addEventListener('keydown', e => {
    if (e.key === 'ArrowRight' || e.key === 'ArrowDown' || e.key === ' ') { e.preventDefault(); goTo(cur + 1); }
    if (e.key === 'ArrowLeft'  || e.key === 'ArrowUp')                    { e.preventDefault(); goTo(cur - 1, -1); }
    if (e.key === 'n' || e.key === 'N') notesBox.classList.toggle('on');
    if (e.key === 'Home') goTo(0, -1);
    if (e.key === 'End')  goTo(total - 1);
  });

  /* ── Buttons ── */
  btnPrev.addEventListener('click', () => goTo(cur - 1, -1));
  btnNext.addEventListener('click', () => goTo(cur + 1));

  /* ── Dots ── */
  dots.forEach((d, i) => d.addEventListener('click', () => goTo(i, i > cur ? 1 : -1)));

  /* ── Touch/swipe ── */
  let tx = 0;
  document.addEventListener('touchstart', e => { tx = e.touches[0].clientX; }, { passive: true });
  document.addEventListener('touchend',   e => {
    const dx = e.changedTouches[0].clientX - tx;
    if (Math.abs(dx) > 50) goTo(dx < 0 ? cur + 1 : cur - 1, dx < 0 ? 1 : -1);
  });

  /* ── Init ── */
  slides[0].classList.add('active');
  updateUI();
})();
