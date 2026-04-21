import axios from "axios";

const request = axios.create({
  baseURL: "/api",
  timeout: 5000
});

export function login(data) {
  return request.post("/auth/login", data);
}

