const registerForm = document.getElementById("registerForm");
const errorBox = document.getElementById("error");

registerForm.addEventListener("submit", e => {
  e.preventDefault();
  const fullName = document.getElementById("fullName").value.trim();
  const email = document.getElementById("email").value.trim().toLowerCase();
  const phone = document.getElementById("phone").value.trim();
  const password = document.getElementById("password").value;
  const confirmPassword = document.getElementById("confirm").value;

  if (fullName.length < 2) return (errorBox.textContent = "Please enter your full name.");
  if (!/^\d{10}$/.test(phone)) return (errorBox.textContent = "Phone number must be exactly 10 digits.");
  if (password.length < 6) return (errorBox.textContent = "Password must be at least 6 characters.");
  if (password !== confirmPassword) return (errorBox.textContent = "Passwords do not match.");

  const users = getUsers();
  if (users.some(u => u.email === email)) {
    errorBox.textContent = "This email is already registered.";
    return;
  }

  users.push({ name: fullName, email, phone, password });
  saveUsers(users);

  setSession(email);              // log the user in straight away
  location.href = "index.html";   // go to the home page
});