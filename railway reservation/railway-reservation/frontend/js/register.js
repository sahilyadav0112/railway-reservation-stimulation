const registerForm = document.getElementById("registerForm");
const errorBox = document.getElementById("error");

registerForm.addEventListener("submit", async (e) => {
  e.preventDefault();

  const fullName = document.getElementById("fullName").value.trim();
  const email = document.getElementById("email").value.trim().toLowerCase();
  const phone = document.getElementById("phone").value.trim();
  const password = document.getElementById("password").value;
  const confirmPassword = document.getElementById("confirm").value;

  // Frontend validation
  if (fullName.length < 2) {
    errorBox.textContent = "Please enter your full name.";
    return;
  }

  if (!/^\d{10}$/.test(phone)) {
    errorBox.textContent = "Phone number must be exactly 10 digits.";
    return;
  }

  if (password.length < 6) {
    errorBox.textContent = "Password must be at least 6 characters.";
    return;
  }

  if (password !== confirmPassword) {
    errorBox.textContent = "Passwords do not match.";
    return;
  }

  try {
    const response = await fetch(
      "http://localhost:8080/railway-reservation/register",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({
          fullName: fullName,
          email: email,
          phone: phone,
          password: password
        })
      }
    );

    const result = await response.json();

    if (result.success) {
      alert("Registration successful!");
      location.href = "login.html";
    } else {
      errorBox.textContent = result.message;
    }

  } catch (error) {
    console.error(error);
    errorBox.textContent =
      "Unable to connect to the server. Please try again.";
  }
});