function initCurrentPage() {
    // Init sidebar
    initSidebar();

    // New batch button handler
    const btnNewBatch = document.getElementById('btn-new-batch');
    if (btnNewBatch) {
        btnNewBatch.addEventListener('click', async () => {
            const name = prompt('Nhập tên lô mới (tùy chọn):');
            if (name !== null) {
                try {
                    const newBatch = await API.createNewBatch(name);
                    Store.setBatch(newBatch);
                    Store.setFruits([]);
                    alert('Tạo lô mới thành công!');
                } catch (e) {
                    alert('Lỗi khi tạo lô mới: ' + e.message);
                }
            }
        });
    }

    Store.subscribe((state) => {
        renderStats(state.stats);
        renderSystemStatus(state.systemStatus);
        renderBatchInfo(state.currentBatch);

        renderQueue(QUEUES[EVENTS.DETECTED], 'Phát hiện / Phân loại', state.fruits, EVENTS.DETECTED, '🔍');
        renderQueue(QUEUES[EVENTS.TRANSFER], 'Đang chuyển', state.fruits, EVENTS.TRANSFER, '🚌');
        renderQueue(QUEUES[EVENTS.SORTED], 'Đã phân loại', state.fruits, EVENTS.SORTED, '✅');
    });
}
