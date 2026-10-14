// Админка: создание бронирования (admin/booking/create.html).
// Данные с сервера передаются через window.AdminBookingCreateConfig в шаблоне.

let { availableBikes, maxParticipants } = window.AdminBookingCreateConfig;
const { i18n } = window.AdminBookingCreateConfig;

function participantLabel(index) {
    return i18n.participant + ' #' + (index + 1);
}

function getSelectedBikes() {
    const selects = document.querySelectorAll('#participantsContainer select[name$=".bikeId"]');
    return Array.from(selects)
        .map(s => s.value)
        .filter(v => v !== '');
}

async function loadAvailableBikes() {
    const date = document.getElementById('date').value;
    const shiftId = document.querySelector('select[name="shiftId"]').value;
    if (!date || !shiftId) {
        availableBikes = [];
        maxParticipants = 0;
        updateBikeSelects();
        return;
    }
    const response = await fetch(
        `/admin/api/bikes/availability?date=${date}&shiftId=${shiftId}`
    );
    const data = await response.json();
    availableBikes = data.bikes;
    maxParticipants = data.maxParticipants;

    // Сбросить невалидные выборы (байк стал occupied)
    document.querySelectorAll('select[name$=".bikeId"]').forEach(select => {
        const bike = availableBikes.find(b => b.id === Number(select.value));
        if (bike?.occupied) select.value = '';
    });
    updateBikeSelects();
}

document.getElementById('date').addEventListener('change', loadAvailableBikes);
document.querySelector('select[name="shiftId"]').addEventListener('change', loadAvailableBikes);

function updateBikeSelects() {
    const selectedBikes = getSelectedBikes();
    const selects = document.querySelectorAll('#participantsContainer select[name$=".bikeId"]');

    selects.forEach(select => {
        const currentValue = select.value;
        select.innerHTML = `<option value="" selected disabled>${i18n.selectBike}</option>`;

        availableBikes.forEach(bike => {
            const label = `${bike.brand} ${bike.model} · ${bike.engineCc}cc`;
            const option = document.createElement('option');
            option.value = bike.id;
            option.textContent = label;
            const occupiedByRide = bike.occupied === true;
            const occupiedInForm = selectedBikes.includes(String(bike.id))
                && String(bike.id) !== currentValue;
            if (occupiedByRide || occupiedInForm) {
                option.disabled = true;
                option.textContent = label + ' (' + i18n.bikeOccupied + ')';
            }
            select.appendChild(option);
        });

        if (currentValue) {
            select.value = currentValue;
        }
    });
}

function participantBlockHtml(i) {
    return `
    <div class="card bg-light border" data-participant-index="${i}">
        <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
                <strong>${participantLabel(i)}</strong>
                <button type="button"
                        class="btn btn-outline-danger btn-sm"
                        onclick="removeParticipant(event)">
                    <i class="fas fa-xmark"></i>
                </button>
            </div>

            <div class="row g-3">
                <div class="col-md-3">
                    <label class="form-label">${i18n.gender}</label>
                    <select class="form-select" name="participants[${i}].gender" required>
                        <option value="" selected disabled>${i18n.genderSelect}</option>
                        <option value="MALE">${i18n.genderMale}</option>
                        <option value="FEMALE">${i18n.genderFemale}</option>
                    </select>
                </div>

                <div class="col-md-3">
                    <label class="form-label">${i18n.age}</label>
                    <input class="form-control" type="number"
                           name="participants[${i}].age"
                           min="12" max="99"
                           placeholder="${i18n.placeholderAge}"
                           required>
                </div>

                <div class="col-md-3">
                    <label class="form-label">${i18n.height}</label>
                    <input class="form-control" type="number"
                           name="participants[${i}].height"
                           min="130" max="220"
                           placeholder="${i18n.placeholderHeight}"
                           required>
                </div>

                <div class="col-md-3">
                    <label class="form-label">${i18n.experience}</label>
                    <select class="form-select"
                            name="participants[${i}].experienceLevel"
                            required>
                        <option value="" selected disabled>${i18n.experienceSelect}</option>
                        <option value="NONE">${i18n.experienceNone}</option>
                        <option value="BEGINNER">${i18n.experienceBeginner}</option>
                        <option value="INTERMEDIATE">${i18n.experienceIntermediate}</option>
                        <option value="ADVANCED">${i18n.experienceAdvanced}</option>
                    </select>
                </div>

                <div class="col-md-12">
                    <label class="form-label">${i18n.bike}</label>
                    <select class="form-select bike-select"
                            name="participants[${i}].bikeId"
                            required>
                        <option value="" selected disabled>${i18n.selectBike}</option>
                    </select>
                </div>
            </div>
        </div>
    </div>`;
}

function addParticipant() {
    const count = document.querySelectorAll('#participantsContainer .card').length;
    if (count >= maxParticipants) return;

    const container = document.getElementById('participantsContainer');
    container.insertAdjacentHTML('beforeend', participantBlockHtml(count));

    updateBikeSelects();

    const newCard = container.lastElementChild;
    const bikeSelect = newCard.querySelector('.bike-select');
    if (bikeSelect) {
        bikeSelect.addEventListener('change', updateBikeSelects);
    }
}

function removeParticipant(event) {
    const btn = event.target.closest('button');
    const card = btn.closest('.card');
    if (card) {
        card.remove();
        reindexParticipants();
        updateBikeSelects();
    }
}

function reindexParticipants() {
    const cards = document.querySelectorAll('#participantsContainer .card');

    cards.forEach((card, newIndex) => {
        card.setAttribute('data-participant-index', newIndex);

        const header = card.querySelector('strong');
        if (header) {
            header.textContent = participantLabel(newIndex);
        }

        const inputs = card.querySelectorAll('input, select');
        inputs.forEach(input => {
            const name = input.getAttribute('name');
            if (name && name.startsWith('participants[')) {
                const newName = name.replace(/participants\[\d+\]/, `participants[${newIndex}]`);
                input.setAttribute('name', newName);
            }
        });
    });
}

document.addEventListener('DOMContentLoaded', function () {
    const addBtn = document.getElementById('addParticipantBtn');

    if (addBtn) {
        addBtn.addEventListener('click', addParticipant);
    }

    addParticipant();
});
