const parkingButton = document.getElementById("fetch-button");
parkingButton.addEventListener('click', async () => {
    try {
        // Отправляем запрос POST без тела
        const response = await fetch('/parking', {
            method: 'POST', // Указываем метод POST
        });

        if (!response.ok) {
            throw new Error(`Ошибка HTTP: ${response.status}`);
        }

        // Получаем текстовый ответ
        const data = await response.text();

        // Выводим данные в HTML
        document.getElementById('header-text').innerText = "Свободно парковочных мест:";
        document.getElementById('count_parking').innerText = data;

    } catch (error) {
        console.error('Ошибка при получении данных:', error);
        document.getElementById('header-text').innerText = 'Ошибка загрузки данных.'+error;
    }
});
