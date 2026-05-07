function initWebSocket(store) {
    const token = localStorage.getItem('accessToken');
    if (!token) {
        console.warn('[SECURITY] Không tìm thấy Token, từ chối kết nối WebSocket.');
        return;
    }

    const wsUrl = `ws://${window.location.host}/ws?token=${token}`;
    const ws = new WebSocket(wsUrl);
    
    ws.onopen = () => {
        console.log('[DEBUG WebSocket] Kết nối thành công.');
    };

    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);
        if (data.stats) {
            console.log('[DEBUG WebSocket] Nhận cập nhật Stats từ Server:', data.stats);
            store.updateStats(data.stats);
        } else {
            console.log('[DEBUG WebSocket] Nhận Fruit Event từ Server:', data);
            // It's a fruit event
            store.updateFruit(data);
        }
    };
    
    ws.onclose = (event) => {
        if (event.code === 4000 || event.code === 4001 || event.code === 4003 || event.code === 1008 || String(event.reason).includes('401')) {
             console.error('[SECURITY WebSocket] Token không hợp lệ hoặc hết hạn. Từ chối kết nối.');
             alert('Phiên đăng nhập hết hạn hoặc không hợp lệ. Vui lòng đăng nhập lại!');
             localStorage.removeItem('accessToken');
             window.location.href = '/login.html';
             return; // Stop reconnecting loop
        }
        
        console.log(`[DEBUG WebSocket] WS closed (Code: ${event.code}), reconnecting in 3s...`);
        setTimeout(() => initWebSocket(store), 3000);
    };
    
    ws.onerror = (err) => console.error('[ERROR WebSocket] WS error:', err);
}