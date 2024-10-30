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
    const [limit, setLimit] = useState(1);
    const [currentPage, setCurrentPage] = useState(1); // Trang hiện tại
    const [totalPages, setTotalPages] = useState(1); // Tổng số trang
    const [postData, setPostData] = useState({});

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

    const fetchPostVoucher = async (page, limit) => {
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
    
            // Fetch posts
            const responsePosts = await axios.get(`http://localhost:1010/api/post/view/all?page=${page}&limit=${limit}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
    
            const dataPosts = responsePosts.data;
            const fetchedPosts = dataPosts.listPosts || [];
            console.log("Fetched Posts:", fetchedPosts);
    
            // Fetch all vouchers
            const responseVouchers = await axios.get('http://localhost:1010/api/voucher/view/all', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            console.log("Raw API response:", responseVouchers.data);
            const fetchedVouchers = responseVouchers.data.body.voucherResponseList || [];
            console.log("Fetched Vouchers:", fetchedVouchers);
            fetchedVouchers.forEach(voucher => console.log("Voucher data:", voucher));
    
            // Map posts to vouchers based on postId
            const postsWithVouchers = fetchedPosts.map(post => {
                const matchingVoucher = fetchedVouchers.find(voucher => {
                    const isMatching = voucher.postId === post.postId;
                    if (isMatching) {
                        console.log(`Matching Voucher Found: Post ID ${post.postId}, Voucher ID ${voucher.voucherId}`);
                    }
                    return isMatching;
                });
                return {
                    ...post,
                    voucher: matchingVoucher || null
                };
            });
    
            console.log("Posts with Vouchers:", postsWithVouchers);
    
            setPosts(postsWithVouchers);
            setTotalPages(dataPosts.totalPages);  // Fix: changed from setTotalPage to setTotalPages
        } catch (error) {
            if (error.response && error.response.status === 401) {
                setError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
            } else {
                setError("Không thể lấy thông tin bài post hoặc voucher.");
            }
            console.error('Error fetching posts or vouchers:', error);
        }
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
        if (newPage > 0 && newPage <= totalPage) {
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
    const totalPageVouchers = Math.ceil(vouchers.length / vouchersPerPage);

    const handlePageChangeVouchers = (newPage) => {
        if (newPage > 0 && newPage <= totalPageVouchers) {
            setCurrentPageVouchers(newPage);
        }
    };

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

        setPosts((prevPosts) =>
            prevPosts.map((post) =>
                post.postId === updatedPost.postId ? {
                    ...post,
                    ...updatedPost
                } : post
            )
        );

        setPostData(updatedPost);
        await fetchPostVoucher(currentPage, limit);
    };

    const handleViewPostDetails = (postId) => {
        setSelectedPostId(postId);
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



    const handleSearchChange = (event) => {
        setSearchTerm(event.target.value); // Cập nhật từ khóa tìm kiếm
    };
    const filteredPosts = posts.filter((post) =>
        post.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        post.shortDescription.toLowerCase().includes(searchTerm.toLowerCase())
    );
    // useEffect(() => {
    //     const pages = Math.ceil(filteredPosts.length / limit);
    //     setTotalPage(pages);
    //     if (currentPage > pages) {
    //         setCurrentPage(1); // Reset lại về trang đầu nếu currentPage vượt quá số trang hiện tại
    //     }
    // }, [filteredPosts, limit, currentPage]);

    // Logic hiển thị vouchers theo trang
    const indexOfLastVoucher = currentPageVouchers * vouchersPerPage;
    const indexOfFirstVoucher = indexOfLastVoucher - vouchersPerPage;
    const currentVouchers = vouchers.slice(indexOfFirstVoucher, indexOfLastVoucher);

    return (
        
        <div className="post-container">
           <Header isMenuOpen={isMenuOpen} toggleMenu={() => setIsMenuOpen(!isMenuOpen)} title="Tiếp thị" />
            <div className="post-content">

                <div className="post-voucher-table" >

                    <>
                        <div className="post-table">
                            <div className="header-post-table">
                                <input
                                    type="text"
                                    placeholder="Tìm kiếm bài đăng..."
                                    className="search-post-admin-input"
                                    id="search-user"
                                    value={searchTerm} // Gắn giá trị từ state
                                    onChange={handleSearchChange} // Gọi hàm xử lý khi thay đổi từ khóa tìm kiếm
                                />
                                <h2>Danh sách bài đăng</h2>
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
                                        post={postToUpdate}  // Truyền toàn bộ đối tượng post
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

                            <table>
                                <thead>
                                    <tr>
                                        <th>STT</th>
                                        <th>Tiêu đề</th>
                                        <th>Mô tả ngắn</th>
                                        <th>Ảnh bài đăng</th>
                                        <th>Mã voucher</th>
                                        <th>Giảm giá</th>
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
                                            <td>{post.shortDescription}</td>
                                            <td>
                                                {post.bannerUrl ? <img src={post.bannerUrl} alt="Post Banner" style={{ width: '70px', height: '70px' }} /> : 'No Image'}
                                            </td>

                                            {/* Display voucher information if available */}
                                            <td>{post.voucher ? post.voucher.key : ''}</td>
                                            <td>{post.voucher && post.voucher.discount !== 0 ? post.voucher.discount : ''}</td>

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
                                                        <i className="ti-info-alt"></i>
                                                    </button>
                                                    <button id="admin-post-update-btn1" onClick={() => handleUpdatePostClick(post)}>
                                                        <i className="ti-pencil"></i> {/* Icon cho cập nhật */}
                                                    </button>


                                                    <button id="admin-post-update-btn2">
                                                        <i className="ti-trash"></i>
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
                        <div className="pagination" style={{position:'absolute', left:'-100px'}}>
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

                </div>
                <div className="user-voucher-table">
                    <table>
                        <thead>
                            <tr>
                                <th>User Voucher ID</th>
                                <th>User ID</th>
                                <th>Voucher ID</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {userVouchers && userVouchers.length > 0 ? (
                                userVouchers.map((userVoucher) => (
                                    <tr key={userVoucher.id}>
                                        <td>{userVoucher.id}</td>
                                        <td>{userVoucher.userId}</td>
                                        <td>{userVoucher.voucherId}</td>
                                        <td>{userVoucher.status}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="4">Không có voucher nào của người dùng.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>

                </div>


            </div>
        </div>
    );

};

export default News;