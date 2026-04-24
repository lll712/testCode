import request from "./request";

export function fetchReportSummary(type) {
  return request.get("/reports/summary", {
    params: { type }
  });
}
