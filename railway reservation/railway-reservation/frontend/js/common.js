/* ============================================================
   common.js - shared data + helper functions used by every page.
   All data is stored in the browser's localStorage (no server).
   ============================================================ */

// ---------- Sample data ----------
const STATIONS = ["Ahmedabad", "Bengaluru", "Chennai", "Delhi", "Hyderabad", "Jaipur", "Kolkata", "Mumbai", "Pune"];

// seats = seats per train, per date, per class (kept small so you can test the waiting list)
const CLASSES = {
  SL:   { name: "Sleeper",   seats: 12, coach: "S1" },
  "3A": { name: "AC 3 Tier", seats: 8,  coach: "B1" },
  "2A": { name: "AC 2 Tier", seats: 6,  coach: "A1" }
};

const ROUTES = [
  { no: 12951, name: "Mumbai Rajdhani",    from: "Mumbai",    to: "Delhi",     dep: "16:35", arr: "08:15", dur: "15h 40m", fare: { SL: 1150, "3A": 2100, "2A": 3000 } },
  { no: 12953, name: "August Kranti",      from: "Mumbai",    to: "Delhi",     dep: "17:40", arr: "12:45", dur: "19h 05m", fare: { SL: 950,  "3A": 1750, "2A": 2500 } },
  { no: 12123, name: "Deccan Queen",       from: "Mumbai",    to: "Pune",      dep: "07:15", arr: "10:25", dur: "3h 10m",  fare: { SL: 180,  "3A": 450,  "2A": 650 } },
  { no: 11029, name: "Koyna Express",      from: "Mumbai",    to: "Pune",      dep: "15:10", arr: "18:40", dur: "3h 30m",  fare: { SL: 160,  "3A": 420,  "2A": 600 } },
  { no: 12009, name: "Shatabdi Express",   from: "Mumbai",    to: "Ahmedabad", dep: "06:25", arr: "12:40", dur: "6h 15m",  fare: { SL: 400,  "3A": 900,  "2A": 1300 } },
  { no: 12163, name: "Chennai Mail",       from: "Mumbai",    to: "Chennai",   dep: "20:35", arr: "04:30", dur: "31h 55m", fare: { SL: 1000, "3A": 2200, "2A": 3100 } },
  { no: 12301, name: "Howrah Rajdhani",    from: "Delhi",     to: "Kolkata",   dep: "16:50", arr: "10:00", dur: "17h 10m", fare: { SL: 1200, "3A": 2250, "2A": 3200 } },
  { no: 12015, name: "Ajmer Shatabdi",     from: "Delhi",     to: "Jaipur",    dep: "06:10", arr: "10:40", dur: "4h 30m",  fare: { SL: 300,  "3A": 700,  "2A": 1000 } },
  { no: 12027, name: "Chennai Shatabdi",   from: "Chennai",   to: "Bengaluru", dep: "06:00", arr: "11:00", dur: "5h 00m",  fare: { SL: 350,  "3A": 800,  "2A": 1150 } },
  { no: 12841, name: "Coromandel Express", from: "Chennai",   to: "Kolkata",   dep: "14:45", arr: "17:00", dur: "26h 15m", fare: { SL: 900,  "3A": 1900, "2A": 2700 } },
  { no: 12785, name: "Hyderabad Express",  from: "Bengaluru", to: "Hyderabad", dep: "21:30", arr: "06:45", dur: "9h 15m",  fare: { SL: 500,  "3A": 1250, "2A": 1800 } }
];

// Every route also runs in the opposite direction
const TRAINS = [];
ROUTES.forEach(r => {
  TRAINS.push(r);
  TRAINS.push({ ...r, no: r.no + 1, name: r.name + " (Return)", from: r.to, to: r.from });
});

// ---------- localStorage helpers ----------
const load = (key, fallback) => JSON.parse(localStorage.getItem(key)) ?? fallback;
const save = (key, value) => localStorage.setItem(key, JSON.stringify(value));

const getUsers = () => load("rr_users", []);
const saveUsers = users => save("rr_users", users);
const getBookings = () => load("rr_bookings", []);
const saveBookings = bookings => save("rr_bookings", bookings);

// ---------- Login session ----------
function getCurrentUser() {
  const userId = localStorage.getItem("userId");
  const name = localStorage.getItem("userName");
  const email = localStorage.getItem("userEmail");

  if (!userId || !email) return null;

  return {
    id: Number(userId),
    name: name || "",
    email: email
  };
}

