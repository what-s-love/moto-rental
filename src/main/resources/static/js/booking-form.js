// Форма бронирования заезда (booking.html и booking-special.html).
// Данные с сервера передаются через window.BookingConfig в шаблоне.
// Функции объявлены глобально: их вызывают onchange/onclick в разметке формы.

const { availableBikes, maxParticipants, routePrice, i18n } = window.BookingConfig;
let participantCounter = 1;
let selectedRoutePrice = routePrice;

function fmt(template, ...values) {
    return values.reduce((s, v, i) => s.replace(`{${i}}`, v), template);
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    validateForm();
    updateTotalPrice();

    // Add age validation listener for first participant (client)
    const clientAgeInput = document.getElementById('clientAge');
    if (clientAgeInput) {
        clientAgeInput.addEventListener('change', validateClientAge);
    }

    // Add form validation listener
    document.getElementById('bookingForm').addEventListener('change', function() {
        validateForm();
        updateTotalPrice();
    });

    // Handle form submission
    document.getElementById('bookingForm').addEventListener('submit', handleFormSubmit);
});

// ===== 3.1 Валидация возраста клиента =====
function validateClientAge() {
    const ageInput = document.getElementById('clientAge');
    const age = parseInt(ageInput.value);
    // Remove existing warning if any
    let warning = document.getElementById('ageWarning');
    if (warning) {
        warning.remove();
    }

    if (age > 0 && age < 18) {
        // Create and show warning
        const warningDiv = document.createElement('div');
        warningDiv.id = 'ageWarning';
        warningDiv.className = 'alert alert-warning mt-3';
        warningDiv.innerHTML = `
            <i class="fas fa-exclamation-triangle me-2"></i>
            <strong>${i18n.ageWarningTitle}</strong> ${i18n.ageWarningText}
        `;

        // Insert after first participant block
        const participantBlock = document.getElementById('participant-0');
        participantBlock.after(warningDiv);

    } else {
        // Enable submit if form is valid
        validateForm();
    }
}

// ===== 3.2 Динамическая фильтрация мотоциклов =====
function filterBikes(participantIndex) {
    const participantBlock = document.getElementById('participant-' + participantIndex);
    if (!participantBlock) return;

    const genderSelect = participantBlock.querySelector('select[name*="gender"]');
    const experienceLevelSelect = participantBlock.querySelector('select[name*="experienceLevel"]');
    const ageInput = participantBlock.querySelector('input[name*="age"]');
    const heightInput = participantBlock.querySelector('input[name*="height"]');

    if (!genderSelect || !ageInput || !heightInput) return;

    const gender = genderSelect.value;
    const experienceLevel = experienceLevelSelect ? experienceLevelSelect.value : '';
    const age = parseInt(ageInput.value) || 0;
    const height = parseInt(heightInput.value) || 0;

    const bikesContainer = participantBlock.querySelector('.bikes-selection');
    const bikeCheckboxes = participantBlock.querySelectorAll('.bike-checkbox');

    // Get all currently selected bikes (occupied by other participants)
    const occupiedBikeIds = getOccupiedBikeIds(participantIndex);

    // Два списка для всплывания доступных
    const availableCheckboxes = [];
    const unavailableCheckboxes = [];

    bikeCheckboxes.forEach(checkbox => {
        const input = checkbox.querySelector('input[type="radio"]');
        const bikeId = parseInt(input.value);
        const minRaw = input.dataset.limitHeightMin;
        const maxRaw = input.dataset.limitHeightMax;
        const heightMin = minRaw !== '' && minRaw != null ? parseInt(minRaw, 10) : null;
        const heightMax = maxRaw !== '' && maxRaw != null ? parseInt(maxRaw, 10) : null;
        const ageMin = parseInt(input.dataset.limitAgeMin) || 0;
        const onlyMen = input.dataset.limitOnlyMen === 'true';

        let isAvailable = true;
        let reasons = [];

        // Check age restriction
        if (age > 0 && age < ageMin) {
            isAvailable = false;
            reasons.push(fmt(i18n.minAge, ageMin));
        }

        // Check height restriction
        if (height > 0) {
            if (heightMin != null && height < heightMin) {
                isAvailable = false;
                reasons.push(fmt(i18n.minHeight, heightMin));
            }
            if (heightMax != null && height > heightMax) {
                isAvailable = false;
                reasons.push(fmt(i18n.maxHeight, heightMax));
            }
        }

        // Check gender restriction
        if (onlyMen && gender === 'FEMALE' && experienceLevel !== 'ADVANCED') {
            isAvailable = false;
            reasons.push(i18n.onlyMenOrPro);
        }

        // Check if bike is already occupied by another participant
        if (occupiedBikeIds.includes(bikeId)) {
            isAvailable = false;
            reasons.push(i18n.bikeOccupied);
        }

        // Update checkbox state
        if (isAvailable) {
            checkbox.classList.remove('text-muted', 'opacity-50');
            input.disabled = false;
            checkbox.title = '';
            availableCheckboxes.push(checkbox);
        } else {
            checkbox.classList.add('text-muted', 'opacity-50');
            input.disabled = true;
            checkbox.title = reasons.join('; ');
            // Uncheck if was selected
            if (input.checked) {
                input.checked = false;
            }
            unavailableCheckboxes.push(checkbox);
        }
    });

    // Переставить узлы: доступные сверху, недоступные снизу.
    // appendChild перемещает существующий узел, поэтому дубликаты не создаются,
    // а выбранное radio-значение сохраняется.
    if (bikesContainer) {
        [...availableCheckboxes, ...unavailableCheckboxes].forEach(cb => {
            bikesContainer.appendChild(cb);
        });
    }

    // Revalidate form after filtering
    validateForm();
}

