const slides = document.querySelectorAll('.slide');
const prevBtn = document.getElementById('prevBtn');
const nextBtn = document.getElementById('nextBtn');
const currentSlideEl = document.getElementById('currentSlide');
const totalSlidesEl = document.getElementById('totalSlides');
const navLinks = document.querySelectorAll('.nav-links a');

let currentSlideIndex = 0;
if (totalSlidesEl) totalSlidesEl.textContent = slides.length;

function updateSlide(index) {
  if (slides.length === 0) return;
  currentSlideIndex = index;
  if (currentSlideIndex < 0) currentSlideIndex = 0;
  if (currentSlideIndex >= slides.length) currentSlideIndex = slides.length - 1;
  slides[currentSlideIndex].scrollIntoView({ behavior: 'smooth', block: 'start' });
  if (currentSlideEl) currentSlideEl.textContent = currentSlideIndex + 1;
  navLinks.forEach((link, i) => {
    link.classList.toggle('active', i === currentSlideIndex);
  });
}

if (nextBtn) nextBtn.addEventListener('click', () => updateSlide(currentSlideIndex + 1));
if (prevBtn) prevBtn.addEventListener('click', () => updateSlide(currentSlideIndex - 1));

document.addEventListener('keydown', (e) => {
  if (e.key === 'ArrowRight') updateSlide(currentSlideIndex + 1);
  if (e.key === 'ArrowLeft') updateSlide(currentSlideIndex - 1);
});

navLinks.forEach((link, i) => {
  link.addEventListener('click', (e) => {
    e.preventDefault();
    updateSlide(i);
  });
});

const observer = new IntersectionObserver((entries) => {
  entries.forEach((entry) => {
    if (entry.isIntersecting) {
      const index = Array.from(slides).indexOf(entry.target);
      if (index !== -1) {
        currentSlideIndex = index;
        if (currentSlideEl) currentSlideEl.textContent = currentSlideIndex + 1;
        navLinks.forEach((link, i) => {
          link.classList.toggle('active', i === currentSlideIndex);
        });
      }
    }
  });
}, { threshold: 0.6 });

slides.forEach((slide) => observer.observe(slide));
updateSlide(0);
