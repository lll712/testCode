import request from "./request";

export function fetchReservations() {
  return request.get("/reservations");
}

export function createReservation(data) {
  return request.post("/reservations", data);
}

export function updateReservation(id, data) {
  return request.put(`/reservations/${id}`, data);
}

export function cancelReservation(id) {
  return request.put(`/reservations/${id}/cancel`);
}
