const Store = {
    state: {
        currentBatch: null,
        fruits: {}, // id -> fruit data
        stats: {
            ripe: 0, unripe: 0, rotten: 0, total: 0, wait: 0
        }
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
    updateFruit(fruitEvent) {
        const id = fruitEvent.id;
        if (!this.state.fruits[id]) {
            this.state.fruits[id] = { 
                id: id, 
                status: fruitEvent.event === 'detected' ? 'DETECTED' : (fruitEvent.event === 'classified' ? 'CLASSIFIED' : (fruitEvent.event === 'transfer' ? 'TRANSFERRED' : 'SORTED')), 
                label: fruitEvent.label, 
                sortedType: fruitEvent.type, 
                createdAt: fruitEvent.timestamp,
                imageUrl: fruitEvent.image_url,
                confidence: fruitEvent.confidence
            };
        } else {
            if (fruitEvent.status) this.state.fruits[id].status = fruitEvent.status;
            if (fruitEvent.label) this.state.fruits[id].label = fruitEvent.label;
            if (fruitEvent.type) this.state.fruits[id].sortedType = fruitEvent.type;
            if (fruitEvent.image_url) this.state.fruits[id].imageUrl = fruitEvent.image_url;
            if (fruitEvent.confidence !== undefined) this.state.fruits[id].confidence = fruitEvent.confidence;
            
            if (fruitEvent.event === 'detected') this.state.fruits[id].status = 'DETECTED';
            if (fruitEvent.event === 'classified') this.state.fruits[id].status = 'CLASSIFIED';
            if (fruitEvent.event === 'transfer') this.state.fruits[id].status = 'TRANSFERRED';
            if (fruitEvent.event === 'sorted') this.state.fruits[id].status = 'SORTED';
        }
        if (fruitEvent.event) {
            this.state.fruits[id].lastEvent = fruitEvent.event;
        }
        this.notify();
    },
    updateStats(stats) {
        this.state.stats = stats;
        this.notify();
    }
};