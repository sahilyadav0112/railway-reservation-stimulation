const params = new URLSearchParams(location.search);

const loginForm = document.getElementById("loginForm");
const errorBox = document.getElementById("error");

if (params.get("registered")) {
  document.getElementById("success").hidden = false;
}

loginForm.addEventListener("submit", async (e) => {
  e.preventDefault();

  const email = document.getElementById("email").value.trim().toLowerCase();
  const password = document.getElementById("password").value;

  errorBox.textContent = "";

  try {
    const response = await fetch(
      "http://localhost:8080/railway-reservation/login",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams({
          email: email,
          password: password
        })
      }
    );

    const result = await response.json();

    if (result.success) {

      // Store logged-in user's information
      setSession(result);

      // Keep the existing redirect behavior
      let next = params.get("next") || "index.html";

      // Prevent external redirects
      if (next.includes("://") || next.startsWith("//")) {
        next = "index.html";
      }

      location.href = next;

    } else {

      errorBox.textContent =
        result.message || "Invalid email or password.";
    }

  } catch (error) {

    console.error("Login error:", error);

    errorBox.textContent =
      "Unable to connect to the server. Please try again.";
  }
});