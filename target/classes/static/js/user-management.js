document.addEventListener('DOMContentLoaded', () => {
    const quickSearch = document.getElementById('quickUserSearch');
    const clearFilters = document.getElementById('clearUserFilters');
    const roleFilters = Array.from(document.querySelectorAll('[data-role-filter]'));
    const rows = Array.from(document.querySelectorAll('.user-row'));
    const noVisibleUsersRow = document.getElementById('noVisibleUsersRow');
    const visibleUsersCount = document.getElementById('visibleUsersCount');
    const visibleAdminsCount = document.getElementById('visibleAdminsCount');
    const visibleRegularUsersCount = document.getElementById('visibleRegularUsersCount');

    let selectedRole = 'ALL';

    function normalize(value) {
        return (value || '')
            .toString()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .toLowerCase();
    }

    function applyFilters() {
        const term = normalize(quickSearch?.value);
        let users = 0;
        let admins = 0;
        let regularUsers = 0;

        rows.forEach((row) => {
            const rowText = normalize(row.textContent);
            const role = row.dataset.userRole || '';
            const matchesSearch = !term || rowText.includes(term);
            const matchesRole = selectedRole === 'ALL' || role === selectedRole;
            const visible = matchesSearch && matchesRole;

            row.classList.toggle('d-none', !visible);

            if (!visible) {
                return;
            }

            users += 1;

            if (role === 'ADMIN') {
                admins += 1;
                return;
            }

            regularUsers += 1;
        });

        if (visibleUsersCount) {
            visibleUsersCount.textContent = users;
        }

        if (visibleAdminsCount) {
            visibleAdminsCount.textContent = admins;
        }

        if (visibleRegularUsersCount) {
            visibleRegularUsersCount.textContent = regularUsers;
        }

        noVisibleUsersRow?.classList.toggle('d-none', users > 0 || rows.length === 0);
    }

    quickSearch?.addEventListener('input', applyFilters);

    roleFilters.forEach((button) => {
        button.addEventListener('click', () => {
            selectedRole = button.dataset.roleFilter || 'ALL';

            roleFilters.forEach((filter) => {
                filter.classList.toggle('active', filter === button);
            });

            applyFilters();
        });
    });

    clearFilters?.addEventListener('click', () => {
        if (quickSearch) {
            quickSearch.value = '';
        }

        selectedRole = 'ALL';

        roleFilters.forEach((filter) => {
            filter.classList.toggle('active', filter.dataset.roleFilter === 'ALL');
        });

        applyFilters();
    });

    applyFilters();
});
