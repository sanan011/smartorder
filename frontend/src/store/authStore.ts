import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import apiClient, {
    setAccessToken,
    setRefreshToken,
    clearTokens,
} from '@/lib/axios';
import type {
    User,
    LoginRequest,
    RegisterRequest,
    AuthResponse,
    RegisterResponse,
} from '@/types/auth';

interface AuthState {
    user:          User | null;
    isAuthenticated: boolean;
    isLoading:     boolean;
    error:         string | null;

    // Actions
    login:         (credentials: LoginRequest) => Promise<void>;
    register:      (data: RegisterRequest) => Promise<RegisterResponse>;
    logout:        (logoutAllDevices?: boolean) => Promise<void>;
    clearError:    () => void;
    setUser:       (user: User | null) => void;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set, get) => ({
            user:            null,
            isAuthenticated: false,
            isLoading:       false,
            error:           null,

            // ── Login ───────────────────────────────────────────────
            login: async (credentials) => {
                set({ isLoading: true, error: null });
                try {
                    const { data } = await apiClient.post<AuthResponse>(
                        '/api/v1/auth/login',
                        credentials
                    );

                    setAccessToken(data.accessToken);
                    setRefreshToken(data.refreshToken);

                    set({
                        user: {
                            userId:   data.userId,
                            email:    data.email,
                            fullName: data.fullName,
                            role:     data.role as User['role'],
                        },
                        isAuthenticated: true,
                        isLoading:       false,
                        error:           null,
                    });

                } catch (err: any) {
                    const message = err.response?.data?.message ?? 'Login failed. Please try again.';
                    set({ error: message, isLoading: false, isAuthenticated: false });
                    throw err;
                }
            },

            // ── Register ────────────────────────────────────────────
            register: async (data) => {
                set({ isLoading: true, error: null });
                try {
                    const response = await apiClient.post<RegisterResponse>(
                        '/api/v1/auth/register',
                        data
                    );
                    set({ isLoading: false });
                    return response.data;

                } catch (err: any) {
                    const message = err.response?.data?.message ?? 'Registration failed.';
                    set({ error: message, isLoading: false });
                    throw err;
                }
            },

            // ── Logout ──────────────────────────────────────────────
            logout: async (logoutAllDevices = false) => {
                set({ isLoading: true });
                try {
                    // Retrieve refresh token from cookie via axios interceptor
                    await apiClient.post('/api/v1/auth/logout', {
                        refreshToken:    document.cookie
                            .split('; ')
                            .find(r => r.startsWith('so_refresh_token='))
                            ?.split('=')[1] ?? '',
                        logoutAllDevices,
                    });
                } catch {
                    // Proceed with local cleanup even if server call fails
                } finally {
                    clearTokens();
                    set({
                        user:            null,
                        isAuthenticated: false,
                        isLoading:       false,
                        error:           null,
                    });
                }
            },

            clearError: () => set({ error: null }),
            setUser:    (user) => set({ user, isAuthenticated: !!user }),
        }),

        {
            name:    'smartorder-auth',
            storage: createJSONStorage(() => sessionStorage),
            // Only persist user identity — never persist tokens in storage
            partialize: (state) => ({
                user:            state.user,
                isAuthenticated: state.isAuthenticated,
            }),
        }
    )
);