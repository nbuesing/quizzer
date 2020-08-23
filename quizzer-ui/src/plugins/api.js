import axios from 'axios'

// This is currently assuming that quizzer-ui and request-handler are on the same
// machine, because I am running them in docker both locally and in gcp, this works.

let apiUrl = window.location.protocol + "//" + window.location.hostname + ":9080";
// let apiUrl = 'http://localhost:9080';
// let apiUrl = 'http://request-handler:9080';

axios.interceptors.response.use(
  res => {

    console.log(">> Headers");
    for (var h in req.headers) {
      console.log(": " + h + " : " + req.headers[h]);
    }

    return res;
  },
  err => {
      console.log(">>> " + err.response.status);
      // if (err.response.status === 303) {
      //     throw new Error(`${err.config.url} not found`);
      // }
      throw err;
  }
);

let api = axios.create({
    baseURL: apiUrl,
    headers: {
        'Content-Type': 'application/json'
    }
});



export default api