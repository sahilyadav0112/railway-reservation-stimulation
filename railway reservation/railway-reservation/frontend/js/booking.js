const params = new URLSearchParams(location.search);
const train = getTrain(params.get("train"));
const travelDate = params.get("date");
const cls = params.get("cls");
const MAX_PASSENGERS = 6;

if (requireLogin()) init();

function init() {
  if (!train || !travelDate || !CLASSES[cls]) {
    document.querySelector("main").innerHTML =
      '<p class="notice error">Invalid booking link. <a href="search.html">Search trains again</a></p>';
    return;
  }
  renderSummary();
  addPassenger();
  document.getElementById("addPassenger").addEventListener("click", addPassenger);
  document.getElementById("bookingForm").addEventListener("submit", submitBooking);
}

function renderSummary() {
  const seats = availableSeats(train.no, travelDate, cls);
  document.getElementById("trainSummary").innerHTML = `
    <h2>${train.name} <small>#${train.no}</small></h2>
    <p>${train.from} → ${train.to} &nbsp;|&nbsp; ${fmtDate(travelDate)} &nbsp;|&nbsp; ${train.dep} - ${train.arr}</p>
    <p>Class: <strong>${CLASSES[cls].name}</strong> &nbsp;|&nbsp; Fare per passenger: <strong>${money(train.fare[cls])}</strong></p>
    <p>${seats > 0
      ? `<span class="badge ok">${seats} seats available</span>`
      : '<span class="badge warn">Sold out - you will be added to the waiting list</span>'}</p>`;
}

function addPassenger() {
  const box = document.getElementById("passengers");
  const count = box.children.length;
  if (count >= MAX_PASSENGERS) {
    alert("Maximum " + MAX_PASSENGERS + " passengers per booking.");
    return;
  }

  const row = document.createElement("div");
  row.className = "passenger-row";
  row.innerHTML = `
    <h4>Passenger ${count + 1}</h4>
    <div class="grid">
      <div class="field"><label>Name</label><input type="text" class="p-name" required></div>
      <div class="field"><label>Age</label><input type="number" class="p-age" min="1" max="120" required></div>
      <div class="field"><label>Gender</label>
        <select class="p-gender"><option>Male</option><option>Female</option><option>Other</option></select>
      </div>
    </div>
    ${count > 0 ? '<button type="button" class="link-btn remove">Remove passenger</button>' : ""}`;
  box.appendChild(row);

  const removeBtn = row.querySelector(".remove");
  if (removeBtn) {
    removeBtn.addEventListener("click", () => {
      row.remove();
      document.querySelectorAll(".passenger-row h4").forEach((h, i) => (h.textContent = "Passenger " + (i + 1)));
      updateTotal();
    });
  }
  updateTotal();
}

function updateTotal() {
  const n = document.getElementById("passengers").children.length;
  document.getElementById("totalFare").textContent = money(n * train.fare[cls]);
}

function submitBooking(e) {
  e.preventDefault();
  const errorBox = document.getElementById("error");

  const passengers = [...document.querySelectorAll(".passenger-row")].map(row => ({
    name: row.querySelector(".p-name").value.trim(),
    age: Number(row.querySelector(".p-age").value),
    gender: row.querySelector(".p-gender").value,
    status: "NEW",   // allocate() will turn this into CONFIRMED or WL
    seatNo: null,
    wlNo: null
  }));

  if (passengers.some(p => p.name === "")) {
    errorBox.textContent = "Please enter a name for every passenger.";
    return;
  }

  const user = getCurrentUser();
  const booking = {
    pnr: makePNR(),
    userEmail: user.email,
    trainNo: train.no,
    trainName: train.name,
    from: train.from,
    to: train.to,
    dep: train.dep,
    arr: train.arr,
    date: travelDate,
    cls: cls,
    passengers: passengers,
    totalFare: passengers.length * train.fare[cls],
    bookedOn: new Date().toISOString(),
    status: "ACTIVE"
  };

  const bookings = getBookings();
  bookings.push(booking);
  allocate(bookings, train.no, travelDate, cls);   // assign seats / waiting-list numbers
  saveBookings(bookings);

  location.href = "ticket.html?pnr=" + booking.pnr;
}