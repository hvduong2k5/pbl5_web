const Store = {
    state: {
        currentBatch: null,
        fruits: {}, // id -> fruit data
        stats: {
            ripe: 0, unripe: 0, reject: 0, total: 0, wait: 0
        },
        systemStatus: localStorage.getItem('systemStatus') || null
    },
    listeners: [],
    subscribe(listener) {
        this.listeners.push(listener);
    },
    notify() {
        this.listeners.forEach(l => l(this.state));
    },
    setBatch(batch) {
        this.state.currentBatch = batch;
        this.notify();
    },
    setFruits(fruitsList) {
        this.state.fruits = {};
        fruitsList.forEach(f => {
            this.state.fruits[f.id] = f;
        });
        this.notify();
    },
    updateSystemStatus(status) {
        this.state.systemStatus = status;
        if (status) {
            localStorage.setItem('systemStatus', status);
        } else {
            localStorage.removeItem('systemStatus');
        }
        this.notify();
    },
    updateFruit(fruitEvent) {
        const id = fruitEvent.id;
        
        if (!this.state.fruits[id]) {
            this.state.fruits[id] = { id: id };
        }
        
        const fruit = this.state.fruits[id];

        if (fruitEvent.espId) fruit.espId = fruitEvent.espId;
        if (fruitEvent.status) fruit.status = fruitEvent.status;
        if (fruitEvent.label) fruit.label = fruitEvent.label;
        
        if (fruitEvent.sortedType) fruit.sortedType = fruitEvent.sortedType;
        if (fruitEvent.imageUrl) fruit.imageUrl = fruitEvent.imageUrl;
        
        if (fruitEvent.confidence !== undefined) fruit.confidence = fruitEvent.confidence;
        
        if (fruitEvent.createdAt) fruit.createdAt = fruitEvent.createdAt;
        if (fruitEvent.classifiedAt) fruit.classifiedAt = fruitEvent.classifiedAt;
        if (fruitEvent.sortedAt) fruit.sortedAt = fruitEvent.sortedAt;

        // Fallback for older WebSocket events (if full status is missing)
        if (!fruitEvent.status && fruitEvent.event) {
            if (fruitEvent.event === 'detected') fruit.status = 'DETECTED';
            else if (fruitEvent.event === 'classified') fruit.status = 'CLASSIFIED';
            else if (fruitEvent.event === 'transfer') fruit.status = 'TRANSFERRED';
            else if (fruitEvent.event === 'sorted') fruit.status = 'SORTED';
        }

        if (fruitEvent.event) {
            fruit.lastEvent = fruitEvent.event;
        }
        this.notify();
    },
    updateStats(stats) {
        this.state.stats = stats;
        this.notify();
    }
};