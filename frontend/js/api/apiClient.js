const DEFAULT_HEADERS = {
    "Content-Type": "application/json"
};

async function request(url, options = {}) {

    const response = await fetch(url, {
        credentials: "include",
        headers: {
            ...DEFAULT_HEADERS,
            ...(options.headers || {})
        },
        ...options
    });

    if (!response.ok) {

        let message = "API Error";

        try {
            const data = await response.json();
            message = data.message || message;
        } catch {}

        throw new Error(message);
    }

    return response.json();
}

export const apiClient = {

    post(url, body) {

        return request(url, {

            method: "POST",

            body: JSON.stringify(body)
        });
    }

};