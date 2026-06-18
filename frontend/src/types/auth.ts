export interface User {
    userId: string;
    email:  string;
    fullName?: string;
    role:   'CUSTOMER' | 'SELLER' | 'ADMIN' | 'SUPPORT';
}

export interface AuthTokens {
    accessToken:          string;
    refreshToken:         string;
    accessTokenExpiresInMs: number;
}

export interface LoginRequest {
    email:    string;
    password: string;
}

export interface RegisterRequest {
    email:     string;
    password:  string;
    firstName: string;
    lastName:  string;
    role?:     'CUSTOMER' | 'SELLER';
}

export interface AuthResponse extends AuthTokens {
    userId:   string;
    email:    string;
    fullName: string;
    role:     string;
    tokenType: string;
}

export interface RegisterResponse {
    userId:  string;
    email:   string;
    message: string;
}