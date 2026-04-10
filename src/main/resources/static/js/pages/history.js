async function initHistoryPage() {
    console.log('[DEBUG History] Bắt đầu initHistoryPage...');
    const batchSelect = document.getElementById('batch-select');
    const historyBody = document.getElementById('history-body');
    
    // Render header ngay lập tức để tránh giao diện bị trống
    renderHeader(window.location.pathname, null);
    
    try {
        console.log('[DEBUG History] Gọi API lấy danh sách Batches...');
        // Load batches
        const batches = await API.getAllBatches();
        console.log('[DEBUG History] Danh sách Batches nhận được:', batches);
        
        batches.forEach(b => {
            const opt = document.createElement('option');
            opt.value = b.id;
            opt.textContent = b.name;
            batchSelect.appendChild(opt);
        });
        
        console.log('[DEBUG History] Gọi API lấy Current Batch để update Header...');
        // Fetch header
        const currentBatch = await API.getCurrentBatch();
        renderHeader(window.location.pathname, currentBatch ? currentBatch.name : null);
    } catch (e) {
        console.error('[ERROR History] Lỗi khi load dữ liệu History:', e);
    }
    
    async function loadHistory() {
        try {
            const val = batchSelect.value;
            console.log(`[DEBUG History] Đang tải dữ liệu quả cho Batch ID: ${val}`);
            const fruits = await API.getFruitsByBatch(val);
            console.log(`[DEBUG History] Nhận được ${fruits.length} quả từ API.`);
            
            historyBody.innerHTML = '';
            fruits.forEach(f => {
                const tr = document.createElement('tr');
                
                // Image styling for history table
                const imgHtml = f.imageUrl 
                    ? `<img src="${f.imageUrl}" alt="${f.id}" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;">`
                    : `<div style="width: 50px; height: 50px; background: #eee; display: flex; align-items: center; justify-content: center; border-radius: 4px; font-size: 10px; color: #888;">N/A</div>`;
                
                const confidenceHtml = f.confidence !== undefined && f.confidence !== null 
                    ? `${(f.confidence * 100).toFixed(1)}%` 
                    : 'N/A';

                tr.innerHTML = `
                    <td>${f.id}</td>
                    <td>${imgHtml}</td>
                    <td><span class="badge ${getBadgeClass(f.status)}">${f.status || ''}</span></td>
                    <td><span class="badge ${getBadgeClass(f.label)}">${f.label ? f.label.toUpperCase() : ''}</span></td>
                    <td>${formatDate(f.createdAt)}</td>
                    <td>${formatDate(f.classifiedAt)}</td>
                    <td>${formatDate(f.sortedAt)}</td>
                    <td>${confidenceHtml}</td>
                `;
                historyBody.appendChild(tr);
            });
        } catch (e) {
            console.error('[ERROR History] Lỗi khi render bảng History:', e);
        }
    }
    
    batchSelect.addEventListener('change', loadHistory);
    loadHistory();
}

console.log('[DEBUG History] File history.js đã được parse. Đăng ký sự kiện DOMContentLoaded.');
document.addEventListener('DOMContentLoaded', initHistoryPage);
