// app/layout.tsx
'use client';

import { useState, useEffect, createContext, useContext } from 'react';
import { useRouter } from 'next/navigation';
import { Toaster, toast } from 'react-hot-toast';
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

type ToastType = 'success' | 'error';

// 전역 로그인 상태를 위한 컨텍스트 생성
interface LoginContextType {
  isLoggedIn: boolean;
  setIsLoggedIn: (isLoggedIn: boolean) => void;
  showToast: (message: string, type: ToastType, redirectPath?: string) => void;
}

const LoginContext = createContext<LoginContextType | undefined>(undefined);

// 로그인 컨텍스트를 제공하는 Provider 컴포넌트
function LoginProvider({ children }: { children: React.ReactNode }) {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const router = useRouter();

  useEffect(() => {
    const token = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : null;
    setIsLoggedIn(!!token);
  }, []);

  const showToast = (message: string, type: ToastType, redirectPath?: string) => {
    if (type === 'success') {
      toast.success(message);
    } else {
      toast.error(message);
    }
    if (redirectPath) {
      setTimeout(() => {
        router.push(redirectPath);
      }, 2000);
    }
  };

  return (
    <LoginContext.Provider value={{ isLoggedIn, setIsLoggedIn, showToast }}>
      {children}
    </LoginContext.Provider>
  );
}

// 컨텍스트를 사용하기 위한 훅
export const useLogin = () => {
  const context = useContext(LoginContext);
  if (context === undefined) {
    throw new Error('useLogin must be used within a LoginProvider');
  }
  return context;
};

function Header() {
  const router = useRouter();
  const { isLoggedIn, setIsLoggedIn, showToast } = useLogin();
  
  const handleHomeClick = () => {
    router.push('/');
  };

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setIsLoggedIn(false);
    showToast('로그아웃되었습니다.', 'success');
    router.push('/');
  };
  
  const handleMypage = () => {
    if (isLoggedIn) {
      router.push('/members/mypage');
    } else {
      showToast('마이페이지는 로그인 후 이용 가능합니다.', 'error', '/members/login');
    }
  };

  const handleFriends = () => {
    if (isLoggedIn) {
      router.push('/members/friend');
    } else {
      showToast('친구 목록은 로그인 후 이용 가능합니다.', 'error', '/members/login');
    }
  };

  return (
    <header className="bg-white shadow-md w-full p-4 md:p-6 fixed top-0 z-40">
      <div className="container mx-auto flex items-center justify-between">
        <div onClick={handleHomeClick} className="cursor-pointer">
          <h1 className="text-3xl font-extrabold text-blue-600">
            <span className="text-blue-600">준비물</span>
            <span className="text-gray-900">.com</span>
          </h1>
        </div>
        <nav className="flex items-center space-x-4">
          {isLoggedIn ? (
            <button
              onClick={handleLogout}
              className="px-4 py-2 bg-red-500 text-white font-semibold rounded-full shadow-md transition-all duration-200 hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-400"
            >
              로그아웃
            </button>
          ) : (
            <>
              <button
                onClick={() => router.push('/members/register')}
                className="px-4 py-2 bg-green-500 text-white font-semibold rounded-full shadow-md transition-all duration-200 hover:bg-green-600 focus:outline-none focus:ring-2 focus:ring-green-400"
              >
                회원가입
              </button>
              <button
                onClick={() => router.push('/members/login')}
                className="px-4 py-2 bg-blue-600 text-white font-semibold rounded-full shadow-md transition-all duration-200 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-400"
              >
                로그인
              </button>
            </>
          )}
          <button
            onClick={handleFriends}
            className="px-4 py-2 text-gray-800 bg-gray-200 font-semibold rounded-full shadow-md transition-all duration-200 hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-400"
          >
            내 친구
          </button>
          <button
            onClick={handleMypage}
            className="px-4 py-2 text-gray-800 bg-gray-200 font-semibold rounded-full shadow-md transition-all duration-200 hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-400"
          >
            마이페이지
          </button>
        </nav>
      </div>
    </header>
  );
}

function Footer() {
  return (
    <footer className="bg-gray-800 text-white text-center p-4">
      <p>© 2025 준비물 닷컴.</p>
    </footer>
  );
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body className={`${geistSans.variable} ${geistMono.variable} antialiased flex flex-col min-h-screen`}>
        <LoginProvider>
          <Header />
          <main className="flex-grow pt-24">
            {children}
          </main>
          <Footer />
          <Toaster 
            position="bottom-center"
            toastOptions={{
              error: {
                style: {
                  background: '#EF4444', 
                  color: 'white',
                },
              },
            }}
          />
        </LoginProvider>
      </body>
    </html>
  );
}