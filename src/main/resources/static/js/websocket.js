function initWebSocket(store) {
    const ws = new WebSocket(`ws://${window.location.host}/ws`);
    
    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        if (data.stats) {
            store.updateStats(data.stats);
        } else {
            // It's a fruit event
            store.updateFruit(data);
        }
    };
    
    ws.onclose = () => {
        console.log('WS closed, reconnecting...');
        setTimeout(() => initWebSocket(store), 3000);
    };
    
    ws.onerror = (err) => console.error('WS error', err);
}