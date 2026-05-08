function initCurrentPage() {
    Store.subscribe((state) => {
        renderHeader(window.location.pathname, state.currentBatch ? state.currentBatch.name : null);
        renderStats(state.stats);
        renderQueue(QUEUES[EVENTS.DETECTED], 'Detected / Classified', state.fruits, EVENTS.DETECTED);
        renderQueue(QUEUES[EVENTS.TRANSFER], 'Transfered', state.fruits, EVENTS.TRANSFER);
        renderQueue(QUEUES[EVENTS.SORTED], 'Sorted', state.fruits, EVENTS.SORTED);
        
        const systemStatusEl = document.getElementById('system-status');
        if (systemStatusEl) {
            if (state.systemStatus) {
                systemStatusEl.textContent = `Status: ${state.systemStatus}`;
                systemStatusEl.style.color = state.systemStatus.toLowerCase().includes('run') ? 'var(--primary-color)' : 'var(--danger-color)';
            } else {
                systemStatusEl.textContent = 'Status: Waiting for ESP...';
                systemStatusEl.style.color = 'var(--secondary-color)';
            }
        }
    });

    const btnNewBatch = document.getElementById('btn-new-batch');

    if (!hasAuthority('MANAGE_BATCH') && !hasAuthority('ROLE_ADMIN')) {
        if (btnNewBatch) btnNewBatch.style.display = 'none';
    } else {
        if (btnNewBatch) {
            btnNewBatch.addEventListener('click', async () => {
                const name = prompt("Enter new batch name (optional):");
                if (name !== null) {
                    const newBatch = await API.createNewBatch(name);
                    Store.setBatch(newBatch);
                    Store.setFruits([]);
                }
            });
        }
    }
}
