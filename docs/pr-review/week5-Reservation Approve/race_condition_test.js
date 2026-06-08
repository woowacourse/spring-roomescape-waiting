import http from 'k6/http';

export const options = {
  vus: 2,
  iterations: 2,
};

export default function () {
  const res = http.del('http://localhost:8080/reservations/20');
  console.log(`VU ${__VU}: status=${res.status}`);
}