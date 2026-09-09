/**
 * Floating Toast Notification Component
 * Member 4 - Frontend & API Integration Lead
 */

const Toast = {
    init() {
        if (typeof document === 'undefined') return;
        if (!document.getElementById('toast-container')) {
            const container = document.createElement('div');
            container.id = 'toast-container';
            document.body.appendChild(container);
        }
    },

    /**
     * Show notification toast
     * @param {string} message - Toast message text
     * @param {'success'|'danger'|'info'|'warning'} type - Toast type
     * @param {number} duration - Auto dismiss duration in ms
     */
    show(message, type = 'info', duration = 3500) {
        this.init();
        const container = document.getElementById('toast-container');
        if (!container) return;

        const icons = {
            success: '✅',
            danger: '❌',
            info: 'ℹ️',
            warning: '⚠️'
        };

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <span class="toast-icon">${icons[type] || '🔔'}</span>
            <span class="toast-message">${message}</span>
            <button class="toast-close" onclick="this.parentElement.remove()">&times;</button>
        `;

        container.appendChild(toast);

        if (duration > 0) {
            setTimeout(() => {
                if (toast && toast.parentElement) {
                    toast.style.opacity = '0';
                    toast.style.transform = 'translateX(50px)';
                    setTimeout(() => toast.remove(), 300);
                }
            }, duration);
        }
    }
};

if (typeof window !== 'undefined') {
    window.Toast = Toast;
}

if (typeof module !== 'undefined') {
    module.exports = Toast;
}
