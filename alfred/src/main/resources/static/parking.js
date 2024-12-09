const parkingButton = document.getElementById("fetch-button");
parkingButton.addEventListener('click', async () => {
    try {
        const response = await fetch('/parking', {
            method: 'POST', // Указываем метод POST
        });
        if (!response.ok) {
            throw new Error(`Ошибка HTTP: ${response.status}`);
        }
        const data = await response.text();
        document.getElementById('header-text').innerText = "Свободно парковочных мест:";
        document.getElementById('count_parking').innerText = data;
    } catch (error) {
        console.error('Ошибка при получении данных:', error);
        document.getElementById('header-text').innerText = 'Ошибка загрузки данных.'+error;
    }
});
