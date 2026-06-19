/** @type {import('next').NextConfig} */
const nextConfig = {
    // Enable React strict mode for catching side-effect bugs early
    reactStrictMode: true,

    // Emit a self-contained server bundle for the Docker runtime stage
    // (the Dockerfile copies .next/standalone).
    output: 'standalone',

    // All API calls go through the Spring Cloud Gateway
    // This avoids CORS issues in development
    async rewrites() {
        return [
            {
                source: '/api/:path*',
                destination: `${process.env.NEXT_PUBLIC_GATEWAY_URL ?? 'http://localhost:8080'}/api/:path*`,
            },
        ];
    },

    // Allow product images served from MinIO
    images: {
        remotePatterns: [
            {
                protocol: 'http',
                hostname: 'localhost',
                port: '9000',
                pathname: '/smartorder-products/**',
            },
            {
                // Production MinIO / CDN hostname
                protocol: 'https',
                hostname: process.env.MINIO_PUBLIC_HOSTNAME ?? 'cdn.smartorder.com',
                pathname: '/**',
            },
        ],
    },

    // Reduce bundle size — only import what we use from lucide
    experimental: {
        optimizePackageImports: ['lucide-react'],
    },
};

export default nextConfig;
