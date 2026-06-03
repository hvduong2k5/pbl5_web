function createFruitCard(fruit, queueType) {
    const el = document.createElement('div');
    
    // Queue 3 (sorted) - compact list style
    if (queueType === 'sorted') {
        el.className = 'sorted-item';
        const labelVi = getLabelVietnamese(fruit.label);
        const badgeClass = getBadgeClass(fruit.label);
        const confidence = fruit.confidence !== undefined && fruit.confidence !== null
            ? (fruit.confidence * 100).toFixed(0) + '%' : 'N/A';
        const time = formatTime(fruit.sortedAt || fruit.classifiedAt || fruit.createdAt);
        
        el.innerHTML = `
            <span class="sorted-item-id">ID: #${fruit.id}</span>
            <span class="sorted-item-time">${time}</span>
            <span class="sorted-item-label">
                ${fruit.label ? `<span class="badge ${badgeClass}">${labelVi}</span>` : ''}
            </span>
            <span class="sorted-item-confidence">${confidence}</span>
        `;
        return el;
    }
    
    // Queue 1 (detected/classified) and Queue 2 (transfer) - card style
    el.className = 'fruit-card';
    el.id = `fruit-${fruit.id}`;

    let imgHtml = '';
    if (fruit.imageUrl) {
        imgHtml = `
            <div class="fruit-card-img-wrapper">
                <img src="${fruit.imageUrl}" alt="${fruit.id}" loading="lazy">
                <span class="fruit-card-id-overlay">ID: #${fruit.id}</span>
            </div>`;
    } else {
        imgHtml = `
            <div class="fruit-card-img-wrapper">
                <div class="fruit-no-img">Không có ảnh</div>
                <span class="fruit-card-id-overlay">ID: #${fruit.id}</span>
            </div>`;
    }

    let bodyHtml = '';
    
    if (queueType === 'detected') {
        // Queue 1 - Detected/Classified
        const labelVi = getLabelVietnamese(fruit.label);
        const badgeClass = getBadgeClass(fruit.label);
        
        bodyHtml = `
            <div class="fruit-card-row">
                <b>Trạng thái</b>
                <span class="dot"></span>
                ${fruit.label 
                    ? `<span class="badge ${badgeClass}">${labelVi}</span>` 
                    : '<span style="color:var(--text-muted)">Đang phân tích</span>'}
            </div>
            <div class="fruit-card-row">
                <b>Độ chín</b>
                <span>${fruit.confidence !== undefined && fruit.confidence !== null ? (fruit.confidence * 100).toFixed(1) + '%' : '---'}</span>
            </div>
        `;
    } else if (queueType === 'transfer') {
        // Queue 2 - Transfer
        const labelVi = getLabelVietnamese(fruit.label);
        const badgeClass = getBadgeClass(fruit.label);
        
        bodyHtml = `
            <div class="fruit-card-row">
                <b>Phân loại</b>
                <span class="dot"></span>
                ${fruit.label 
                    ? `<span class="badge ${badgeClass}">${labelVi}</span>` 
                    : '<span style="color:var(--text-muted)">---</span>'}
            </div>
            <div class="fruit-card-row">
                <b>Độ chín</b>
                <span>${fruit.confidence !== undefined && fruit.confidence !== null ? (fruit.confidence * 100).toFixed(0) + '%' : '---'}</span>
            </div>
        `;
    }

    el.innerHTML = imgHtml + `<div class="fruit-card-body">${bodyHtml}</div>`;
    return el;
}