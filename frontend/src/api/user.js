import request from "./request";

export function fetchUsers() {
  return request.get("/users");
}

export function updateUser(id, data) {
  return request.put(`/users/${id}`, data);
}

export function disableUser(id) {
  return request.put(`/users/${id}/disable`);
}
