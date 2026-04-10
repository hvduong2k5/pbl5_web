async function initApp() {
    try {
        console.log('[DEBUG] Bắt đầu tải dữ liệu khởi tạo (initApp)...');
        
        // Initial fetch
        const currentBatch = await API.getCurrentBatch();
        console.log('[DEBUG] Thông tin Batch hiện tại từ API:', currentBatch);
        Store.setBatch(currentBatch);
        
        if (currentBatch) {
            console.log(`[DEBUG] Đang fetch toàn bộ Fruits cho batch ID = ${currentBatch.id}`);
            const fruits = await API.getFruitsByBatch(currentBatch.id);
            console.log(`[DEBUG] Dữ liệu Fruits nhận được từ DB:`, fruits);
            Store.setFruits(fruits);
        } else {
            console.warn('[DEBUG] Không có active batch nào trong hệ thống!');
        }
        
        const stats = await API.getStats();
        console.log('[DEBUG] Thông số Stats nhận được:', stats);
        if (stats) Store.updateStats(stats);
    } catch (e) {
        console.error('[ERROR] Lỗi khi khởi tạo app data:', e);
    }
    
    console.log('[DEBUG] Đang khởi tạo kết nối WebSocket...');
    initWebSocket(Store);
    
    if (document.getElementById('queue-detected')) {
        console.log('[DEBUG] Khởi tạo giao diện trang chủ (Home)...');
        initCurrentPage();
        Store.notify(); // trigger initial render
    }
}

document.addEventListener('DOMContentLoaded', () => {
    console.log('[DEBUG] DOM Content Loaded. Kiểm tra trang hiện tại...');
    if (typeof initApp === 'function' && document.getElementById('queue-detected')) {
        console.log('[DEBUG] Trang hiện tại là Home. Bắt đầu initApp().');
        initApp();
    } else if (typeof initHistoryPage === 'function') {
        console.log('[DEBUG] Trang hiện tại là History. Bắt đầu initHistoryPage().');
        initHistoryPage();
    }
});