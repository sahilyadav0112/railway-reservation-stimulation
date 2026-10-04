const params = new URLSearchParams(location.search);

const trainId = params.get("trainId");
const trainNumber = params.get("train");
const travelDate = params.get("date");

const MAX_PASSENGERS = 6;

let trainClasses = [];
let selectedClass = null;


// Require user login before booking
if (requireLogin()) {
  init();
}


// Initialize booking page
async function init() {

  if (!trainId || !trainNumber || !travelDate) {

    document.querySelector("main").innerHTML =
      '<p class="notice error">' +
      'Invalid booking link. ' +
      '<a href="search.html">Search trains again</a>' +
      '</p>';

    return;
  }

  await loadTrainClasses();

  if (!selectedClass) {
    return;
  }

  renderSummary();

  addPassenger();

  document
    .getElementById("addPassenger")
    .addEventListener("click", addPassenger);

  document
    .getElementById("bookingForm")
    .addEventListener("submit", submitBooking);
}


// Load classes, coaches, fares and seat availability
async function loadTrainClasses() {

  try {

    const response = await fetch(
      "http://localhost:8080/railway-reservation/train-classes?trainId=" +
      encodeURIComponent(trainId)
    );

    const data = await response.json();

    if (!data.success) {

      document.querySelector("main").innerHTML =
        `<p class="notice error">
          ${data.message || "Unable to load train classes."}
        </p>`;

      return;
    }

    trainClasses = data.classes;

    if (trainClasses.length === 0) {

      document.querySelector("main").innerHTML =
        '<p class="notice error">' +
        'No classes are available for this train.' +
        '</p>';

      return;
    }

    // Select the first class having available seats
    selectedClass =
      trainClasses.find(
        c => c.availableSeats > 0
      ) || trainClasses[0];

  } catch (error) {

    console.error(
      "Train class loading error:",
      error
    );

    document.querySelector("main").innerHTML =
      '<p class="notice error">' +
      'Unable to connect to the server. Please try again.' +
      '</p>';
  }
}


// Display train and class information
function renderSummary() {

  const available =
    selectedClass.availableSeats;

  document.getElementById("trainSummary").innerHTML = `

    <h2>
      Train #${trainNumber}
    </h2>

    <p>
      Journey date:
      <strong>${fmtDate(travelDate)}</strong>
    </p>

    <p>
      Class:
      <strong>
        ${selectedClass.className}
        (${selectedClass.classCode})
      </strong>
    </p>

    <p>
      Coach:
      <strong>
        ${selectedClass.coachNumber}
      </strong>
    </p>

    <p>
      Fare per passenger:
      <strong>
        ${money(selectedClass.fare)}
      </strong>
    </p>

    <p>
      ${
        available > 0
          ? `<span class="badge ok">
               ${available} seats available
             </span>`
          : `<span class="badge warn">
               Sold out
             </span>`
      }
    </p>
  `;
}


// Add a passenger
function addPassenger() {

  const box =
    document.getElementById("passengers");

  const count =
    box.children.length;

  if (count >= MAX_PASSENGERS) {

    alert(
      "Maximum " +
      MAX_PASSENGERS +
      " passengers per booking."
    );

    return;
  }

  const row =
    document.createElement("div");

  row.className =
    "passenger-row";

  row.innerHTML = `

    <h4>
      Passenger ${count + 1}
    </h4>

    <div class="grid">

      <div class="field">

        <label>Name</label>

        <input
          type="text"
          class="p-name"
          required
        >

      </div>


      <div class="field">

        <label>Age</label>

        <input
          type="number"
          class="p-age"
          min="1"
          max="120"
          required
        >

      </div>


      <div class="field">

        <label>Gender</label>

        <select class="p-gender">

          <option>Male</option>
          <option>Female</option>
          <option>Other</option>

        </select>

      </div>

    </div>

    ${
      count > 0
        ? '<button type="button" class="link-btn remove">Remove passenger</button>'
        : ""
    }

  `;

  box.appendChild(row);


  const removeBtn =
    row.querySelector(".remove");


  if (removeBtn) {

    removeBtn.addEventListener(
      "click",
      () => {

        row.remove();

        document
          .querySelectorAll(
            ".passenger-row h4"
          )
          .forEach(
            (h, i) => {

              h.textContent =
                "Passenger " + (i + 1);

            }
          );

        updateTotal();
      }
    );
  }

  updateTotal();
}