function setSession(user) {
  localStorage.setItem("userId", user.userId);
  localStorage.setItem("userName", user.name);
  localStorage.setItem("userEmail", user.email);
}

function logout() {
  localStorage.removeItem("userId");
  localStorage.removeItem("userName");
  localStorage.removeItem("userEmail");
  location.href = "index.html";
}

// Call at the top of pages that need a logged-in user
function requireLogin() {
  if (getCurrentUser()) return true;

  const page = location.pathname.split("/").pop() + location.search;
  location.href = "login.html?next=" + encodeURIComponent(page);
  return false;
}

// ---------- Small utilities ----------
const money = n => "₹" + n.toLocaleString("en-IN");
const getTrain = no => TRAINS.find(t => t.no === Number(no));
const makePNR = () => String(Math.floor(1000000000 + Math.random() * 9000000000));

function todayStr() {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 10);
}
function fmtDate(str) {
  return new Date(str + "T00:00:00").toLocaleDateString("en-IN", { weekday: "short", day: "numeric", month: "short", year: "numeric" });
}
// Escapes text typed by users before we put it inside innerHTML
function esc(text) {
  const d = document.createElement("div");
  d.textContent = text;
  return d.innerHTML;
}

// ---------- Seat allocation (the "reservation" logic) ----------
// All passengers of active bookings for one train + date + class, in booking order
function groupOf(bookings, trainNo, date, cls) {
  const group = [];
  bookings
    .filter(b => b.status === "ACTIVE" && b.trainNo === Number(trainNo) && b.date === date && b.cls === cls)
    .forEach(b => b.passengers.forEach(p => group.push(p)));
  return group;
}

function availableSeats(trainNo, date, cls) {
  const confirmed = groupOf(getBookings(), trainNo, date, cls).filter(p => p.status === "CONFIRMED").length;
  return CLASSES[cls].seats - confirmed;
}

// Gives free seats to passengers who are "NEW" or waiting (WL). Everyone else gets a waiting-list number.
// Call it after adding a booking OR cancelling one (cancelling promotes the waiting list).
function allocate(bookings, trainNo, date, cls) {
  const total = CLASSES[cls].seats;
  const group = groupOf(bookings, trainNo, date, cls);
  const used = new Set(group.filter(p => p.status === "CONFIRMED").map(p => p.seatNo));
  let wl = 0;
  group.forEach(p => {
    if (p.status === "CONFIRMED") return;
    let seat = 1;
    while (used.has(seat)) seat++;
    if (seat <= total) { p.status = "CONFIRMED"; p.seatNo = seat; p.wlNo = null; used.add(seat); }
    else { p.status = "WL"; p.seatNo = null; p.wlNo = ++wl; }
  });
}

function seatText(booking, p) {
  if (booking.status === "CANCELLED") return "Cancelled";
  return p.status === "CONFIRMED" ? `${CLASSES[booking.cls].coach}/${p.seatNo}` : `WL/${p.wlNo}`;
}

// ---------- Page setup shared by all pages ----------
function renderNav() {
  const nav = document.getElementById("navbar");
  if (!nav) return;
  const user = getCurrentUser();
  nav.innerHTML = `
    <div class="nav-inner">
      <a class="brand" href="index.html">🚆 RailBook</a>
      <div class="nav-links">
        <a href="index.html">Home</a>
        <a href="search.html">Search Trains</a>
        ${user
          ? `<a href="mybookings.html">My Bookings</a>
             <span class="nav-user">Hi, ${esc(user.name.split(" ")[0])}</span>
             <a href="#" id="logoutLink">Logout</a>`
          : `<a href="login.html">Login</a>
             <a class="btn-small" href="register.html">Register</a>`}
      </div>
    </div>`;
  const out = document.getElementById("logoutLink");
  if (out) out.addEventListener("click", e => { e.preventDefault(); logout(); });
}

function fillStations() {
  document.querySelectorAll(".station-select").forEach(sel => {
    sel.innerHTML = '<option value="">Select station</option>' + STATIONS.map(s => `<option>${s}</option>`).join("");
  });
}

function setDateLimits() {
  const today = todayStr();
  document.querySelectorAll('input[type="date"]').forEach(d => { d.min = today; if (!d.value) d.value = today; });
}

// This file is loaded at the end of <body>, so the elements already exist:
renderNav();
fillStations();
setDateLimits();