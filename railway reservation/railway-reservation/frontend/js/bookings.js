if (requireLogin()) {
  loadBookings();
}


async function loadBookings() {

  const list =
    document.getElementById("bookingList");

  const user =
    getCurrentUser();


  if (!user) {
    return;
  }


  list.innerHTML =
    '<p class="notice">Loading your bookings...</p>';


  try {

    const response =
      await fetch(
        "http://localhost:8080/railway-reservation/my-bookings?userId=" +
        encodeURIComponent(user.id)
      );


    const data =
      await response.json();


    if (!response.ok ||
        !data.success) {

      list.innerHTML =
        `<p class="notice error">
          ${data.message ||
            "Unable to load bookings."}
        </p>`;

      return;
    }


    if (data.bookings.length === 0) {

      list.innerHTML =
        '<p class="notice">' +
        'You have no bookings yet. ' +
        '<a href="search.html">Book a train</a>' +
        '</p>';

      return;
    }


    list.innerHTML =
      data.bookings
        .map(renderBooking)
        .join("");


  } catch (error) {

    console.error(
      "My bookings error:",
      error
    );


    list.innerHTML =
      '<p class="notice error">' +
      'Unable to connect to the booking server.' +
      '</p>';
  }
}


function renderBooking(booking) {

  const statusClass =
    booking.status === "CONFIRMED"
      ? "ok"
      : "danger";


  const cancelButton =
    booking.status === "CONFIRMED"
      ? `
        <button
          class="btn btn-danger cancel-btn"
          data-pnr="${esc(booking.pnr)}">
          Cancel Booking
        </button>
        `
      : "";


  return `

    <div class="card">

      <div class="booking-head">

        <div>

          <strong>
            PNR ${esc(booking.pnr)}
          </strong>

          <br>

          <small>
            Booked on
            ${formatBookingDate(
              booking.bookingDate
            )}
          </small>

        </div>


        <span class="badge ${statusClass}">
          ${esc(booking.status)}
        </span>

      </div>


      <h3>

        ${esc(booking.trainName)}

        <small>
          #${esc(booking.trainNumber)}
        </small>

      </h3>


      <p>

        ${esc(booking.from)}

        →

        ${esc(booking.to)}

        &nbsp;|&nbsp;

        ${fmtDate(booking.journeyDate)}

        &nbsp;|&nbsp;

        ${esc(booking.className)}

        (${esc(booking.classCode)})

      </p>


      <p>

        Total:

        <strong>
          ${money(booking.totalFare)}
        </strong>

      </p>


      <div class="actions">

        <a
          class="btn btn-outline"
          href="ticket.html?pnr=${encodeURIComponent(booking.pnr)}">
          View Ticket
        </a>

        ${cancelButton}

      </div>

    </div>

  `;
}


function formatBookingDate(value) {

  if (!value) {
    return "";
  }


  const date =
    new Date(
      value.replace(" ", "T")
    );


  if (Number.isNaN(
    date.getTime()
  )) {

    return value;
  }


  return date.toLocaleDateString(
    "en-IN"
  );
}


async function cancelBooking(pnr) {

  const user =
    getCurrentUser();


  if (!user) {
    return;
  }


  const confirmed =
    confirm(
      "Are you sure you want to cancel booking " +
      pnr +
      "?"
    );


  if (!confirmed) {
    return;
  }


  try {

    const formData =
      new URLSearchParams();


    formData.append(
      "pnr",
      pnr
    );


    formData.append(
      "userId",
      user.id
    );


    const response =
      await fetch(
        "http://localhost:8080/railway-reservation/cancel-booking",
        {
          method: "POST",

          headers: {
            "Content-Type":
              "application/x-www-form-urlencoded"
          },

          body: formData
        }
      );


    const data =
      await response.json();


    if (!response.ok ||
        !data.success) {

      alert(
        data.message ||
        "Unable to cancel booking."
      );

      return;
    }


    alert(
      "Booking cancelled successfully."
    );


    /*
     * Reload bookings from MySQL
     */
    loadBookings();


  } catch (error) {

    console.error(
      "Cancellation error:",
      error
    );


    alert(
      "Unable to connect to the booking server."
    );
  }
}


document.addEventListener(
  "click",
  function (event) {

    const button =
      event.target.closest(
        ".cancel-btn"
      );


    if (!button) {
      return;
    }


    const pnr =
      button.dataset.pnr;


    cancelBooking(pnr);
  }
);


function esc(value) {

  if (
    value === null ||
    value === undefined
  ) {
    return "";
  }


  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}