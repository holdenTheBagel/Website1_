document.addEventListener('DOMContentLoaded', function () {
    var sections = document.querySelectorAll('.service-story');
    if (!sections.length) {
        return;
    }

    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
        sections.forEach(function (section) {
            section.classList.add('is-active');
        });
        return;
    }

    var observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            entry.target.classList.toggle('is-active', entry.isIntersecting);
        });
    }, { threshold: 0.35 });

    sections.forEach(function (section) {
        observer.observe(section);
    });
});
