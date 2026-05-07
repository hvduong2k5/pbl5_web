function initCurrentPage() {
    Store.subscribe((state) => {
        renderHeader(window.location.pathname, state.currentBatch ? state.currentBatch.name : null);
        renderStats(state.stats);
        renderQueue(QUEUES[EVENTS.DETECTED], 'Detected', state.fruits, EVENTS.DETECTED);
        renderQueue(QUEUES[EVENTS.TRANSFER], 'Transfered', state.fruits, EVENTS.TRANSFER);
        renderQueue(QUEUES[EVENTS.SORTED], 'Sorted', state.fruits, EVENTS.SORTED);
    });

    const btnStart = document.getElementById('btn-start');
    const btnStop = document.getElementById('btn-stop');
    const btnNewBatch = document.getElementById('btn-new-batch');

    // Phân quyền hiển thị
    if (!hasAuthority('CONTROL_SYSTEM') && !hasAuthority('ROLE_ADMIN')) {
        if (btnStart) btnStart.style.display = 'none';
        if (btnStop) btnStop.style.display = 'none';
    } else {
        if (btnStart) {
            btnStart.addEventListener('click', () => {
                API.sendCommand({ command: 'start' });
            });
        }
        if (btnStop) {
            btnStop.addEventListener('click', () => {
                API.sendCommand({ command: 'stop' });
            });
        }
    }

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
