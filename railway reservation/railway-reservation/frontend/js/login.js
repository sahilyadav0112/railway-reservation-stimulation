const params = new URLSearchParams(location.search);
const loginForm = document.getElementById("loginForm");
const errorBox = document.getElementById("error");

if (params.get("registered")) document.getElementById("success").hidden = false;

loginForm.addEventListener("submit", e => {
  e.preventDefault();
  const email = document.getElementById("email").value.trim().toLowerCase();
  const password = document.getElementById("password").value;

  const user = getUsers().find(u => u.email === email && u.password === password);
  if (!user) {
    errorBox.textContent = "Invalid email or password.";
    return;
  }

  setSession(user.email);

  // If the user was sent here from a protected page, go back to it
  let next = params.get("next") || "index.html";
  if (next.includes("://") || next.startsWith("//")) next = "index.html";
  location.href = next;
});