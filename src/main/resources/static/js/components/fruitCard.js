function createFruitCard(fruit, queueType) {
    const el = document.createElement('div');

    // All queues use card style now
    el.className = 'fruit-card';
    el.id = `fruit-${fruit.id}`;

    let imgHtml = '';
    if (fruit.imageUrl) {
        imgHtml = `
            <div class="fruit-card-img-wrapper">
                <img src="${fruit.imageUrl}" alt="${fruit.id}" loading="lazy">
                <span class="fruit-card-id-overlay">ID: #${fruit.id} ${fruit.espId ? `(ESP_ID: ${fruit.espId})` : ''}</span>
            </div>`;
    } else {
        imgHtml = `
            <div class="fruit-card-img-wrapper">
                <div class="fruit-no-img">Không có ảnh</div>
                <span class="fruit-card-id-overlay">ID: #${fruit.id} ${fruit.espId ? `(ESP_ID: ${fruit.espId})` : ''}</span>
            </div>`;
    }

    const labelVi = getLabelVietnamese(fruit.label);
    const badgeClass = getBadgeClass(fruit.label);

    const createdTime = formatTime(fruit.createdAt) || '---';
    const classifiedTime = formatTime(fruit.classifiedAt) || '---';
    const sortedTime = formatTime(fruit.sortedAt) || '---';
    
    const confidenceHtml = fruit.confidence !== undefined && fruit.confidence !== null 
        ? (fruit.confidence * 100).toFixed(1) + '%' 
        : '---';

    let statusText = 'Đang phân tích';
    if (fruit.status === 'SORTED') statusText = 'Đã vào hộp';
    else if (fruit.status === 'TRANSFERRED') statusText = 'Đang chuyển';
    else if (fruit.status === 'CLASSIFIED') statusText = 'Đã phân loại';
    else if (fruit.status === 'DETECTED') statusText = 'Đã phát hiện';

    const bodyHtml = `
        <div class="fruit-card-row">
            <b>Phân loại / Trạng thái</b>
            <span class="dot"></span>
            ${fruit.label 
                ? `<span class="badge ${badgeClass}">${labelVi}</span>` 
                : `<span style="color:var(--text-muted)">${statusText}</span>`}
        </div>
        <div class="fruit-card-row">
            <b>Phát hiện lúc</b>
            <span>${createdTime}</span>
        </div>
        <div class="fruit-card-row">
            <b>Phân loại lúc</b>
            <span>${classifiedTime}</span>
        </div>
        <div class="fruit-card-row">
            <b>Vào hộp lúc</b>
            <span>${sortedTime}</span>
        </div>
        <div class="fruit-card-row">
            <b>Độ tin cậy</b>
            <span>${confidenceHtml}</span>
        </div>
    `;

    el.innerHTML = imgHtml + `<div class="fruit-card-body">${bodyHtml}</div>`;
    return el;
}