// Helper function to get bikes occupied by other participants
function getOccupiedBikeIds(excludeIndex) {
    const occupiedIds = [];
    const allParticipants = document.querySelectorAll('.participant-block');

    allParticipants.forEach((block) => {
        const blockId = block.id;
        const blockIndex = parseInt(blockId.replace('participant-', ''));

        // Skip the current participant
        if (blockIndex === excludeIndex) return;

        const selectedBike = block.querySelector('input[type="radio"]:checked');
        if (selectedBike) {
            occupiedIds.push(parseInt(selectedBike.value));
        }
    });

    return occupiedIds;
}

// ===== 3.3 Обработчики onChange (используется в updateAvailableBikes) =====
function updateAvailableBikes(index) {
    filterBikes(index);

    // Also refilter all other participants to update occupied bikes
    const allParticipants = document.querySelectorAll('.participant-block');
    allParticipants.forEach((block) => {
        const blockIndex = parseInt(block.id.replace('participant-', ''));
        if (blockIndex !== index) {
            filterBikes(blockIndex);
        }
    });
}

// ===== 3.4 Добавление участника =====
function addParticipant() {
    const currentCount = document.querySelectorAll('.participant-block').length;

    if (currentCount >= maxParticipants) {
        alert(fmt(i18n.maxParticipants, maxParticipants));
        return;
    }

    const index = participantCounter++;
    const container = document.getElementById('participantsContainer');

    const participantHtml = `
    <div class="participant-block mb-4 p-3 border rounded bg-light" id="participant-${index}">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h6 class="mb-0">
                <i class="fas fa-user me-2"></i>
                ${i18n.participant} #${index + 1}
            </h6>
            <button type="button"
                    class="btn btn-sm btn-outline-danger"
                    onclick="removeParticipant(${index})">
                <i class="fas fa-times"></i> ${i18n.participantRemove}
            </button>
        </div>

        <div class="row g-3">
            <div class="col-lg-8">
                <div class="row g-3">
                    <div class="col-md-6">
                        <label class="form-label">
                            ${i18n.gender} <span class="text-danger">${i18n.required}</span>
                        </label>
                        <select class="form-select"
                                name="participants[${index}].gender"
                                required
                                onchange="updateAvailableBikes(${index})">
                            <option value="">${i18n.genderSelect}</option>
                            <option value="MALE">${i18n.genderMale}</option>
                            <option value="FEMALE">${i18n.genderFemale}</option>
                        </select>
                    </div>

                    <div class="col-md-6">
                        <label class="form-label">
                            ${i18n.age} <span class="text-danger">${i18n.required}</span>
                        </label>
                        <input type="number"
                               class="form-control"
                               name="participants[${index}].age"
                               min="12"
                               max="99"
                               required
                               onchange="updateAvailableBikes(${index})"
                               placeholder="${i18n.placeholderAge}">
                    </div>

                    <div class="col-md-6">
                        <label class="form-label">
                            ${i18n.height} <span class="text-danger">${i18n.required}</span>
                        </label>
                        <input type="number"
                               class="form-control"
                               name="participants[${index}].height"
                               min="130"
                               max="220"
                               required
                               onchange="updateAvailableBikes(${index})"
                               placeholder="${i18n.placeholderHeight}">
                    </div>

                    <div class="col-md-6">
                        <label class="form-label">
                            ${i18n.experience} <span class="text-danger">${i18n.required}</span>
                        </label>
                        <select class="form-select"
                                name="participants[${index}].experienceLevel"
                                required>
                            <option value="">${i18n.experienceSelect}</option>
                            <option value="NONE">${i18n.experienceNone}</option>
                            <option value="BEGINNER">${i18n.experienceBeginner}</option>
                            <option value="INTERMEDIATE">${i18n.experienceIntermediate}</option>
                            <option value="ADVANCED">${i18n.experienceAdvanced}</option>
                        </select>
                    </div>
                </div>
            </div>

            <div class="col-lg-4">
                <label class="form-label">
                    ${i18n.bikeSelect} <span class="text-danger">${i18n.required}</span>
                </label>
                <div id="bikes-${index}" class="bikes-selection">
                    ${generateBikeOptions(index)}
                </div>
            </div>
        </div>
    </div>
`;

    container.insertAdjacentHTML('beforeend', participantHtml);

    // Initialize filters for new participant
    filterBikes(index);

    validateForm();
    updateTotalPrice();
    validateClientAge(); // Recheck age restriction
}

