const API_BASE_URL = '/api';
const WS_URL = `ws://${window.location.host}/ws`;

const EVENTS = {
    DETECTED: 'detected',
    CLASSIFIED: 'classified',
    SORTED: 'sorted',
    TRANSFER: 'transfer'
};

const QUEUES = {
    [EVENTS.DETECTED]: 'queue-detected',
    [EVENTS.TRANSFER]: 'queue-transfer',
    [EVENTS.SORTED]: 'queue-sorted'
};