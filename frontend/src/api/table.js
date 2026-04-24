import request from "./request";

export function fetchTables() {
  return request.get("/tables");
}

export function createTable(data) {
  return request.post("/tables", data);
}

export function updateTable(id, data) {
  return request.put(`/tables/${id}`, data);
}

export function deleteTable(id) {
  return request.delete(`/tables/${id}`);
}

export function openTable(id) {
  return request.put(`/tables/${id}/open`);
}

export function closeTable(id, data) {
  return request.put(`/tables/${id}/close`, data);
}