// Generate bike options HTML
function generateBikeOptions(index) {
    return availableBikes.map(bike => `
    <div class="form-check bike-checkbox">
        <input class="form-check-input"
               type="radio"
               name="participants[${index}].bikeId"
               id="bike-${index}-${bike.id}"
               value="${bike.id}"
               data-limit-height-min="${bike.limit.heightMin ?? ''}"
               data-limit-height-max="${bike.limit.heightMax ?? ''}"
               data-limit-age-min="${bike.limit.ageMin}"
               data-limit-only-men="${bike.limit.onlyMen}"
               onchange="updateAvailableBikes(${index})"
               required>
        <div class="bike-selected-badge position-absolute top-0 end-0 m-1 d-none">
            <span class="badge bg-primary rounded-circle p-1">
                <i class="fas fa-check"></i>
            </span>
        </div>
        <label class="form-check-label w-100" for="bike-${index}-${bike.id}">
            <div class="d-flex align-items-center">
                <img src="${bike.photoPath}"
                     class="me-2 rounded"
                     style="width: 50px; height: 40px; object-fit: cover;"
                     alt="${i18n.altBikePhoto}">
                <div class="flex-grow-1 d-flex flex-column">
                    <small class="text-muted">${bike.engineCc} ${i18n.bikeCc}, ${bike.transmissionType === 'AUTOMATIC' ? i18n.transmissionAutomatic : i18n.transmissionManual}</small>
                    <small class="text-muted">
                        ${fmt(i18n.bikeYearsFrom, bike.limit.ageMin)}${(() => {
                            const min = bike.limit.heightMin;
                            const max = bike.limit.heightMax;
                            if (min != null && max != null) return ', ' + fmt(i18n.bikeHeightRange, min, max);
                            if (min != null) return ', ' + fmt(i18n.bikeHeightFrom, min);
                            if (max != null) return ', ' + fmt(i18n.bikeHeightUpto, max);
                            return '';
                        })()}
                    </small>
                </div>
            </div>
        </label>
    </div>
`).join('');
}

// Remove participant function
function removeParticipant(index) {
    const participant = document.getElementById('participant-' + index);
    if (participant) {
        participant.remove();

        // Refilter all participants to update occupied bikes
        const allParticipants = document.querySelectorAll('.participant-block');
        allParticipants.forEach((block) => {
            const blockIndex = parseInt(block.id.replace('participant-', ''));
            filterBikes(blockIndex);
        });

        validateForm();
        updateTotalPrice();
        validateClientAge(); // Recheck age restriction
    }
}

