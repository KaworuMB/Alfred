document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function(event) {
            event.preventDefault(); // Предотвращаем стандартное поведение формы

            // Получаем данные из формы
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value.trim();

            // Проверяем, что поля не пустые
            if (!username || !password) {
                alert('Пожалуйста, заполните все поля.');
                return;
            }

            // Создаем объект с данными для отправки
            const data = { username, password };

            // Отправляем запрос на бэкенд
            fetch('/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(response => response.json())
                .then(result => {
                    if (result.success) {
                        localStorage.setItem('username', username);
                        window.location.href = 'main.html';
                    } else {
                        alert(result.message || 'Ошибка входа. Проверьте данные и попробуйте снова.');
                    }
                })
                .catch(error => {
                    console.error('Ошибка:', error);
                    alert('Произошла ошибка при выполнении запроса.');
                });
        });
    }

    const generateButton = document.getElementById('generateCode');
    const submitButton = document.getElementById('submitCode');
    const generatedCodeSpan = document.getElementById('generatedCode');
    const codeInput = document.getElementById('codeInput');

    // Функция для генерации 9-значного кода
    function generateNineDigitCode() {
        return Array.from({ length: 9 }, () => Math.floor(Math.random() * 10)).join('');
    }

    // Обработчик для кнопки "Сгенерировать код"
    if (generateButton) {
        generateButton.addEventListener('click', function() {
            const code = generateNineDigitCode();
            generatedCodeSpan.textContent = code;
            codeInput.value = code; // Для отправки, если это требуется
        });
    }

    // Обработчик для кнопки "Завершить"
    if (submitButton) {
        submitButton.addEventListener('click', function() {
            const code = codeInput.value.trim();

            // Проверяем, что код состоит из 9 цифр
            if (!/^\d{9}$/.test(code)) {
                alert('Пожалуйста, введите корректный 9-значный код.');
                return;
            }

            // Получаем имя пользователя из localStorage
            const username = localStorage.getItem('username');
            if (!username) {
                alert('Пользователь не найден. Пожалуйста, войдите в систему.');
                window.location.href = '/login.html';
                return;
            }

            // Отправляем данные на сервер
            const data = { username, code };

            fetch('/submitCode', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(response => response.json())
                .then(result => {
                    if (result.success) {
                        alert('Код успешно отправлен!');
                        codeInput.value = '';
                        generatedCodeSpan.textContent = '';
                    } else {
                        alert(result.message || 'Ошибка при отправке кода.');
                    }
                })
                .catch(error => {
                    console.error('Ошибка:', error);
                    alert('Произошла ошибка при отправке данных.');
                });
        });
    }
});
