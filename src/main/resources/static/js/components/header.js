function renderHeader(currentPath, currentBatchName) {
    const container = document.getElementById('header-container');
    if (!container) return;
    
    const isHome = currentPath === 'home' || currentPath === '/' || currentPath.endsWith('index.html');
    const isHistory = currentPath.endsWith('history.html');
    
    container.innerHTML = `
        <header class="app-header">
            <div class="nav-links">
                <a href="/" class="${isHome ? 'active' : ''}">Home</a>
                <a href="/history.html" class="${isHistory ? 'active' : ''}">History</a>
            </div>
            <div class="current-batch">
                ${currentBatchName ? `Current Batch: ${currentBatchName}` : 'No Active Batch'}
            </div>
        </header>
    `;
}