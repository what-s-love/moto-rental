// Общие скрипты панели администратора
document.addEventListener('DOMContentLoaded', function () {
    // Подтверждение действий: data-confirm="Текст" на кнопке отправки или на форме
    document.querySelectorAll('button[data-confirm]').forEach(btn => {
        btn.addEventListener('click', function (e) {
            if (!confirm(this.dataset.confirm)) {
                e.preventDefault();
            }
        });
    });
    document.querySelectorAll('form[data-confirm]').forEach(form => {
        form.addEventListener('submit', function (e) {
            if (!confirm(this.dataset.confirm)) {
                e.preventDefault();
            }
        });
    });

    // Копирование в буфер: .copy-btn копирует data-copy со своей кнопки
    // или значение соседнего .copy-field внутри .input-group
    document.querySelectorAll('.copy-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const input = btn.closest('.input-group')?.querySelector('.copy-field');
            copyValue(btn.dataset.copy || (input && (input.dataset.copy || input.value)), btn);
        });
    });
    document.querySelectorAll('.copy-field').forEach(input => {
        input.addEventListener('click', () => {
            const btn = input.closest('.input-group').querySelector('.copy-btn');
            copyValue(input.dataset.copy || input.value, btn);
        });
        input.style.cursor = 'pointer';
    });
});

function copyValue(value, btn) {
    if (!value) return;
    navigator.clipboard.writeText(value).then(() => {
        const icon = btn.querySelector('i');
        const outlineClass = [...btn.classList].find(c => c.startsWith('btn-outline-'));
        icon.classList.replace('fa-copy', 'fa-check');
        btn.classList.add('btn-success');
        if (outlineClass) btn.classList.remove(outlineClass);
        setTimeout(() => {
            icon.classList.replace('fa-check', 'fa-copy');
            btn.classList.remove('btn-success');
            if (outlineClass) btn.classList.add(outlineClass);
        }, 1500);
    });
}
