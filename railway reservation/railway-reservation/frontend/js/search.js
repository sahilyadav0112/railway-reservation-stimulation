const params = new URLSearchParams(location.search);

const fromStation = params.get("from");
const toStation = params.get("to");
const travelDate = params.get("date");

const fromSelect = document.getElementById("from");
const toSelect = document.getElementById("to");
const results = document.getElementById("results");

// Load stations from the backend
async function loadStations() {
  try {
    const response = await fetch(
      "http://localhost:8080/railway-reservation/stations"
    );

    const data = await response.json();

    if (!data.success) {
      throw new Error(data.message || "Unable to load stations.");
    }

    fromSelect.innerHTML = '<option value="">Select station</option>';
    toSelect.innerHTML = '<option value="">Select station</option>';

    data.stations.forEach((station) => {

      const fromOption = document.createElement("option");
      fromOption.value = station.name;
      fromOption.textContent =
        `${station.name} (${station.code})`;

      const toOption = document.createElement("option");
      toOption.value = station.name;
      toOption.textContent =
        `${station.name} (${station.code})`;

      fromSelect.appendChild(fromOption);
      toSelect.appendChild(toOption);
    });

    // Restore selected values after loading stations
    if (fromStation) {
      fromSelect.value = fromStation;
    }

    if (toStation) {
      toSelect.value = toStation;
    }

  } catch (error) {

    console.error("Station loading error:", error);

    results.innerHTML =
      '<p class="notice error">Unable to load stations from the server.</p>';
  }
}


// Search trains using the backend
async function searchTrains() {

  if (!fromStation || !toStation || !travelDate) {
    return;
  }

  if (fromStation === toStation) {

    results.innerHTML =
      '<p class="notice error">Source and destination cannot be the same.</p>';

    return;
  }

  results.innerHTML =
    '<p class="notice">Searching trains...</p>';

  try {

    const url =
      "http://localhost:8080/railway-reservation/trains" +
      "?from=" + encodeURIComponent(fromStation) +
      "&to=" + encodeURIComponent(toStation);

    const response = await fetch(url);

    const data = await response.json();

    if (!data.success) {

      results.innerHTML =
        `<p class="notice error">${data.message}</p>`;

      return;
    }

    if (data.trains.length === 0) {

      results.innerHTML =
        `<p class="notice">
          No trains found from ${fromStation} to ${toStation}.
        </p>`;

      return;
    }

    results.innerHTML =
      `<h2>
        ${data.trains.length} train(s):
        ${fromStation} → ${toStation}
        on ${fmtDate(travelDate)}
      </h2>` +
      data.trains.map(trainCard).join("");

  } catch (error) {

    console.error("Train search error:", error);

    results.innerHTML =
      '<p class="notice error">Unable to search trains. Please try again.</p>';
  }
}


// Display a train returned by the backend
function trainCard(train) {

  return `
    <div class="card">

      <h3>
        ${train.name}
        <small>#${train.number}</small>
      </h3>

      <p>
        <strong>Type:</strong>
        ${train.type || "Not specified"}
      </p>

      <div class="route">

        <div>
          <strong>${formatTime(train.departure)}</strong>
          <br>
          ${train.from}
        </div>

        <div class="dur">
          →
        </div>

        <div>
          <strong>${formatTime(train.arrival)}</strong>
          <br>
          ${train.to}
        </div>

      </div>

      <div class="classes">

        <a
          class="class-box"
          href="booking.html?trainId=${encodeURIComponent(train.id)}&train=${encodeURIComponent(train.number)}&date=${encodeURIComponent(travelDate)}"
        >
          <strong>Select Train</strong>
          <span class="fare">
            Continue
          </span>
        </a>

      </div>

    </div>
  `;
}


// Convert HH:mm:ss to HH:mm
function formatTime(time) {

  if (!time) {
    return "";
  }

  return time.substring(0, 5);
}


// Load stations first
loadStations();

// Then perform search if parameters exist
searchTrains();