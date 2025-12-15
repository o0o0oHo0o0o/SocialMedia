import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./Test.css";
import App from "./App.jsx";
createRoot(document.getElementById("root")).render(
  <StrictMode>
    <App></App>
  </StrictMode>,
);
