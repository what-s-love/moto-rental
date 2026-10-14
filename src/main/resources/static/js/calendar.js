// Страница календаря заездов (FullCalendar)
// Данные с сервера передаются через window.CalendarConfig в шаблоне calendar.html
document.addEventListener('DOMContentLoaded', function() {
    const calendarEl = document.getElementById('calendar');
    const { rides: ridesData, shifts: allShifts, i18n } = window.CalendarConfig;

    const events = ridesData.map(ride => ({
        id: 'ride-' + ride.rideId,
        title: `${ride.startTime.substring(0, 5)}-${ride.endTime.substring(0, 5)} | ${ride.participantCount} ${i18n.peopleShort}`,
        start: ride.date,
        allDay: true,
        backgroundColor: ride.isFull ? '#0059ff' : '#22bbff',
        borderColor: ride.isFull ? '#0059ff' : '#22bbff',
        extendedProps: {
            rideId: ride.rideId,
            shiftId: ride.shiftId,
            shiftName: ride.shiftName,
            participantCount: ride.participantCount,
            availableBikes: ride.availableBikes,
            totalBikes: ride.totalBikes,
            routeName: ride.routeName,
            routeDescription: ride.routeDescription,
            isFull: ride.isFull,
            startTime: ride.startTime.substring(0, 5),
            endTime: ride.endTime.substring(0, 5)
        }
    }));

    const calendar = new FullCalendar.Calendar(calendarEl, {
        themeSystem: 'bootstrap5',
        initialView: 'dayGridMonth',
        locale: 'ru',
        firstDay: 1,
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,listWeek'
        },
        buttonText: {
            today: i18n.today,
            month: i18n.month,
            list: i18n.list
        },
        height: 'auto',
        events: events,
        eventContent: function(arg) {
            const props = arg.event.extendedProps;
            return {
                html: `
                    <div class="fc-event-main-frame">
                        <div class="fc-event-title-container">
                            <div class="fc-event-title fc-sticky d-flex justify-content-between">
                                <div>
                                    <i class="fas fa-clock me-1"></i>
                                    ${props.startTime}-${props.endTime}
                                </div>
                                <div>
                                    ${props.participantCount} <i class="fas fa-users me-1"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                `
            };
        },
        eventClick: function(info) {
            showRideModal(info.event.extendedProps);
        },
        dateClick: function(info) {
            showShiftModal(info.dateStr);
        }
    });

    calendar.render();

    function showRideModal(props) {
        document.getElementById('rideModalShift').textContent = props.shiftName;
        document.getElementById('rideModalTime').textContent = `${props.startTime} - ${props.endTime}`;
        document.getElementById('rideModalParticipants').textContent = props.participantCount;

        const availableEl = document.getElementById('rideModalAvailable');
        availableEl.textContent = props.availableBikes;
        availableEl.classList.toggle('text-danger', props.availableBikes === 0);
        availableEl.classList.toggle('fw-semibold', props.availableBikes === 0);

        const statusEl = document.getElementById('rideModalStatus');
        statusEl.textContent = props.isFull ? i18n.statusFull : i18n.statusAvailable;
        statusEl.classList.remove('text-danger', 'text-success', 'fw-semibold');
        if (props.isFull) {
            statusEl.classList.add('text-danger', 'fw-semibold');
        } else {
            statusEl.classList.add('text-success');
        }

        bootstrap.Modal.getOrCreateInstance(document.getElementById('rideModal')).show();
    }

    function showShiftModal(dateStr) {
        const date = new Date(dateStr + 'T00:00:00');
        const formattedDate = date.toLocaleDateString('ru-RU', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });

        document.getElementById('selectedDate').textContent = formattedDate;

        // Находим заезды для выбранной даты
        const ridesForDate = ridesData.filter(ride => ride.date === dateStr);

        const shiftsContainer = document.getElementById('shiftsContainer');
        shiftsContainer.innerHTML = '';

        allShifts.forEach(shift => {
            // Проверяем, есть ли заезд для этой смены
            const ride = ridesForDate.find(r => r.shiftId === shift.id);

            const button = document.createElement('button');
            button.className = 'btn btn-outline-primary shift-button';

            if (ride) {
                // Есть заезд - показываем информацию
                const isFull = ride.isFull;
                button.className = isFull ? 'btn btn-success' : 'btn btn-primary';

                button.innerHTML = `
                    <div class="d-flex justify-content-between align-items-center">
                        <div class="text-start">
                            <strong>${shift.name}</strong><br>
                            <small><i class="fas fa-clock me-1"></i>${shift.startTime} - ${shift.endTime}</small>
                        </div>
                        <div class="text-end">
                            <span class="badge bg-custom-primary">
                                <i class="fas fa-users me-1"></i>${ride.participantCount} ${i18n.peopleShort}
                            </span><br>
                            <small>${ride.availableBikes} ${i18n.availableShort}</small>
                        </div>
                    </div>
                `;

                if (isFull) {
                    button.disabled = true;
                    button.innerHTML += `<div class="mt-1"><small>${i18n.noSpots}</small></div>`;
                }
            } else {
                // Нет заезда - показываем только смену
                button.innerHTML = `
                    <div class="text-start">
                        <strong>${shift.name}</strong><br>
                        <small><i class="fas fa-clock me-1"></i>${shift.startTime} - ${shift.endTime}</small>
                    </div>
                `;
            }

            button.onclick = function() {
                if (!button.disabled) {
                    window.location.href = `/ride?date=${dateStr}&shiftId=${shift.id}`;
                }
            };

            shiftsContainer.appendChild(button);
        });

        const modal = new bootstrap.Modal(document.getElementById('shiftModal'));
        modal.show();
    }
});
