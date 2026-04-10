function initCurrentPage() {
    Store.subscribe((state) => {
        renderHeader(window.location.pathname, state.currentBatch ? state.currentBatch.name : null);
        renderStats(state.stats);
        renderQueue(QUEUES[EVENTS.DETECTED], 'Detected', state.fruits, EVENTS.DETECTED);
        renderQueue(QUEUES[EVENTS.TRANSFER], 'Transfered', state.fruits, EVENTS.TRANSFER);
        renderQueue(QUEUES[EVENTS.SORTED], 'Sorted', state.fruits, EVENTS.SORTED);
    });

    document.getElementById('btn-start').addEventListener('click', () => {
        API.sendCommand({ command: 'start' });
    });
    
    document.getElementById('btn-stop').addEventListener('click', () => {
        API.sendCommand({ command: 'stop' });
    });
    
    document.getElementById('btn-new-batch').addEventListener('click', async () => {
        const name = prompt("Enter new batch name (optional):");
        if (name !== null) {
            const newBatch = await API.createNewBatch(name);
            Store.setBatch(newBatch);
            Store.setFruits([]);
        }
    });
}