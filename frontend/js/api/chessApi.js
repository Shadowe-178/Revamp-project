import { apiClient } from "./apiClient.js";

export const ChessApi = {

    init() {

        return apiClient.post("/api/chess/init", {});
    },

    move(data) {

        return apiClient.post("/api/chess/move", data);
    },

    legal(data) {

        return apiClient.post("/api/chess/legal", data);
    },

    ai(data) {

        return apiClient.post("/api/chess/ai", data);
    }

};