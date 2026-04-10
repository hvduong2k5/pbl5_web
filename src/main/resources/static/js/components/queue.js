function renderQueue(queueId, title, fruits, eventType) {
    const queueEl = document.getElementById(queueId);
    if (!queueEl) return;
    
    // Setup header once
    if (!queueEl.querySelector('.queue-header')) {
        queueEl.innerHTML = `
            <div class="queue-header">${title}</div>
            <div class="queue-body" id="${queueId}-body"></div>
        `;
    }
    
    const bodyEl = document.getElementById(`${queueId}-body`);
    bodyEl.innerHTML = '';
    
    // Filter fruits for this queue based on lastEvent or status
    const queueFruits = Object.values(fruits).filter(f => {
        // Handle realtime websocket events
        if (f.lastEvent) {
            console.log(`[DEBUG] Fruit ID ${f.id} có WebSocket lastEvent là "${f.lastEvent}". Đang so sánh với queue type "${eventType}"...`);
            if (eventType === 'detected') return f.lastEvent === 'detected' || f.lastEvent === 'classified';
            if (eventType === 'transfer') return f.lastEvent === 'transfer';
            if (eventType === 'sorted') return f.lastEvent === 'sorted';
            return false;
        }
        
        // Handle fallback based on DB status on page load
        console.log(`[DEBUG] Fruit ID ${f.id} tải từ DB có status là "${f.status}". Đang so sánh với queue type "${eventType}"...`);
        if (eventType === 'detected') return f.status === 'DETECTED' || f.status === 'CLASSIFIED';
        if (eventType === 'transfer') return f.status === 'TRANSFERRED';
        if (eventType === 'sorted') return f.status === 'SORTED';
        return false;
    });

    console.log(`[DEBUG] Đưa ${queueFruits.length} quả vào ${title} (Queue: ${eventType}).`);

    queueFruits.forEach(f => {
        bodyEl.appendChild(createFruitCard(f));
    });
}