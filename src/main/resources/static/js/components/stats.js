function renderStats(stats) {
    const container = document.getElementById('stats-container');
    if (!container) return;
    
    container.innerHTML = `
        <div class="panel-title">Thống kê lô hiện tại</div>
        <div class="panel-body">
            <ul class="stats-list">
                <li class="stat-total">
                    <span class="stats-label">Tổng cộng</span>
                    <span class="stats-value">${stats.total || 0}</span>
                </li>
                <li class="stat-wait">
                    <span class="stats-label">Chờ</span>
                    <span class="stats-value">${stats.wait || 0}</span>
                </li>
                <li class="stat-ripe">
                    <span class="stats-label">Chín</span>
                    <span class="stats-value">${stats.ripe || 0}</span>
                </li>
                <li class="stat-unripe">
                    <span class="stats-label">Chưa chín</span>
                    <span class="stats-value">${stats.unripe || 0}</span>
                </li>
                <li class="stat-reject">
                    <span class="stats-label">Loại bỏ</span>
                    <span class="stats-value">${stats.reject || 0}</span>
                </li>
            </ul>
        </div>
    `;
}

function renderSystemStatus(status) {
    const container = document.getElementById('system-status-panel');
    if (!container) return;
    
    const isRunning = status && status.toLowerCase().includes('run');
    const statusText = isRunning ? 'Đang chạy' : (status ? status : 'Đang chờ ESP...');
    const indicatorClass = isRunning ? '' : 'stopped';
    
    container.innerHTML = `
        <div class="panel-title">Trạng thái hệ thống</div>
        <div class="panel-body">
            <div class="system-status-display">
                <div class="system-status-indicator ${indicatorClass}"></div>
                <div class="system-status-text">${statusText}</div>
            </div>
        </div>
    `;
}

function renderBatchInfo(batch) {
    const container = document.getElementById('batch-info-panel');
    if (!container) return;
    
    if (batch) {
        const createdAt = batch.createdAt ? formatDate(batch.createdAt).replace('\n', ' - ') : '';
        container.innerHTML = `
            <div class="panel-title">Lô hiện tại</div>
            <div class="panel-body">
                <div class="batch-info">
                    <div class="batch-name">Lô: ${batch.name || 'N/A'}</div>
                    <div class="batch-detail">${createdAt ? 'Bắt đầu: ' + createdAt : ''}</div>
                </div>
            </div>
        `;
    } else {
        container.innerHTML = `
            <div class="panel-title">Lô hiện tại</div>
            <div class="panel-body">
                <div class="batch-info">
                    <div class="batch-detail" style="color: var(--text-muted);">Không có lô đang hoạt động</div>
                </div>
            </div>
        `;
    }
}