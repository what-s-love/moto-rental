var maxParticleCount = 200; // Увеличим количество для лучшего эффекта
var particleSpeed = 5;
var startConfetti;
var stopConfetti;
var toggleConfetti;
var removeConfetti;

(function() {
    startConfetti = startConfettiInner;
    stopConfetti = stopConfettiInner;
    toggleConfetti = toggleConfettiInner;
    removeConfetti = removeConfettiInner;
    var colors = ["DodgerBlue", "OliveDrab", "Gold", "Pink", "SlateBlue", "LightBlue", "Violet", "PaleGreen", "SteelBlue", "SandyBrown", "Chocolate", "Crimson"]
    var streamingConfetti = false;
    var animationTimer = null;
    var particles = [];
    var waveAngle = 0;
    var canvas;
    var context;

    // Функция для правильной настройки размера canvas
    function setupCanvas() {
        if (!canvas) return;

        var displayWidth = canvas.clientWidth;
        var displayHeight = canvas.clientHeight;

        // Проверяем, нужно ли изменять размер canvas
        if (canvas.width !== displayWidth || canvas.height !== displayHeight) {
            canvas.width = displayWidth;
            canvas.height = displayHeight;
        }
    }

    function resetParticle(particle, width, height) {
        particle.color = colors[(Math.random() * colors.length) | 0];
        particle.x = Math.random() * width;
        particle.y = Math.random() * height - height;
        // Еще больше уменьшаем размер частиц
        particle.width = Math.random() * 6 + 4;
        particle.height = Math.random() * 6 + 4;
        particle.tilt = Math.random() * 10 - 10;
        particle.tiltAngleIncrement = Math.random() * 0.07 + 0.05;
        particle.tiltAngle = 0;
        particle.type = Math.floor(Math.random() * 3);
        particle.rotation = Math.random() * Math.PI * 2;
        particle.rotationSpeed = Math.random() * 0.1 - 0.05;
        return particle;
    }

    function startConfettiInner() {
        window.requestAnimFrame = (function() {
            return window.requestAnimationFrame ||
                window.webkitRequestAnimationFrame ||
                window.mozRequestAnimationFrame ||
                window.oRequestAnimationFrame ||
                window.msRequestAnimationFrame ||
                function (callback) {
                    return window.setTimeout(callback, 16.6666667);
                };
        })();

        canvas = document.getElementById("confetti-canvas");
        if (canvas === null) {
            canvas = document.createElement("canvas");
            canvas.setAttribute("id", "confetti-canvas");
            canvas.setAttribute("style", "position:fixed;top:0;left:0;width:100%;height:100%;z-index:1;pointer-events:none;");
            document.body.appendChild(canvas);
        }

        // Настраиваем правильный размер canvas
        setupCanvas();
        context = canvas.getContext("2d");

        // Настройки для четкой отрисовки
        context.imageSmoothingEnabled = false;
        context.webkitImageSmoothingEnabled = false;
        context.mozImageSmoothingEnabled = false;

        // Обработчик изменения размера окна
        window.addEventListener("resize", function() {
            setupCanvas();
        });

        var width = canvas.width;
        var height = canvas.height;

        while (particles.length < maxParticleCount) {
            particles.push(resetParticle({}, width, height));
        }

        streamingConfetti = true;
        if (animationTimer === null) {
            (function runAnimation() {
                setupCanvas(); // Всегда проверяем размер перед отрисовкой
                context.clearRect(0, 0, canvas.width, canvas.height);
                if (particles.length === 0) {
                    animationTimer = null;
                } else {
                    updateParticles();
                    drawParticles(context);
                    animationTimer = requestAnimFrame(runAnimation);
                }
            })();
        }
    }

    function stopConfettiInner() {
        streamingConfetti = false;
    }

    function removeConfettiInner() {
        stopConfetti();
        particles = [];
    }

    function toggleConfettiInner() {
        if (streamingConfetti)
            stopConfettiInner();
        else
            startConfettiInner();
    }

    function drawParticles(context) {
        var particle;
        for (var i = 0; i < particles.length; i++) {
            particle = particles[i];
            context.save();
            context.translate(particle.x + particle.tilt, particle.y);
            context.rotate(particle.rotation);

            context.fillStyle = particle.color;
            context.globalAlpha = 0.9; // Добавляем немного прозрачности

            switch(particle.type) {
                case 0: // Прямоугольник
                    context.fillRect(-particle.width/2, -particle.height/2, particle.width, particle.height);
                    break;
                case 1: // Круг
                    context.beginPath();
                    context.arc(0, 0, Math.min(particle.width, particle.height) / 2, 0, Math.PI * 2);
                    context.fill();
                    break;
                case 2: // Ромб
                    context.beginPath();
                    context.moveTo(0, -particle.height/2);
                    context.lineTo(particle.width/2, 0);
                    context.lineTo(0, particle.height/2);
                    context.lineTo(-particle.width/2, 0);
                    context.closePath();
                    context.fill();
                    break;
            }
            context.restore();
        }
    }

    function updateParticles() {
        var width = canvas.width;
        var height = canvas.height;
        var particle;
        waveAngle += 0.01;

        for (var i = 0; i < particles.length; i++) {
            particle = particles[i];
            if (!streamingConfetti && particle.y < -15) {
                particle.y = height + 100;
            } else {
                particle.tiltAngle += particle.tiltAngleIncrement;
                particle.rotation += particle.rotationSpeed;
                particle.x += Math.sin(waveAngle) * 0.5;
                particle.y += (Math.cos(waveAngle) + particleSpeed) * 0.5;
                particle.tilt = Math.sin(particle.tiltAngle) * 5;
            }

            if (particle.x > width + 20 || particle.x < -20 || particle.y > height) {
                if (streamingConfetti && particles.length <= maxParticleCount) {
                    resetParticle(particle, width, height);
                } else {
                    particles.splice(i, 1);
                    i--;
                }
            }
        }
    }
})();