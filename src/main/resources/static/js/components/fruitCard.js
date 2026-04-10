function createFruitCard(fruit) {
    const el = document.createElement('div');
    el.className = 'fruit-card';
    el.id = `fruit-${fruit.id}`;
    
    // Determine context based on status
    const isQueue1 = fruit.status === 'DETECTED' || fruit.status === 'CLASSIFIED';
    const isQueue2 = fruit.status === 'TRANSFERRED';
    const isQueue3 = fruit.status === 'SORTED';

    let displayStatus = fruit.status;
    if (fruit.status === 'DETECTED') displayStatus = 'WAIT';
    if (fruit.status === 'CLASSIFIED') displayStatus = 'READY';

    let contentHtml = '';
    
    // Image
    if (fruit.imageUrl) {
        contentHtml += `<img src="${fruit.imageUrl}" alt="${fruit.id}" class="fruit-img">`;
    } else {
        contentHtml += `<div class="fruit-no-img">No Image</div>`;
    }

    // ID
    contentHtml += `<div class="card-id"><b>ID:</b> ${fruit.id}</div>`;

    // Queue 1 Logic
    if (isQueue1) {
        contentHtml += `<div><b>Status:</b> <span class="badge ${getBadgeClass(displayStatus)}">${displayStatus}</span></div>`;
        if (fruit.label) {
            contentHtml += `<div><b>Label:</b> <span class="badge ${getBadgeClass(fruit.label)}">${fruit.label.toUpperCase()}</span></div>`;
        }
        contentHtml += `<div><b>Confidence:</b> ${fruit.confidence !== undefined && fruit.confidence !== null ? (fruit.confidence * 100).toFixed(1) + '%' : 'N/A'}</div>`;
    } 
    // Queue 2 Logic
    else if (isQueue2) {
        if (fruit.label) {
            contentHtml += `<div><b>Label:</b> <span class="badge ${getBadgeClass(fruit.label)}">${fruit.label.toUpperCase()}</span></div>`;
        }
        contentHtml += `<div><b>Confidence:</b> ${fruit.confidence !== undefined && fruit.confidence !== null ? (fruit.confidence * 100).toFixed(1) + '%' : 'N/A'}</div>`;
    } 
    // Queue 3 Logic
    else if (isQueue3) {
        if (fruit.label) {
            contentHtml += `<div><b>Label:</b> <span class="badge ${getBadgeClass(fruit.label)}">${fruit.label.toUpperCase()}</span></div>`;
        }
        contentHtml += `<div><b>Status:</b> <span class="badge ${getBadgeClass(fruit.status)}">${fruit.status}</span></div>`;
        contentHtml += `<div><b>Confidence:</b> ${fruit.confidence !== undefined && fruit.confidence !== null ? (fruit.confidence * 100).toFixed(1) + '%' : 'N/A'}</div>`;
        if (fruit.sortedAt) {
            contentHtml += `<div style="font-size: 0.8em; color: #999; margin-top: 5px;">Time: ${formatDate(fruit.sortedAt)}</div>`;
        } else if (fruit.createdAt) {
            contentHtml += `<div style="font-size: 0.8em; color: #999; margin-top: 5px;">Time: ${formatDate(fruit.createdAt)}</div>`;
        }
    }

    el.innerHTML = contentHtml;
    return el;
}