import axios from "axios";
const service = axios.create({
    baseURL: 'http://localhost:8099/',
    timeout: 5000,
    withCredentials: false, // 允许携带cookie
    headers: {
        "Content-Type": "application/json;charset=utf-8",
    }
});

export default service
