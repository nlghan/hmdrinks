import React, { useState, useEffect } from 'react';
import './Post.css';
import axios from 'axios';
import Header from '../../components/Header/Header';
import FormAddPost from '../../components/Form/FormAddPost';
import FormDetailsPost from '../../components/Form/FormDetailsPost';
import FormUpdatePost from '../../components/Form/FormUpdatePost';
import pLimit from 'p-limit';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import debounce from 'lodash/debounce';
import axiosInstance from '../../utils/axiosConfig';
import Swal from 'sweetalert2';

const News = () => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [posts, setPosts] = useState([]);
    const [vouchers, setVouchers] = useState([]);
    const [isFormAddPostVisible, setFormAddPostVisible] = useState(false);
    const [isFormUpdatePostVisible, setFormUpdatePostVisible] = useState(false);
    const [postToUpdate, setPostToUpdate] = useState(null);
    const [isUpdatePostOpen, setIsUpdatePostOpen] = useState(false);
    const [selectedPost, setSelectedPost] = useState(null);
    const [userVouchers, setUserVouchers] = useState([]);
    const [selectedPostId, setSelectedPostId] = useState(null);
    const [activeMenu, setActiveMenu] = useState('posts');
    const [error, setError] = useState('');
    const [users, setUsers] = useState([]);
    const [vouchersPerPage] = useState(2);
    const [currentPageVouchers, setCurrentPageVouchers] = useState(1);
    const [searchTerm, setSearchTerm] = useState('');
    const [limit, setLimit] = useState(3);
    const [currentPage, setCurrentPage] = useState(1); // Current page for posts
    const [totalPages, setTotalPages] = useState(1); // Total pages for posts
    const [postData, setPostData] = useState({});
    const [voucherSearchTerm, setVoucherSearchTerm] = useState('');
    const [allVouchers, setAllVouchers] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [voucher, setVoucher] = useState(null);
    const [post, setPost] = useState(null);
    const [isUserVouchersLoading, setIsUserVouchersLoading] = useState(false);
    const [total, setTotal] = useState(); // Tổng số trang
    const [totalV, setTotalV] = useState(); // Tổng số trang

    const getUserIdFromToken = (token) => {
        try {
            const payload = token.split('.')[1];
            const decodedPayload = JSON.parse(atob(payload));
            return decodedPayload.UserId;
        } catch (error) {
            console.error("Unable to decode token:", error);
            return null;
        }
    };

    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
    };

    const token = getCookie('access_token');
    const userId = getUserIdFromToken(token);

    const postVoucherCache = new Map();

    const fetchPostVoucher = async (page, limit, type = 'all') => {
        const cacheKey = `${page}-${limit}-${type}`;
        if (postVoucherCache.has(cacheKey)) {
            console.log("Sử dụng dữ liệu từ cache:", cacheKey);
            const cachedData = postVoucherCache.get(cacheKey);
            setPosts(cachedData.postsWithVouchers);
            setTotalPages(cachedData.totalPages);
            return;
        }

        try {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            const userId = getUserIdFromToken(token);
            if (!userId) {
                setError("Không thể lấy userId từ token.");
                return;
            }

            let url = '';
            if (type === 'all') {
                url = `http://localhost:1010/api/admin/post/view/all?page=${page}&limit=${limit}`;
            } else {
                url = `http://localhost:1010/api/admin/post/view/type/all?page=${page}&limit=${limit}&type=${type}`;
            }

            const responsePosts = await axiosInstance.get(url, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            const dataPosts = responsePosts.data;
            const fetchedPosts = dataPosts.listPosts || [];

            const responseVouchers = await axiosInstance.get('http://localhost:1010/api/voucher/view/all', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            const fetchedVouchers = responseVouchers.data.body.voucherResponseList || [];

            const postsWithVouchers = fetchedPosts.map(post => {
                const matchingVoucher = fetchedVouchers.find(voucher => voucher.postId === post.postId);
                return {
                    ...post,
                    voucher: matchingVoucher || null
                };
            });

            // Cập nhật cache
            postVoucherCache.set(cacheKey, {
                postsWithVouchers,
                totalPages: dataPosts.totalPages,
            });

            setPosts(postsWithVouchers);
            setTotalPages(dataPosts.totalPages);
            setTotal(dataPosts.total)

        } catch (error) {
            console.error('Error fetching posts or vouchers:', error);
        }
    };




    const [selectedType, setSelectedType] = useState('all');

    const handleTypeChange = (event) => {
        const newType = event.target.value;
        setSelectedType(newType);
        fetchPostVoucher(currentPage, limit, newType);
    };




    useEffect(() => {
        fetchPostVoucher(currentPage, limit);
    }, [currentPage]);

    const fetchUserVouchersBatch = async (userIds) => {
        const token = getCookie('access_token');
        if (!token) {
            setError("Bạn cần đăng nhập để xem thông tin này.");
            return;
        }

        const batchSize = 5; // Giới hạn mỗi lần gửi tối đa 5 yêu cầu
        for (let i = 0; i < userIds.length; i += batchSize) {
            const batch = userIds.slice(i, i + batchSize);
            await Promise.all(
                batch.map(async (userId) => {
                    try {
                        const response = await axiosInstance.get(`http://localhost:1010/api/admin/list-voucher/${userId}`, {
                            headers: {
                                'Authorization': `Bearer ${token}`,
                            },
                        });

                        if (response.data?.getVoucherResponseList?.length > 0) {
                            setUserVouchers((prevVouchers) => [
                                ...prevVouchers,
                                ...response.data.getVoucherResponseList,
                            ]);
                        }
                    } catch (error) {
                        console.error(`Error fetching vouchers for userId ${userId}:`, error);
                        if (error.response?.status === 429) {
                            const retryAfter = error.response.headers['retry-after'] || 1000; // Mặc định 1 giây nếu không có header
                            console.warn(`Rate limit hit for userId ${userId}. Retrying after ${retryAfter}ms...`);
                            await new Promise((resolve) => setTimeout(resolve, retryAfter * 1000)); // Chờ theo thời gian được chỉ định
                        }
                    }
                })
            );
            await new Promise((resolve) => setTimeout(resolve, 2000)); // Chờ 2 giây trước khi xử lý batch tiếp theo
        }
    };

    const userCache = new Map();

    const fetchAllUsers = async () => {
        if (userCache.has('allUsers')) {
            console.log("Sử dụng dữ liệu từ cache: allUsers");
            const cachedData = userCache.get('allUsers');
            setUsers(cachedData.users);
            await fetchUserVouchersBatch(cachedData.userIds);
            return;
        }

        try {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            const allUsers = [];
            let page = 1;
            const limit = 15;
            let hasMoreUsers = true;

            while (hasMoreUsers) {
                const response = await axiosInstance.get(`http://localhost:1010/api/admin/listUser?page=${page}&limit=${limit}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });

                const userData = response.data.detailUserResponseList;

                if (userData && userData.length > 0) {
                    allUsers.push(...userData);
                    if (userData.length < limit) {
                        hasMoreUsers = false;
                    }
                } else {
                    hasMoreUsers = false;
                }

                page++;
            }

            const userIds = allUsers.map((user) => user.userId);

            // Lưu dữ liệu vào cache
            userCache.set('allUsers', { users: allUsers, userIds });

            setUsers(allUsers);
            if (userIds.length > 0) {
                await fetchUserVouchersBatch(userIds);
            }
        } catch (error) {
            console.error("Error fetching all users:", error);
        }
    };

    const fetchWithRetry = async (url, options, retries = 3, delay = 1000) => {
        try {
            return await axiosInstance.get(url, options);
        } catch (error) {
            if (retries > 0 && error.response?.status === 429) {
                console.warn(`Retrying after ${delay}ms...`);
                await new Promise(resolve => setTimeout(resolve, delay));
                return fetchWithRetry(url, options, retries - 1, delay * 2); // Tăng delay cho lần sau
            }
            throw error;

        }
    };



    useEffect(() => {
        fetchPostVoucher(currentPage, limit);
    }, [currentPage]);

    const handlePageChange = (newPage) => {
        if (newPage > 0 && newPage <= totalPages) {  // Corrected to use totalPages
            setCurrentPage(newPage);
        }
    };

    const getPaginationNumbers = () => {
        const paginationNumbers = [];
        const maxButtons = 5; // Max page buttons to display

        // Show ellipsis khi có nhiều hơn maxButtons trang
        if (totalPages <= maxButtons) {
            for (let i = 1; i <= totalPages; i++) {
                paginationNumbers.push(i);
            }
        } else {
            // Luôn hiển thị trang đầu tiên
            paginationNumbers.push(1);

            if (currentPage > 3) {
                paginationNumbers.push('...'); // Ellipsis nếu trang hiện tại lớn hơn 3
            }

            const startPage = Math.max(2, currentPage - 1); // Bắt đầu từ trang thứ 2 hoặc trang hiện tại -1
            const endPage = Math.min(totalPages - 1, currentPage + 1); // Kết thúc ở trang trước cuối hoặc trang hiện tại +1

            for (let i = startPage; i <= endPage; i++) {
                paginationNumbers.push(i);
            }

            if (currentPage < totalPages - 2) {
                paginationNumbers.push('...'); // Ellipsis nếu trang hiện tại nhỏ hơn tổng trang -2
            }

            // Luôn hiển thị trang cuối
            paginationNumbers.push(totalPages);
        }

        return paginationNumbers;
    };

    const handleSearchChange = debounce((value) => {
        fetchPosts(value.trim()); // Call fetchPosts with the search term
    }, 500);

    const handleInputChange = debounce((event, newInputValue) => {
        setSearchTerm(newInputValue);
        handleSearchChange(newInputValue); // Trigger search on input change
    }, 500);



    // Filtering posts based on search term
    const filteredPosts = posts.filter((post) =>
        (post.title && post.title.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (post.shortDescription && post.shortDescription.toLowerCase().includes(searchTerm.toLowerCase()))
    );

    const handleOnSubmit = async () => {
        try {
            // Fetch lại danh sách posts
            await fetchPostVoucher(currentPage, limit);

            // Fetch lại danh sách vouchers only when adding a post
            setIsUserVouchersLoading(true); // Set loading for user vouchers
            const token = getCookie('access_token');
            const response = await axiosInstance.get('http://localhost:1010/api/voucher/view/all', {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.data && response.data.body) {
                const fetchedVouchers = response.data.body.voucherResponseList || [];
                setAllVouchers(fetchedVouchers);
                setTotalV(response.data.body.total)
            }
        } catch (error) {
            console.error("Error refreshing data after post addition:", error);
            setError("Không thể cập nhật dữ liệu sau khi thêm bài đăng");
        } finally {
            setIsUserVouchersLoading(false); // Reset loading state
        }
    };

    const handleUpdatePost = async (updatedPost) => {
        console.log("Updating post with data:", updatedPost);

        // Ensure updatedPost is defined and has the required properties
        if (!updatedPost || !updatedPost.postId) {
            console.error("Invalid post data:", updatedPost);
            return;
        }

        // Cập nhật bài viết trong state
        setPosts((prevPosts) =>
            prevPosts.map((post) =>
                post.postId === updatedPost.postId ? {
                    ...post,
                    ...updatedPost
                } : post
            )
        );

        // Cập nhật thông tin bài viết
        setPostData(updatedPost);

        // Fetch lại danh sách bài viết sau khi cập nhật
        await fetchPostVoucher(currentPage, limit);

        // Fetch lại danh sách vouchers only when updating a post
        setIsUserVouchersLoading(true); // Set loading for user vouchers
        const token = getCookie('access_token');
        const response = await axiosInstance.get('http://localhost:1010/api/voucher/view/all', {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.data && response.data.body) {
            const fetchedVouchers = response.data.body.voucherResponseList || [];
            setAllVouchers(fetchedVouchers);
        }
        setIsUserVouchersLoading(false); // Reset loading state
    };


    const handleViewPostDetails = (postId) => {
        setSelectedPostId(postId);
        setIsLoading(true);
    };

    const handleUpdatePostClick = (post) => {
        if (post) {
            setPostToUpdate(post); // Make sure post is not null or undefined
            setIsUpdatePostOpen(true); // Open update form
        } else {
            console.error("No post found for update.");
        }
    };

    const handleCloseUpdatePost = () => {
        setIsUpdatePostOpen(false);
        setPostToUpdate(null); // Reset the post to update
    };



    const handleSwitchChange = async (postId) => {
        try {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để thực hiện thao tác này.");
                return;
            }

            // Find the post in the current posts state
            const postToUpdate = posts.find(post => post.postId === postId);
            if (!postToUpdate) {
                console.error("No post found with the given postId:", postId);
                return;
            }

            // Determine the correct API endpoint based on isDeleted status
            const apiUrl = postToUpdate.isDeleted
                ? 'http://localhost:1010/api/post/enable'
                : 'http://localhost:1010/api/post/disable';
        
                const newIsDeletedStatus = !postToUpdate.isDeleted;
        
                // Hiển thị xác nhận với SweetAlert2
                const confirmMessage = newIsDeletedStatus
                    ? 'Bạn có chắc chắn muốn vô hiệu bài đăng này cùng voucher tương ứng?'
                    : 'Bạn có chắc chắn muốn kích hoạt bài đăng này?';
        
                const result = await Swal.fire({
                    title: 'Xác nhận',
                    text: confirmMessage,
                    icon: 'warning',
                    showCancelButton: true,
                    confirmButtonColor: '#3085d6',
                    cancelButtonColor: '#d33',
                    confirmButtonText: 'Xác nhận',
                    cancelButtonText: 'Hủy'
                });
        
                if (!result.isConfirmed) {
                    return; // Người dùng hủy bỏ, không thực hiện tiếp
                }

            // Send the request to enable or disable the post
            const response = await axiosInstance.put(
                apiUrl,
                { id: postId },
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                }
            );

            // If the API call is successful, update the local state
            if (response.status === 200) {
                setPosts((prevPosts) =>
                    prevPosts.map((post) =>
                        post.postId === postId
                            ? {
                                ...post,
                                isDeleted: newIsDeletedStatus,
                                dateDeleted: newIsDeletedStatus ? new Date().toISOString() : null,
                            }
                            : post
                    )
                );
                console.log(
                    `Post with ID ${postId} is now ${newIsDeletedStatus ? 'disabled' : 'enabled'}.`
                );
            } else {
                setError("Không thể thay đổi trạng thái bài post. Vui lòng thử lại.");
            }
        } catch (error) {
            console.error("Error changing post status:", error);
            setError("Không thể thay đổi trạng thái bài post. Vui lòng thử lại.");
        }
    };

    const handleVoucherSearch = (event) => {
        setVoucherSearchTerm(event.target.value);
    };

    // Thêm hàm formatDate ở đầu component
    const formatDate = (dateString) => {
        try {
            if (!dateString) {
                console.log('Date string is empty or null:', dateString);
                return '';
            }
            const date = new Date(dateString);
            if (isNaN(date.getTime())) {
                console.log('Invalid date string:', dateString);
                return '';
            }
            return date.toLocaleDateString('vi-VN');
        } catch (error) {
            console.error('Error formatting date:', error);
            return '';
        }
    };

    // Thêm logs để kiểm tra dữ liệu
    useEffect(() => {
        const fetchPostDetails = async () => {
            if (!selectedPostId) return; // Thêm điều kiện kiểm tra

            setIsLoading(true); // Set loading to true when fetching starts
            const token = getCookie('access_token');
            try {
                const response = await axiosInstance.get(`http://localhost:1010/api/post/view/${selectedPostId}`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                setPost(response.data.body);

                const responseVouchers = await axiosInstance.get('http://localhost:1010/api/voucher/view/all', {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                const fetchedVouchers = responseVouchers.data.body.voucherResponseList || [];
                console.log("Fetched Posts:", response.data.body);
                console.log("Fetched Vouchers:", fetchedVouchers);

                // Log để kiểm tra cấu trúc dữ liệu
                if (fetchedVouchers.length > 0) {
                    console.log("Sample Voucher Structure:", {
                        key: fetchedVouchers[0].key,
                        discount: fetchedVouchers[0].discount,
                        startDate: fetchedVouchers[0].startDate,
                        endDate: fetchedVouchers[0].endDate,
                        status: fetchedVouchers[0].status
                    });
                }

                const matchingVoucher = fetchedVouchers.find(
                    (voucher) => String(voucher.postId) === String(selectedPostId)
                );
                setVoucher(matchingVoucher || null);
            } catch (err) {
                console.error("Error fetching data:", err);
                setError(err.message);
            } finally {
                setIsLoading(false); // Set loading to false when fetching is done
            }
        };

        fetchPostDetails();
    }, [selectedPostId]); // Thay đổi dependency từ postId sang selectedPostId

    // Thêm useEffect để fetch dữ liệu ban đầu
    useEffect(() => {
        fetchPostVoucher(currentPage, limit);
    }, [currentPage, limit]);

    // Thêm log cho filteredVouchers
    const filteredVouchers = filteredPosts.filter(post => {
        console.log("Checking post:", post);
        console.log("Post voucher:", post.voucher);
        return post.voucher &&
            post.voucher.key &&
            (post.voucher.key.toLowerCase().includes(voucherSearchTerm.toLowerCase()) ||
                post.voucher.status.toLowerCase().includes(voucherSearchTerm.toLowerCase()) ||
                post.voucher.discount.toString().includes(voucherSearchTerm));
    });

    // Thêm useEffect để fetch tất cả vouchers khi component mount
    useEffect(() => {
        const fetchAllVouchers = async () => {
            setIsLoading(true);
            try {
                const token = getCookie('access_token');
                const response = await axiosInstance.get('http://localhost:1010/api/voucher/view/all', {
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                if (response.data && response.data.body) {
                    const fetchedVouchers = response.data.body.voucherResponseList || [];
                    setAllVouchers(fetchedVouchers);
                    setTotalV(response.data.body.total)
                }
            } catch (error) {
                console.error("Error fetching vouchers:", error);
                setError("Không thể tải danh sách voucher");
            } finally {
                setIsLoading(false);
            }
        };

        fetchAllVouchers();
    }, []); // Empty dependency array means this runs once when component mounts
    
    const getPostTypeText = (type) => {
        const typeMap = {
            'EVENT': 'Sự kiện',
            'DISCOUNT': 'Giảm giá',
            'NEW': 'Món mới'
        };
        
        return typeMap[type] || type;
    };

    return (

        <div className="post-container">
            <Header isMenuOpen={isMenuOpen} toggleMenu={() => setIsMenuOpen(!isMenuOpen)} title="Tiếp thị" />
            <div className="post-content">

                <div className="post-voucher-table" >

                    <>
                        <div className="post-table">
                            <div className="header-post-table">
                                <h2>Danh sách bài đăng ({total})</h2>
                                <Autocomplete
                                    freeSolo
                                    options={Array.isArray(posts) ? posts.map((post) => post.title) : []} // Assuming title is the field to search
                                    inputValue={searchTerm}
                                    onInputChange={handleInputChange}
                                    renderInput={(params) => (
                                        <TextField
                                            {...params}
                                            label="Tìm kiếm bài đăng..."
                                            variant="outlined"
                                            className="search-post-admin-input"
                                        />
                                    )}
                                    style={{ width: '1500px', borderRadius: '20px', marginLeft: '750px', marginRight: '-750px' }} // Adjust size as needed
                                />
                                <select value={selectedType} onChange={handleTypeChange} className="type-select" style={{ width: '11.5%', borderRadius: '20px' }}>
                                    <option value="all">Tất cả</option>
                                    <option value="EVENT">Sự kiện</option>
                                    <option value="DISCOUNT">Giảm giá</option>
                                    <option value="NEW">Món mới</option>
                                </select>
                                <button className="post-table-add-btn" onClick={() => setFormAddPostVisible(true)} >
                                    <i className="ti-plus"></i>
                                </button>
                            </div>

                            {isFormAddPostVisible && (
                                <div className="form-add-post-overlay">
                                    <FormAddPost userId={userId} onSubmit={handleOnSubmit} onClose={() => setFormAddPostVisible(false)} />
                                </div>
                            )}
                            {isUpdatePostOpen && (
                                <div className="overlay-update-post">
                                    <FormUpdatePost
                                        post={postToUpdate}  // Truyền toàn bộ đi tượng post
                                        postId={postToUpdate?.postId}  // Truyền riêng postId
                                        onClose={handleCloseUpdatePost}
                                        onSave={handleUpdatePost}

                                    />
                                </div>
                            )}

                            {selectedPostId && (
                                <div className="form-details-post-overlay">
                                    <FormDetailsPost postId={selectedPostId} onClose={() => setSelectedPostId(null)} />
                                </div>
                            )}

                            <table className='post-list-table'>
                                <thead>
                                    <tr>
                                        <th>STT</th>
                                        <th>Tiêu đề</th>
                                        <th>Danh mục</th>
                                        <th>Ảnh bài đăng</th>
                                        <th>Trạng thái</th>
                                        <th>Mã voucher</th>
                                        {/* <th>Giảm giá</th> */}
                                        {/* <th>Trạng thái</th> */}
                                        <th>Ngày bắt đầu</th>
                                        <th>Ngày kết thúc</th>
                                        <th></th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredPosts.length > 0 ? filteredPosts.map((post, index) => (
                                        <tr key={post.postId}>
                                            <td>{(currentPage - 1) * limit + index + 1}</td>
                                            <td>{post.title}</td>
                                            <td>{getPostTypeText(post.typePost)}</td>
                                            <td>
                                                {post.url ? <img src={post.url} alt="Post Banner" style={{ width: '100px', height: '100px' }} /> : 'No Image'}
                                            </td>
                                            <td>
                                                <label className="post-switch">
                                                    <input
                                                        type="checkbox"
                                                        checked={!post.isDeleted} // Checked if post is not deleted
                                                        onChange={() => handleSwitchChange(post.postId)} // Pass function reference
                                                    />
                                                    <span className="post-slider round"></span>
                                                </label>

                                            </td>

                                            {/* Display voucher information if available */}
                                            <td>{post.voucher ? post.voucher.key : ''}</td>
                                            {/* <td>{post.voucher && post.voucher.discount !== 0 ? post.voucher.discount : ''}</td> */}

                                            {/* <td>
                                                {post.voucher && (
                                                    post.voucher.key === '' &&
                                                    (post.voucher.discount === 0 || post.voucher.discount === '') &&
                                                    (post.voucher.startDate === '2024-11-12 07:01:29' && post.voucher.endDate === '2024-11-12 08:01:29')
                                                ) ? '' : post.voucher ? post.voucher.status : ''}
                                            </td> */}
                                            

                                            <td>
                                                {post.voucher && post.voucher.startDate === '2024-11-12 07:01:29' && post.voucher.endDate === '2024-11-12 08:01:29'
                                                    ? ''
                                                    : post.voucher
                                                        ? new Date(post.voucher.startDate).toLocaleDateString('vi-VN')
                                                        : ''
                                                }
                                            </td>
                                            <td>
                                                {post.voucher && post.voucher.startDate === '2024-11-12 07:01:29' && post.voucher.endDate === '2024-11-12 08:01:29'
                                                    ? ''
                                                    : post.voucher
                                                        ? new Date(post.voucher.endDate).toLocaleDateString('vi-VN')
                                                        : ''
                                                }
                                            </td>
                                            <td>
                                                <div className="admin-post-button-container">
                                                    <button id="admin-post-update-btn3" onClick={() => handleViewPostDetails(post.postId)}>
                                                        <i className="ti-info-alt" style={{ color: 'violet' }}></i>
                                                    </button>
                                                    <button
                                                        id="admin-post-update-btn1"
                                                        onClick={() => handleUpdatePostClick(post)}
                                                        disabled={post.isDeleted} // Disable the button if the post is deleted
                                                        style={{ cursor: post.isDeleted ? 'not-allowed' : 'pointer' }} // Show 'not-allowed' cursor if post is deleted
                                                    >
                                                        <i
                                                            className="ti-pencil"
                                                            style={{ color: post.isDeleted ? 'gray' : 'blue' }} // Change icon color if post is deleted
                                                        ></i>
                                                        {/* Icon for update */}
                                                    </button>



                                                </div>
                                            </td>
                                        </tr>

                                    )) : (
                                        <tr>
                                            <td colSpan="10" style={{ textAlign: 'center' }}>Không có kết quả tìm kiếm.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>

                            {/* Phân trang */}
                            <div className="pagination" style={{ position: 'absolute', left: '-100px' }}>
                                <button
                                    className="btn btn-pre me-2"
                                    onClick={() => handlePageChange(currentPage - 1)}
                                    disabled={currentPage === 1}
                                >
                                    &lt;
                                </button>
                                {getPaginationNumbers().map((number, index) => (
                                    <button
                                        key={index}
                                        className={`btn ${number === currentPage ? 'btn-page' : 'btn-light'} me-2`}
                                        onClick={() => {
                                            if (number !== '...') {
                                                handlePageChange(number);
                                            }
                                        }}
                                        disabled={number === '...'} // Disable button for ellipsis
                                    >
                                        {number}
                                    </button>
                                ))}
                                <button
                                    className="btn btn-next"
                                    onClick={() => handlePageChange(currentPage + 1)}
                                    disabled={currentPage === totalPages}
                                >
                                    &gt;
                                </button>
                            </div>
                        </div>
                    </>

                    <div className="user-voucher-table">
                        <div className="header-post-table">
                            <h2>Danh sách voucher ({totalV})</h2>
                            <Autocomplete
                                freeSolo
                                options={Array.isArray(allVouchers) ? allVouchers.map((voucher) => voucher.key) : []} // Assuming 'key' is the field to search
                                inputValue={voucherSearchTerm}
                                onInputChange={(event, newInputValue) => {
                                    setVoucherSearchTerm(newInputValue);
                                    handleVoucherSearch(newInputValue); // Call your search handler
                                }}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        placeholder="Tìm kiếm voucher..."
                                        variant="outlined"
                                        className="search-post-admin-input1"
                                    />
                                )}
                                style={{ width: '1000px', borderRadius: '20px', marginLeft: '900px' }} // Adjusted position to the left
                            />
                        </div>
                        <div className="table-wrapper">
                            {isUserVouchersLoading ? (
                                <div className="loading-message">Đang tải dữ liệu...</div>
                            ) : error ? (
                                <div className="error-message">{error}</div>
                            ) : (
                                <table className='post-list-table'>
                                    <thead>
                                        <tr>
                                            <th style={{ width: '8%' }}>STT</th>
                                            <th style={{ width: '15%' }}>Mã Voucher</th>
                                            <th style={{ width: '15%' }}>Giảm giá</th>
                                            <th style={{ width: '12%' }}>Số lượng</th>
                                            <th style={{ width: '15%' }}>Ngày bắt đầu</th>
                                            <th style={{ width: '15%' }}>Ngày kết thúc</th>
                                            <th style={{ width: '20%' }}>Trạng thái</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {allVouchers.length > 0 ? (
                                            allVouchers
                                                .filter(voucher =>
                                                    voucher.key?.toLowerCase().includes(voucherSearchTerm.toLowerCase()) ||
                                                    voucher.status?.toLowerCase().includes(voucherSearchTerm.toLowerCase()) ||
                                                    voucher.discount?.toString().includes(voucherSearchTerm)
                                                )
                                                .map((voucher, index) => (
                                                    <tr key={voucher.id || index}>
                                                        <td>{index + 1}</td>
                                                        <td>{voucher.key}</td>
                                                        <td>{voucher.discount?.toLocaleString()} VND</td>
                                                        <td>{voucher.number || 0}</td>
                                                        <td>{formatDate(voucher.startDate)}</td>
                                                        <td>{formatDate(voucher.endDate)}</td>
                                                        <td>{voucher.status === 'ACTIVE' ? 'Đang hoạt động' : 'Đã hết hạn'}</td>                                                        
                                                    </tr>
                                                ))
                                        ) : (
                                            <tr>
                                                <td colSpan="7" style={{ textAlign: 'center' }}>
                                                    Không có voucher nào.
                                                </td>
                                            </tr>
                                        )}
                                    </tbody>
                                </table>
                            )}
                        </div>
                    </div>

                </div>

            </div>
        </div>
    );

};

export default News;
