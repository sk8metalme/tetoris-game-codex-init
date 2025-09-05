(() => {
  const $ = (sel) => document.querySelector(sel);
  const boardEl = $('#board');
  const statusEl = $('#status');
  const btnStart = $('#btnStart');
  let session = { id: null, rev: null };

  function uuid() {
    return ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
      (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
    );
  }

  async function startGame() {
    const res = await fetch('/api/game/start', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Idempotency-Key': uuid() },
      body: JSON.stringify({})
    });
    session.rev = res.headers.get('ETag');
    const body = await res.json();
    session.id = body.id;
    render(body.state);
    statusEl.textContent = `Started id=${session.id} rev=${session.rev}`;
  }

  async function send(action, repeat=1) {
    if (!session.id) return;
    const res = await fetch(`/api/game/${session.id}/input`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'If-Match': session.rev ?? '',
        'Idempotency-Key': uuid(),
      },
      body: JSON.stringify({ action, repeat })
    });
    if (res.status === 409) {
      statusEl.textContent = '409 CONFLICT: rev mismatch. Refetching...';
      await refresh();
      return;
    }
    session.rev = res.headers.get('ETag') ?? session.rev;
    const body = await res.json();
    render(body.state);
  }

  async function refresh() {
    const res = await fetch(`/api/game/${session.id}/state`);
    session.rev = res.headers.get('ETag') ?? session.rev;
    const body = await res.json();
    render(body.state);
  }

  function render(state) {
    boardEl.style.setProperty('--cols', state.width);
    boardEl.innerHTML = '';
    const types = state.cellTypes || { 0: 'EMPTY', 1: 'LOCKED' };
    // draw board
    for (let y = 0; y < state.height; y++) {
      for (let x = 0; x < state.width; x++) {
        const v = state.board[y][x];
        const cell = document.createElement('div');
        const name = (types[v] || 'EMPTY').toLowerCase();
        cell.className = `cell type-${name}`;
        cell.dataset.x = x; cell.dataset.y = y;
        boardEl.appendChild(cell);
      }
    }
    // overlay current
    if (state.current && Array.isArray(state.current.cells)) {
      for (const p of state.current.cells) {
        const cell = boardEl.querySelector(`.cell[data-x="${p.x}"][data-y="${p.y}"]`);
        if (cell) cell.classList.add('curr');
      }
    }
  }

  function onKey(e) {
    if (!session.id) return;
    if (e.repeat) return;
    switch (e.code) {
      case 'ArrowLeft': send('MOVE_LEFT'); break;
      case 'ArrowRight': send('MOVE_RIGHT'); break;
      case 'ArrowDown': send('SOFT_DROP'); break;
      case 'Space': e.preventDefault(); send('HARD_DROP'); break;
      default: return;
    }
  }

  btnStart.addEventListener('click', startGame);
  window.addEventListener('keydown', onKey);
})();
