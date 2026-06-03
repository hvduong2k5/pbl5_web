/**
 * Theme Manager - Light/Dark Mode
 * Persists user preference in localStorage under key 'theme'.
 */
const ThemeManager = {
    STORAGE_KEY: 'theme',
    LIGHT: 'light',
    DARK: 'dark',

    init() {
        const saved = localStorage.getItem(this.STORAGE_KEY);
        const theme = saved === this.DARK ? this.DARK : this.LIGHT;
        this.apply(theme);
        this.bindToggle();
    },

    apply(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem(this.STORAGE_KEY, theme);
        this.updateIcons(theme);
    },

    toggle() {
        const current = document.documentElement.getAttribute('data-theme');
        const next = current === this.DARK ? this.LIGHT : this.DARK;
        this.apply(next);
    },

    updateIcons(theme) {
        const sunIcons = document.querySelectorAll('.theme-icon-sun');
        const moonIcons = document.querySelectorAll('.theme-icon-moon');
        sunIcons.forEach(el => {
            el.style.opacity = theme === this.LIGHT ? '1' : '0.4';
        });
        moonIcons.forEach(el => {
            el.style.opacity = theme === this.DARK ? '1' : '0.4';
        });
    },

    bindToggle() {
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.theme-toggle-btn');
            if (btn) {
                e.preventDefault();
                this.toggle();
            }
        });
    }
};

// Auto-init on load
ThemeManager.init();
