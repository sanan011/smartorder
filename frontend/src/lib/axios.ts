import axios, {
    AxiosError,
    AxiosResponse,
    InternalAxiosRequestConfig,
} from 'axios';
// @ts-ignore
import Cookies from 'js-cookie';

// ── Constants ─────────────────────────────────────────────
const GATEWAY_URL      = process.env.NEXT_PUBLIC_GATEWAY_URL ?? 'http://localhost:8080';
const ACCESS_TOKEN_KEY = 'so_access_token';
const REFRESH_TOKEN_KEY= 'so_refresh_token';

// ── Axios Instance ────────────────────────────────────────
export const apiClient = axios.create({
    baseURL: GATEWAY_URL,
    timeout: 15_000,
    headers: {
        'Content-Type': 'application/json',
        'Accept':       'application/json',
    },
    withCredentials: true,
});

// ── Request Interceptor ───────────────────────────────────
// Attaches the JWT access token to every outgoing request
apiClient.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = Cookies.get(ACCESS_TOKEN_KEY);
        if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        // Propagate correlation ID for distributed tracing
        const correlationId = crypto.randomUUID();
        config.headers['X-Correlation-Id'] = correlationId;

        return config;
    },
    (error) => Promise.reject(error)
);

// ── Response Interceptor ──────────────────────────────────
// Handles 401 → silent refresh → retry original request.
// Handles 403 → redirect to home.

let isRefreshing  = false;
let refreshQueue: Array<{
    resolve: (token: string) => void;
    reject:  (error: unknown) => void;
}> = [];

const processQueue = (error: unknown, token: string | null = null) => {
    refreshQueue.forEach(({ resolve, reject }) => {
        if (error) reject(error);
        else resolve(token!);
    });
    refreshQueue = [];
};

apiClient.interceptors.response.use(
    (response: AxiosResponse) => response,

    async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & {
            _retry?: boolean;
        };

        // ── 401 Unauthorized → attempt silent token refresh ──
        if (error.response?.status === 401 && !originalRequest._retry) {
            const refreshToken = Cookies.get(REFRESH_TOKEN_KEY);

            if (!refreshToken) {
                clearTokens();
                redirectToLogin();
                return Promise.reject(error);
            }

            if (isRefreshing) {
                // Queue concurrent requests while refresh is in flight
                return new Promise((resolve, reject) => {
                    refreshQueue.push({ resolve, reject });
                }).then((token) => {
                    originalRequest.headers.Authorization = `Bearer ${token}`;
                    return apiClient(originalRequest);
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                const { data } = await axios.post(
                    `${GATEWAY_URL}/api/v1/auth/refresh`,
                    { refreshToken },
                    { headers: { 'Content-Type': 'application/json' } }
                );

                const newAccessToken = data.accessToken;
                setAccessToken(newAccessToken);
                processQueue(null, newAccessToken);

                originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
                return apiClient(originalRequest);

            } catch (refreshError) {
                processQueue(refreshError, null);
                clearTokens();
                redirectToLogin();
                return Promise.reject(refreshError);

            } finally {
                isRefreshing = false;
            }
        }

        // ── 403 Forbidden → redirect to home ────────────────
        if (error.response?.status === 403) {
            if (typeof window !== 'undefined') {
                window.location.href = '/';
            }
        }

        return Promise.reject(error);
    }
);

// ── Token Helpers (exported for use by auth store) ────────
export const setAccessToken = (token: string) => {
    Cookies.set(ACCESS_TOKEN_KEY, token, {
        secure:   process.env.NODE_ENV === 'production',
        sameSite: 'strict',
        expires:  1 / 96, // 15 minutes
    });
};

export const setRefreshToken = (token: string) => {
    Cookies.set(REFRESH_TOKEN_KEY, token, {
        secure:   process.env.NODE_ENV === 'production',
        sameSite: 'strict',
        expires:  7, // 7 days
    });
};

export const clearTokens = () => {
    Cookies.remove(ACCESS_TOKEN_KEY);
    Cookies.remove(REFRESH_TOKEN_KEY);
};

export const getAccessToken = (): string | undefined =>
    Cookies.get(ACCESS_TOKEN_KEY);

const redirectToLogin = () => {
    if (typeof window !== 'undefined' &&
        !window.location.pathname.startsWith('/auth')) {
        window.location.href = `/auth/login?redirect=${encodeURIComponent(window.location.pathname)}`;
    }
};

export default apiClient;