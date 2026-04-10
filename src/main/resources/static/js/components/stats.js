function renderStats(stats) {
    const container = document.getElementById('stats-container');
    if (!container) return;
    
    container.innerHTML = `
        <h3>Statistics</h3>
        <div class="stats-item"><span>Total:</span> <strong>${stats.total}</strong></div>
        <div class="stats-item"><span>Wait:</span> <strong>${stats.wait}</strong></div>
        <div class="stats-item"><span>Ripe:</span> <strong>${stats.ripe}</strong></div>
        <div class="stats-item"><span>Unripe:</span> <strong>${stats.unripe}</strong></div>
        <div class="stats-item"><span>Rotten:</span> <strong>${stats.rotten}</strong></div>
    `;
}