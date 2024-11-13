import React, { useState, useEffect } from 'react';
import './Post.css';
import axios from 'axios';
import Header from '../../components/Header/Header';
import FormAddPost from '../../components/Form/FormAddPost';
import FormDetailsPost from '../../components/Form/FormDetailsPost';
import FormUpdatePost from '../../components/Form/FormUpdatePost';

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
    const [limit, setLimit] = useState(4);
    const [currentPage, setCurrentPage] = useState(1); // Current page for posts
    const [totalPages, setTotalPages] = useState(1); // Total pages for posts
    const [postData, setPostData] = useState({});
    const [voucherSearchTerm, setVoucherSearchTerm] = useState('');
    const [allVouchers, setAllVouchers] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [voucher, setVoucher] = useState(null);
    const [post, setPost] = useState(null);

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

    const fetchPostVoucher = async (page, limit, type = 'all') => {
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
                // If type is 'all', fetch all posts
                url = `http://localhost:1010/api/admin/post/view/all?page=${page}&limit=${limit}`;
            } else {
                // If type is specific, fetch posts by type
                url = `http://localhost:1010/api/admin/post/view/type/all?page=${page}&limit=${limit}&type=${type}`;
            }

            // Fetch posts
            const responsePosts = await axios.get(url, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            const dataPosts = responsePosts.data;
            const fetchedPosts = dataPosts.listPosts || [];
            console.log("Fetched Posts:", fetchedPosts);

            if (fetchedPosts.length === 0) {
                setError(`Không có bài đăng thuộc loại ${type}`);
                setPosts([]); // Clear any existing posts
                return; // Return early since no posts were found
            }

            // Fetch all vouchers
            const responseVouchers = await axios.get('http://localhost:1010/api/voucher/view/all', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            const fetchedVouchers = responseVouchers.data.body.voucherResponseList || [];
            console.log("Fetched Vouchers:", fetchedVouchers);

            // Map posts to vouchers based on postId
            const postsWithVouchers = fetchedPosts.map(post => {
                const matchingVoucher = fetchedVouchers.find(voucher => voucher.postId === post.postId);
                return {
                    ...post,
                    voucher: matchingVoucher || null
                };
            });

            setPosts(postsWithVouchers);
            setTotalPages(dataPosts.totalPages);

        } catch (error) {
            if (error.response && error.response.status === 401) {
                setError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            } else {
                setError("Không thể lấy thông tin bài post hoặc voucher.");
            }
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

    const fetchAllUsers = async () => {
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
                const response = await axios.get(`http://localhost:1010/api/admin/listUser?page=${page}&limit=${limit}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
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

            setUsers(allUsers);

            const userIds = allUsers.map(user => user.userId);
            if (userIds.length > 0) {
                await Promise.all(userIds.map(userId => fetchUserVouchers(userId)));
            }

        } catch (error) {
            console.error("Error fetching all users:", error);
            setError("Không thể lấy danh sách người dùng.");
        }
    };

    const fetchUserVouchers = async (userId) => {
        try {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                return;
            }

            const response = await axios.get(`http://localhost:1010/api/admin/list-voucher/${userId}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.data?.getVoucherResponseList?.length === 0) {
                return;
            }

            setUserVouchers(prevVouchers => [
                ...prevVouchers,
                ...response.data.getVoucherResponseList
            ]);

        } catch (error) {
            console.error(`Error fetching vouchers for userId ${userId}:`, error);
            if (error.response) {
                console.error("Error response:", error.response);
            }
        }
    };

    useEffect(() => {
        fetchAllUsers();
    }, []);

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

    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value); // Cập nhật từ khóa tìm kiếm
    };

    // Filtering posts based on search term
    const filteredPosts = posts.filter((post) =>
        (post.title && post.title.toLowerCase().includes(searchTerm.toLowerCase())) ||
        (post.shortDescription && post.shortDescription.toLowerCase().includes(searchTerm.toLowerCase()))
    );

    const handleOnSubmit = () => {
        fetchPostVoucher(currentPage, limit);
    };
    const handleUpdatePost = async (updatedPost) => {
        console.log("Updating post with data:", updatedPost); // Log dữ liệu vào console

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
        fetchPostVoucher(currentPage, limit);
    };


    const handleViewPostDetails = async (postId) => {
        setSelectedPostId(postId);
        setIsLoading(true);
        try {
            const token = getCookie('access_token');
            // Fetch voucher details cho post được chọn
            const response = await axios.get(`http://localhost:1010/api/admin/list-voucher/${postId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.data && response.data.getVoucherResponseList) {
                // Lấy voucher đầu tiên từ danh sách (nếu có)
                const voucherData = response.data.getVoucherResponseList[0];
                setVoucher(voucherData);
            } else {
                setVoucher(null);
            }
        } catch (error) {
            console.error("Error fetching voucher details:", error);
            setError("Không thể tải thông tin voucher");
            setVoucher(null);
        } finally {
            setIsLoading(false);
        }
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

            // Send the request to enable or disable the post
            const response = await axios.put(
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

            const token = getCookie('access_token');
            try {
                const response = await axios.get(`http://localhost:1010/api/post/view/${selectedPostId}`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                setPost(response.data.body);

                const responseVouchers = await axios.get('http://localhost:1010/api/voucher/view/all', {
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
                setLoading(false);
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
                const response = await axios.get('http://localhost:1010/api/voucher/view/all', {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                
                if (response.data && response.data.body) {
                    const fetchedVouchers = response.data.body.voucherResponseList || [];
                    setAllVouchers(fetchedVouchers);
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

    return (

        <div className="post-container">
            <Header isMenuOpen={isMenuOpen} toggleMenu={() => setIsMenuOpen(!isMenuOpen)} title="Tiếp thị" />
            <div className="post-content">

                <div className="post-voucher-table" >

                    <>
                        <div className="post-table">
                            <div className="header-post-table">
                                <h2>Danh sách bài đăng</h2>
                                <input
                                    type="text"
                                    placeholder="Tìm kiếm bài đăng..."
                                    className="search-post-admin-input"
                                    id="search-user"
                                    value={searchTerm} // Gắn giá trị từ state
                                    onChange={handleSearchChange} // Gọi hàm xử lý khi thay đổi từ khóa tìm kiếm
                                />
                                <select value={selectedType} onChange={handleTypeChange} className="type-select" style={{ width: '11.5%', borderRadius: '20px' }}>
                                    <option value="all">Tất cả</option>
                                    <option value="EVENT">Sự kiện</option>
                                    <option value="DISCOUNT">Giảm giá</option>
                                    <option value="NEW">Món mới</option>
                                </select>
                                <button className="post-table-add-btn" onClick={() => setFormAddPostVisible(true)} >
                                    Thêm bài đăng và voucher +
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
                                        <th>Trạng thái</th>
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
                                            <td>{post.typePost}</td>
                                            <td>
                                                {post.url ? <img src={post.url} alt="Post Banner" style={{ width: '100px', height: '100px' }} /> : 'No Image'}
                                            </td>
                                            <td>
                                                <label className="cate-switch">
                                                    <input
                                                        type="checkbox"
                                                        checked={!post.isDeleted} // Checked if post is not deleted
                                                        onChange={() => handleSwitchChange(post.postId)} // Pass function reference
                                                    />
                                                    <span className="cate-slider round"></span>
                                                </label>


                                            </td>

                                            {/* Display voucher information if available */}
                                            <td>{post.voucher ? post.voucher.key : ''}</td>
                                            {/* <td>{post.voucher && post.voucher.discount !== 0 ? post.voucher.discount : ''}</td> */}

                                            <td>
                                                {/* Check if all voucher fields are empty and display empty string if so */}
                                                {post.voucher && (
                                                    post.voucher.key === '' &&
                                                    (post.voucher.discount === 0 || post.voucher.discount === '') &&
                                                    (post.voucher.startDate === '2024-11-12 07:01:29' && post.voucher.endDate === '2024-11-12 08:01:29')
                                                ) ? '' : post.voucher ? post.voucher.status : ''}
                                            </td>

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
                            <h2>Danh sách voucher</h2>
                            <input
                                type="text"
                                placeholder="Tìm kiếm voucher..."
                                className="search-post-admin-input"
                                value={voucherSearchTerm}
                                onChange={handleVoucherSearch}
                            />
                        </div>
                        <div className="table-wrapper">
                            {isLoading ? (
                                <div className="loading-message">Đang tải dữ liệu...</div>
                            ) : error ? (
                                <div className="error-message">{error}</div>
                            ) : (
                                <table className='post-list-table'>
                                    <thead>
                                        <tr>
                                            <th style={{width: '8%'}}>STT</th>
                                            <th style={{width: '15%'}}>Mã Voucher</th>
                                            <th style={{width: '15%'}}>Giảm giá</th>
                                            <th style={{width: '12%'}}>Số lượng</th>
                                            <th style={{width: '15%'}}>Ngày bắt đầu</th>
                                            <th style={{width: '15%'}}>Ngày kết thúc</th>
                                            <th style={{width: '20%'}}>Trạng thái</th>
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
                                                        <td>{voucher.status}</td>
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
