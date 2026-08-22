document.addEventListener('DOMContentLoaded', function () {
    var sections = Array.from(document.querySelectorAll('.service-story'));
    if (!sections.length) {
        return;
    }

    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
        sections.forEach(function (section) {
            section.classList.add('is-active');
        });
        return;
    }

    var ratios = new Map();
    sections.forEach(function (section) {
        ratios.set(section, 0);
    });

    function updateActive() {
        var winner = null;
        var maxRatio = 0;
        ratios.forEach(function (ratio, section) {
            if (ratio > maxRatio) {
                maxRatio = ratio;
                winner = section;
            }
        });

        sections.forEach(function (section) {
            section.classList.toggle('is-active', section === winner && maxRatio > 0.15);
        });
    }

    var thresholds = [];
    for (var i = 0; i <= 20; i++) {
        thresholds.push(i / 20);
    }

    var observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            ratios.set(entry.target, entry.intersectionRatio);
        });
        updateActive();
    }, { threshold: thresholds });

    sections.forEach(function (section) {
        observer.observe(section);
    });
});
