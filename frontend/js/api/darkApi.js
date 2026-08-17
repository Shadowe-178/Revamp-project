import { apiClient } from "./apiClient.js";

export const DarkApi = {

    init() {

        return apiClient.post("/api/dark/init", {});
    },

    flip(data) {

        return apiClient.post("/api/dark/flip", data);
    },

    move(data) {

        return apiClient.post("/api/dark/move", data);
    },

    legal(data) {

        return apiClient.post("/api/dark/legal", data);
    },

    ai(data) {

        return apiClient.post("/api/dark/ai", data);
    }

};