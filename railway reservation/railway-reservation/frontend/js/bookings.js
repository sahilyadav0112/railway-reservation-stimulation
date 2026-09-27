if (requireLogin()) render();

function render() {
  const list = document.getElementById("bookingList");
  const user = getCurrentUser();
  const mine = getBookings().filter(b => b.userEmail === user.email).reverse();   // newest first

  if (mine.length === 0) {
    list.innerHTML = '<p class="notice">You have no bookings yet. <a href="search.html">Book a train</a></p>';
    return;
  }

  list.innerHTML = mine.map(b => `
    <div class="card">
      <div class="booking-head">
        <div><strong>PNR ${b.pnr}</strong><br><small>Booked on ${new Date(b.bookedOn).toLocaleDateString("en-IN")}</small></div>
        <span class="badge ${b.status === "ACTIVE" ? "ok" : "danger"}">${b.status}</span>
      </div>
      <h3>${b.trainName} <small>#${b.trainNo}</small></h3>
      <p>${b.from} → ${b.to} &nbsp;|&nbsp; ${fmtDate(b.date)} &nbsp;|&nbsp; ${CLASSES[b.cls].name}</p>
      <ul class="pax">
        ${b.passengers.map(p => `<li>${esc(p.name)} (${p.age}, ${p.gender}) - <strong>${seatText(b, p)}</strong></li>`).join("")}
      </ul>
      <p>Total: <strong>${money(b.totalFare)}</strong></p>
      <div class="actions">
        <a class="btn btn-outline" href="ticket.html?pnr=${b.pnr}">View Ticket</a>
        ${b.status === "ACTIVE" ? `<button class="btn btn-danger cancel-btn" data-pnr="${b.pnr}">Cancel Booking</button>` : ""}
      </div>
    </div>`).join("");

  list.querySelectorAll(".cancel-btn").forEach(btn =>
    btn.addEventListener("click", () => cancelBooking(btn.dataset.pnr))
  );
}

function cancelBooking(pnr) {
  if (!confirm("Cancel booking " + pnr + "?")) return;

  const bookings = getBookings();
  const b = bookings.find(x => x.pnr === pnr);
  b.status = "CANCELLED";
  allocate(bookings, b.trainNo, b.date, b.cls);   // freed seats go to the waiting list
  saveBookings(bookings);
  render();
}