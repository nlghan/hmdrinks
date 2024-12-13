import axios from 'axios';

const axiosInstance = axios.create({
    timeout: 30000,
});

const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
};

// Biến để theo dõi trạng thái refresh token
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

const refreshToken = async () => {
    try {
        const refreshToken = getCookie('refresh_token');
        if (!refreshToken) {
            console.error('No refresh token found in cookies');
            throw new Error('No refresh token found');
        }

        console.log('Attempting to refresh token with:', refreshToken);
        
        // Decode JWT để kiểm tra thời gian hết hạn
        const tokenParts = refreshToken.split('.');
        if (tokenParts.length === 3) {
            const payload = JSON.parse(atob(tokenParts[1]));
            console.log('Token payload:', payload);
            console.log('Token expiration:', new Date(payload.exp * 1000));
            console.log('Current time:', new Date());
            if (payload.exp * 1000 < Date.now()) {
                console.error('Refresh token has expired');
                throw new Error('Refresh token expired');
            }
        }

        const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/v1/auth/refresh-token`, null, {
            headers: {
                'Authorization': `Bearer ${refreshToken}`
            }
        }).catch(error => {
            console.error('Refresh token request failed:', {
                status: error.response?.status,
                statusText: error.response?.statusText,
                data: error.response?.data,
                headers: error.response?.headers
            });
            throw error;
        });

        console.log('Refresh token response:', response);

        if (response.data && response.data.access_token) {
            const { access_token, refresh_token } = response.data;
            document.cookie = `access_token=${access_token}; path=/`;
            if (refresh_token) {
                document.cookie = `refresh_token=${refresh_token}; path=/`;
            }
            return access_token;
        }
        console.error('Failed to refresh token, no access token in response');
        throw new Error('Failed to refresh token');
    } catch (error) {
        console.error('Refresh token error:', error);
        if (error.response?.data) {
            console.error('Server error details:', error.response.data);
        }
        // Xóa cookies khi refresh token thất bại
        document.cookie = 'access_token=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT';
        document.cookie = 'refresh_token=; path=/; expires=Thu, 01 Jan 1970 00:00:01 GMT';
        throw error;
    }
};

// Interceptor cho response
axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // Nếu là lỗi mạng, trả về lỗi ngay lập tức
        if (error.code === 'ERR_NETWORK') {
            return Promise.reject(error);
        }

        // Chỉ xử lý khi nhận được lỗi 410 và chưa thử refresh token
        if (error.response?.status === 410 && !originalRequest._retry) {
            if (isRefreshing) {
                try {
                    const token = await new Promise((resolve, reject) => {
                        failedQueue.push({ resolve, reject });
                    });
                    originalRequest.headers.Authorization = `Bearer ${token}`;
                    return axiosInstance(originalRequest);
                } catch (err) {
                    return Promise.reject(err);
                }
            }

            isRefreshing = true;
            originalRequest._retry = true;

            try {
                const newToken = await refreshToken();
                processQueue(null, newToken);
                originalRequest.headers.Authorization = `Bearer ${newToken}`;
                return axiosInstance(originalRequest);
            } catch (refreshError) {
                processQueue(refreshError, null);
                // Chỉ chuyển hướng khi thực sự cần thiết
                // if (refreshError.response?.status === 403 || !getCookie('refresh_token')) {
                //     window.location.href = '/login';
                // }
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        // // Nếu là lỗi 403 từ refresh token, chuyển hướng về login
        // if (error.response?.status === 403 && error.config.url.includes('refresh-token')) {
        //     window.location.href = '/login';
        // }

        return Promise.reject(error);
    }
);

export default axiosInstance; 