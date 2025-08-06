import type { NextConfig } from "next";

// next.config.js
/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'kacg-2025-images.s3.ap-northeast-2.amazonaws.com',
        port: '',
        pathname: '/member/**',
      },
      {
        protocol: 'https',
        hostname: 'kacg-2025-images.s3.ap-northeast-2.amazonaws.com',
        port: '',
        pathname: '/club/**',
      },
    ],
  },
};

module.exports = nextConfig;

export default nextConfig;
