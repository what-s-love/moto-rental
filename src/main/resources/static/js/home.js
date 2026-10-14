// Главная страница: слайдеры мотоциклов и маршрутов, закрытие спец-баннера
document.addEventListener('DOMContentLoaded', function() {
    // Initialize Swiper for bikes
    const bikesSwiper = new Swiper('.bikes-swiper', {
        slidesPerView: 1,
        spaceBetween: 20,
        loop: true,
        autoplay: {
            delay: 2800,
            disableOnInteraction: false,
            pauseOnMouseEnter: true,
        },
        pagination: {
            el: '.swiper-pagination',
            clickable: true,
        },
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
        },
        breakpoints: {
            640: {
                slidesPerView: 2,
                spaceBetween: 20,
            },
            768: {
                slidesPerView: 3,
                spaceBetween: 20,
            },
            1024: {
                slidesPerView: 4,
                spaceBetween: 20,
            },
        },
    });

    // Pause autoplay on bike cards hover
    const bikeCards = document.querySelectorAll('.bikes-swiper .card');
    bikeCards.forEach(card => {
        card.addEventListener('mouseenter', function() {
            bikesSwiper.autoplay.stop();
        });

        card.addEventListener('mouseleave', function() {
            bikesSwiper.autoplay.start();
        });
    });

    // Initialize Swiper for routes
    new Swiper('.routes-swiper', {
        slidesPerView: 1,
        spaceBetween: 20,
        loop: true,
        autoplay: {
            delay: 3200,
            disableOnInteraction: false,
            pauseOnMouseEnter: true,
        },
        pagination: {
            el: '.swiper-pagination',
            clickable: true,
        },
        navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev',
        },
        breakpoints: {
            640: {
                slidesPerView: 2,
                spaceBetween: 20,
            },
            768: {
                slidesPerView: 3,
                spaceBetween: 20,
            },
        },
    });

    // Close special banner (кнопка внутри ссылки — не даём перейти по ней)
    document.querySelectorAll('.banner-close-btn').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();
            e.stopPropagation();
            this.closest('.special-banner-sticky-wrapper').style.display = 'none';
        });
    });
});
