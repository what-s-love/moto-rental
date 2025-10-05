document.addEventListener('DOMContentLoaded', function() {
    const themeSwitcher = document.getElementById('themeSwitcher');
    const body = document.body;

    const savedTheme = localStorage.getItem('theme') || 'light';
    body.setAttribute('data-bs-theme', savedTheme);
    themeSwitcher.checked = savedTheme === 'dark';

    themeSwitcher.addEventListener('change', function() {
        const theme = this.checked ? 'dark' : 'light';
        body.setAttribute('data-bs-theme', theme);
        localStorage.setItem('theme', theme);
    });
});