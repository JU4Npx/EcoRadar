const copyTrackingCode = document.getElementById('copyTrackingCode');

copyTrackingCode.addEventListener('click', async () => {
    try {
        await navigator.clipboard.writeText(copyTrackingCode.dataset.protocol);
        copyTrackingCode.innerHTML = '<i class="bi bi-check-lg"></i>';
        copyTrackingCode.setAttribute('aria-label', 'Protocolo copiado');
    } catch {
        copyTrackingCode.title = 'Não foi possível copiar o protocolo';
    }
});