// Update total fare
function updateTotal() {

  const numberOfPassengers =
    document
      .getElementById("passengers")
      .children.length;

  const total =
    numberOfPassengers *
    Number(selectedClass.fare);

  document
    .getElementById("totalFare")
    .textContent =
    money(total);
}


// Submit booking to backend
async function submitBooking(e) {

  e.preventDefault();


  const errorBox =
    document.getElementById("error");

  errorBox.textContent = "";


  // Collect passenger information
  const passengers =
    [
      ...document.querySelectorAll(
        ".passenger-row"
      )
    ].map(row => ({

      name:
        row
          .querySelector(".p-name")
          .value
          .trim(),

      age:
        Number(
          row
            .querySelector(".p-age")
            .value
        ),

      gender:
        row
          .querySelector(".p-gender")
          .value

    }));


  // Check passenger count
  if (passengers.length === 0) {

    errorBox.textContent =
      "Please add at least one passenger.";

    return;
  }


  // Check passenger names
  if (
    passengers.some(
      p => p.name === ""
    )
  ) {

    errorBox.textContent =
      "Please enter a name for every passenger.";

    return;
  }


  // Check passenger ages
  if (
    passengers.some(
      p =>
        !Number.isInteger(p.age) ||
        p.age < 1 ||
        p.age > 120
    )
  ) {

    errorBox.textContent =
      "Please enter a valid age for every passenger.";

    return;
  }


  // Check login
  const user =
    getCurrentUser();

  if (!user) {

    errorBox.textContent =
      "Please log in before booking.";

    return;
  }


  // Check seat availability
  if (
    selectedClass.availableSeats <
    passengers.length
  ) {

    errorBox.textContent =
      "Not enough seats are available for this booking.";

    return;
  }


  // Prepare form data
  const formData =
    new URLSearchParams();


  formData.append(
    "userId",
    user.id
  );

  formData.append(
    "trainId",
    trainId
  );

  formData.append(
    "classId",
    selectedClass.classId
  );

  formData.append(
    "coachId",
    selectedClass.coachId
  );

  formData.append(
    "journeyDate",
    travelDate
  );


  // Add all passengers
  passengers.forEach(
    passenger => {

      formData.append(
        "passengerName",
        passenger.name
      );

      formData.append(
        "passengerAge",
        passenger.age
      );

      formData.append(
        "passengerGender",
        passenger.gender
      );

    }
  );


  try {

    errorBox.textContent =
      "Confirming your booking...";


    // Send booking to Java Servlet
    const response =
      await fetch(
        "http://localhost:8080/railway-reservation/book",
        {
          method: "POST",

          headers: {
            "Content-Type":
              "application/x-www-form-urlencoded"
          },

          body: formData
        }
      );


    const result =
      await response.json();


    console.log(
      "Booking response:",
      result
    );


    // Backend booking failed
    if (!response.ok ||
        !result.success) {

      errorBox.textContent =
        result.message ||
        "Booking failed.";

      return;
    }


    // Save successful booking temporarily
    sessionStorage.setItem(
      "lastBooking",
      JSON.stringify(result)
    );


    /*
     * Booking is successfully created.
     * Now go to the simulated payment page.
     */
    location.href =
      "payment.html?bookingId=" +
      encodeURIComponent(
        result.bookingId
      ) +
      "&amount=" +
      encodeURIComponent(
        result.totalFare
      );


  } catch (error) {

    console.error(
      "Booking error:",
      error
    );

    errorBox.textContent =
      "Unable to connect to the booking server. Please try again.";
  }
}