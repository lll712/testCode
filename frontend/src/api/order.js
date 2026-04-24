import request from "./request";

export function fetchOrders() {
  return request.get("/orders");
}

export function createOrder(data) {
  return request.post("/orders", data);
}

export function payOrder(id) {
  return request.put(`/orders/${id}/pay`);
}