// Check policy consent
function onConsentChange() {
    const consentError = document.getElementById('consentError');
    if (document.getElementById('consentCheckbox').checked) {
        consentError.classList.add('d-none');
    }
    validateForm();
}

// Validate form
function validateForm() {
    const form = document.getElementById('bookingForm');
    const submitBtn = document.getElementById('submitBtn');

    // Check consent checkbox
    const consentCheckbox = document.getElementById('consentCheckbox');
    if (!consentCheckbox.checked) {
        submitBtn.disabled = true;
        return false;
    }

    // Check form validity
    if (form.checkValidity()) {
        submitBtn.disabled = false;
        return true;
    } else {
        submitBtn.disabled = true;
        return false;
    }
}

// Update total price
function updateTotalPrice() {
    const participantCount = document.querySelectorAll('.participant-block').length;
    const totalPrice = selectedRoutePrice * participantCount;

    document.getElementById('participantCount').textContent = participantCount;
    document.getElementById('totalPrice').textContent = totalPrice.toFixed(0);
}

// ===== 3.5 Обработка отправки формы =====
function handleFormSubmit(e) {
    e.preventDefault();

    // Явная проверка чекбокса с показом inline-ошибки
    const consentCheckbox = document.getElementById('consentCheckbox');
    if (!consentCheckbox.checked) {
        document.getElementById('consentError').classList.remove('d-none');
        document.getElementById('consentBlock').scrollIntoView({ behavior: 'smooth' });
        return;
    }

    if (!validateForm()) {
        alert(i18n.fillRequired);
        return;
    }

    // Собрать данные формы
    const formData = {
        date: document.querySelector('[name="date"]').value,
        shiftId: parseInt(document.querySelector('[name="shiftId"]').value),
        consentGiven: true,
        client: {
            name: document.querySelector('[name="client.name"]').value,
            email: document.querySelector('[name="client.email"]').value,
            phone: document.querySelector('[name="client.phone"]').value || null,
            telegramId: document.querySelector('[name="client.telegramId"]').value || null
        },
        participants: []
    };

    // Маршрут передаётся только на странице специального заезда
    const routeIdInput = document.querySelector('[name="routeId"]');
    if (routeIdInput) {
        formData.routeId = parseInt(routeIdInput.value);
    }

    // Собрать всех участников
    const participantBlocks = document.querySelectorAll('.participant-block');
    participantBlocks.forEach((block, index) => {
        const actualIndex = parseInt(block.id.replace('participant-', ''));

        const genderSelect = block.querySelector(`select[name*="gender"]`);
        const ageInput = block.querySelector(`input[name*="age"]`);
        const heightInput = block.querySelector(`input[name*="height"]`);
        const experienceLevelSelect = block.querySelector(`select[name*="experienceLevel"]`);
        const bikeInput = block.querySelector(`input[type="radio"]:checked`);

        if (genderSelect && ageInput && heightInput && experienceLevelSelect && bikeInput) {
            formData.participants.push({
                gender: genderSelect.value,
                age: parseInt(ageInput.value),
                height: parseInt(heightInput.value),
                experienceLevel: experienceLevelSelect.value,
                bikeId: parseInt(bikeInput.value)
            });
        }
    });

    // Disable submit button to prevent double submission
    const submitBtn = document.getElementById('submitBtn');
    const submitBtnHtml = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>' + i18n.processing;

    // POST запрос на /booking
    fetch('/booking', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json', [getCsrfHeaderName()]: getCsrfToken() },
        body: JSON.stringify(formData)
    })
        .then(response => {
            const ct = response.headers.get('content-type') || '';
            if (response.redirected) { window.location.href = response.url; return; }
            if (ct.includes('application/json')) return response.json();
            return response.text().then(t => { throw new Error('Server returned HTML'); });
        })
        .then(data => {
            if (data && data.bookingId) {
                window.location.href = `/booking/confirmation/${data.bookingId}`;
            } else {
                throw new Error(data?.message || i18n.errorCreating);
            }
        })
        .catch(err => {
            alert(i18n.errorCreating + ': ' + err.message);
            submitBtn.disabled = false;
            submitBtn.innerHTML = submitBtnHtml;
        });
}

function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]')?.content ?? '';
}
function getCsrfHeaderName() {
    return document.querySelector('meta[name="_csrf_header"]')?.content ?? 'X-XSRF-TOKEN';
}
