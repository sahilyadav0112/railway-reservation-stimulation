const params = new URLSearchParams(location.search);
const fromStation = params.get("from");
const toStation = params.get("to");
const travelDate = params.get("date");
const results = document.getElementById("results");

// Keep the form filled in after searching
if (fromStation) document.getElementById("from").value = fromStation;
if (toStation) document.getElementById("to").value = toStation;
if (travelDate) document.getElementById("date").value = travelDate;

if (fromStation && toStation && travelDate) {
  if (fromStation === toStation) {
    results.innerHTML = '<p class="notice error">Source and destination cannot be the same.</p>';
  } else {
    const found = TRAINS.filter(t => t.from === fromStation && t.to === toStation);
    if (found.length === 0) {
      results.innerHTML = `<p class="notice">No trains found from ${fromStation} to ${toStation}. Try Mumbai → Delhi or Mumbai → Pune.</p>`;
    } else {
      results.innerHTML =
        `<h2>${found.length} train(s): ${fromStation} → ${toStation} on ${fmtDate(travelDate)}</h2>` +
        found.map(trainCard).join("");
    }
  }
}

function trainCard(t) {
  const classBoxes = Object.keys(CLASSES).map(cls => {
    const seats = availableSeats(t.no, travelDate, cls);
    const badge = seats > 0
      ? `<span class="badge ok">${seats} available</span>`
      : `<span class="badge warn">Waiting list</span>`;
    return `
      <a class="class-box" href="booking.html?train=${t.no}&date=${travelDate}&cls=${cls}">
        <strong>${CLASSES[cls].name}</strong>
        <span class="fare">${money(t.fare[cls])}</span>
        ${badge}
      </a>`;
  }).join("");

  return `
    <div class="card">
      <h3>${t.name} <small>#${t.no}</small></h3>
      <div class="route">
        <div><strong>${t.dep}</strong><br>${t.from}</div>
        <div class="dur">${t.dur}</div>
        <div><strong>${t.arr}</strong><br>${t.to}</div>
      </div>
      <div class="classes">${classBoxes}</div>
    </div>`;
}