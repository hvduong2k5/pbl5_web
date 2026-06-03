function renderQueue(queueId, title, fruits, eventType, icon) {
    const queueEl = document.getElementById(queueId);
    if (!queueEl) return;
    
    // Filter fruits for this queue
    const queueFruits = Object.values(fruits).filter(f => {
        if (f.lastEvent) {
            if (eventType === 'detected') return f.lastEvent === 'detected' || f.lastEvent === 'classified';
            if (eventType === 'transfer') return f.lastEvent === 'transfer';
            if (eventType === 'sorted') return f.lastEvent === 'sorted';
            return false;
        }
        if (eventType === 'detected') return f.status === 'DETECTED' || f.status === 'CLASSIFIED';
        if (eventType === 'transfer') return f.status === 'TRANSFERRED';
        if (eventType === 'sorted') return f.status === 'SORTED';
        return false;
    });

    // Setup header once
    if (!queueEl.querySelector('.queue-header')) {
        queueEl.innerHTML = `
            <div class="queue-header">
                <span class="queue-icon">${icon || '📋'}</span>
                <span>${title}</span>
                <span class="queue-count" id="${queueId}-count">${queueFruits.length}</span>
            </div>
            <div class="queue-body" id="${queueId}-body"></div>
        `;
    } else {
        // Update count
        const countEl = document.getElementById(`${queueId}-count`);
        if (countEl) countEl.textContent = queueFruits.length;
    }
    
    const bodyEl = document.getElementById(`${queueId}-body`);
    bodyEl.innerHTML = '';

    queueFruits.forEach(f => {
        bodyEl.appendChild(createFruitCard(f, eventType));
    });
}