import React, { useState, useEffect, useRef } from 'react';
import './User.css';
import Header from '../../components/Header/Header';
import { assets } from '../../assets/assets';
import Cookies from 'js-cookie';
import axios from 'axios';
import Menu from '../../components/Menu/Menu';
import FormAddUser from '../../components/Form/FormAddUser';
import FormDetailsUser from '../../components/Form/FormDetailsUser';
import FormUpdateUser from '../../components/Form/FormUpdateUser';
import GaugeCard from '../../components/Card/GaugeCardRes';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import debounce from 'lodash/debounce';


const User = () => {
    const [users, setUsers] = useState([]);
    const [switches, setSwitches] = useState({});
    const [error, setError] = useState("");

    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [isFormOpen, setIsFormOpen] = useState(false);

    const [selectedUser, setSelectedUser] = useState(null);
    const [isDetailsOpen, setIsDetailsOpen] = useState(false);

    const [isUpdateOpen, setIsUpdateOpen] = useState(false);
    const [userToUpdate, setUserToUpdate] = useState(null);

    const [searchTerm, setSearchTerm] = useState("");

    const [adminCount, setAdminCount] = useState(0);
    const [customerCount, setCustomerCount] = useState(0);
    const [shipperCount, setShipperCount] = useState(0);
    const [totalUsers, setTotalUsers] = useState(0);

    const [currentPage, setCurrentPage] = useState(1);
    const [limit, setLimit] = useState(7);
    const [totalPage, setTotalPage] = useState(1);
    const boxRef = useRef(null);
    const [selectedType, setSelectedType] = useState('all');

    const [pendingCount, setPendingCount] = useState(0);
    const [approvedCount, setApprovedCount] = useState(0);
    const [rejectedCount, setRejectedCount] = useState(0);

    const [adminPercentage, setAdminPercentage] = useState(0);
    const [customerPercentage, setCustomerPercentage] = useState(0);
    const [shipperPercentage, setShipperPercentage] = useState(0);
    const [total, setTotal] = useState(); // Tổng số trang

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
    const handleTypeChange = (event) => {
        const selectedRole = event.target.value;
        setSelectedType(selectedRole);  // Update selected role state
        fetchUsers(1, 7, selectedRole);  // Call fetchUsers with the selected role
    };


    const fetchUsers = async (page, limit, role = 'all', keyword = '') => {
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
    
            console.log('Keyword:', keyword);
            console.log('Page:', page);
            console.log('Limit:', limit);
    
            let url = '';
            if (keyword) {
                url = `http://localhost:1010/api/admin/search-user?keyword=${encodeURIComponent(keyword)}&page=${page}&limit=${limit}`;
            } else if (role === 'all') {
                url = `http://localhost:1010/api/admin/listUser?page=${page}&limit=${limit}`;
            } else {
                url = `http://localhost:1010/api/admin/listUser-role?page=${page}&limit=${limit}&role=${role}`;
            }
    
            console.log('Fetching URL:', url);
    
            const response = await axios.get(url, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
    
            console.log('Response data:', response.data);
    
            // Kiểm tra cấu trúc trả về
            const data = keyword ? response.data.body : response.data;
    
            const userData = data.detailUserResponseList || [];
            setUsers(userData); // Cập nhật danh sách người dùng
            console.log('Updated users:', userData);
    
            // Cập nhật các state khác
            setCurrentPage(data.currentPage || 1);
            setTotalPage(data.totalPage || 1);
            setLimit(data.limit || limit);
            setTotal(data.total || userData.length); // Tổng số người dùng
    
            // Xử lý trạng thái switch (isDelete)
            const initialSwitchStates = {};
            userData.forEach(user => {
                initialSwitchStates[user.userId] = user.isDelete === false;
            });
            setSwitches(initialSwitchStates);
    
        } catch (error) {
            console.error('Error fetching users:', error);
            setError("Không thể lấy thông tin người dùng.");
        }
    };
    


    const fetchTotalCounts = async () => {
        try {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để xem thông tin này.");
                console.log("No access token found.");
                return;
            }

            const firstPageUrl = `http://localhost:1010/api/admin/listUser?page=1&limit=${limit}`; // Lấy trang đầu tiên
            console.log("Fetching first page:", firstPageUrl);
            const firstPageResponse = await axios.get(firstPageUrl, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });

            const totalPages = firstPageResponse.data.totalPage; // Lấy tổng số trang
            console.log('Total Pages:', totalPages); // Kiểm tra tổng số trang
            let allUsers = [];

            // Lặp qua tất cả các trang để lấy dữ liệu
            for (let page = 1; page <= totalPages; page++) {
                const url = `http://localhost:1010/api/admin/listUser?page=${page}&limit=${limit}`;
                console.log(`Fetching page ${page}:`, url);
                const response = await axios.get(url, {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                    },
                });
                console.log(`Response for page ${page}:`, response.data); // Kiểm tra phản hồi cho từng trang
                allUsers = allUsers.concat(response.data.detailUserResponseList || []);
            }

            // Tính toán số lượng người dùng theo vai trò
            const adminCount = allUsers.filter(user => user.role === 'ADMIN').length;
            const customerCount = allUsers.filter(user => user.role === 'CUSTOMER').length;
            const shipperCount = allUsers.filter(user => user.role === 'SHIPPER').length;
            const totalUsers = allUsers.length;

            setAdminCount(adminCount);
            setCustomerCount(customerCount);
            setShipperCount(shipperCount);

            // Tính toán phần trăm
            const adminPercentage = totalUsers ? ((adminCount / totalUsers) * 100).toFixed(0) : 0;
            const customerPercentage = totalUsers ? ((customerCount / totalUsers) * 100).toFixed(0) : 0;
            const shipperPercentage = totalUsers ? ((shipperCount / totalUsers) * 100).toFixed(0) : 0;

            setAdminPercentage(adminPercentage);
            setCustomerPercentage(customerPercentage);
            setShipperPercentage(shipperPercentage);

        } catch (error) {
            console.error('Error fetching total counts:', error);
            setError("Không thể lấy thông tin tổng số lượng người dùng.");
            console.log("Error details:", error.response ? error.response.data : error.message);
        }
    };

    useEffect(() => {
        fetchTotalCounts(); // Gọi hàm fetchTotalCounts khi component được mount
    }, []);

    useEffect(() => {
        fetchUsers(currentPage, limit, selectedType, searchTerm.trim());
    }, [currentPage, limit, selectedType, searchTerm]);


    const handlePageChange = (newPage) => {
        console.log(`Attempting to change to page: ${newPage}`);
        if (newPage > 0 && newPage <= totalPage) {
            setCurrentPage(newPage);
        }
    };

    const handleAddUserClick = () => setIsFormOpen(true);
    const handleCloseForm = () => setIsFormOpen(false);

    const handleSubmitForm = (formData) => {
        setUsers(prevUsers => [...prevUsers, formData]);
    };

    const handleSwitchChange = async (userId) => {
        try {
            const token = getCookie('access_token');
            if (!token) {
                setError("Bạn cần đăng nhập để thực hiện thao tác này.");
                return;
            }

            // Find the user in the current users state
            const userToUpdate = users.find(user => user.userId === userId);
            if (!userToUpdate) {
                console.error("No user found with the given userId:", userId);
                return;
            }

            // Determine the correct API endpoint based on isDelete status
            const apiUrl = userToUpdate.isDelete
                ? 'http://localhost:1010/api/user/enable'
                : 'http://localhost:1010/api/user/disable';

            const newIsDeletedStatus = !userToUpdate.isDelete;

            // Send the request to enable or disable the user
            const response = await axios.put(
                apiUrl,
                { id: userId }, // Send the userId in the request body
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                }
            );

            // If the API call is successful, update the local state
            if (response.status === 200) {
                setUsers((prevUsers) =>
                    prevUsers.map((user) =>
                        user.userId === userId
                            ? { ...user, isDelete: newIsDeletedStatus }
                            : user
                    )
                );
                fetchUsers(currentPage, limit, 'all'); // Đảm bảo role là tham số phù hợp
                console.log(`User with ID ${userId} is now ${newIsDeletedStatus ? 'disabled' : 'enabled'}.`);
                // fetchUsers(currentPage,limit, role = 'all')
            } else {
                setError("Không thể thay đổi trạng thái người dùng. Vui lòng thử lại.");
            }
        } catch (error) {
            console.error("Error changing user status:", error);
            setError("Không thể thay đổi trạng thái người dùng. Vui lòng thử lại.");
        }
    };

    const getPaginationNumbers = () => {
        const paginationNumbers = [];
        const maxButtons = 5;

        if (totalPage <= maxButtons) {
            for (let i = 1; i <= totalPage; i++) {
                paginationNumbers.push(i);
            }
        } else {
            paginationNumbers.push(1);

            if (currentPage > 3) {
                paginationNumbers.push('...');
            }

            const startPage = Math.max(2, currentPage - 1);
            const endPage = Math.min(totalPage - 1, currentPage + 1);

            for (let i = startPage; i <= endPage; i++) {
                paginationNumbers.push(i);
            }

            if (currentPage < totalPage - 2) {
                paginationNumbers.push('...');
            }

            paginationNumbers.push(totalPage);
        }

        return paginationNumbers;
    };
    const handleDetailsClick = (user) => {
        setSelectedUser(user);
        setIsDetailsOpen(true);
    };

    const handleCloseDetails = () => {
        setIsDetailsOpen(false);
        setSelectedUser(null);
    };


    const handleUpdateClick = (user) => {
        setUserToUpdate(user);
        setIsUpdateOpen(true);
    };

    const handleUpdateUser = (updatedUser) => {
        setUsers((prevUsers) =>
            prevUsers.map((user) => (user.userId === updatedUser.userId ? updatedUser : user))
        );
    };

    const handleCloseUpdate = () => {
        setIsUpdateOpen(false);
        setUserToUpdate(null);
    };

    const handleSearchChange = debounce((value) => {
        fetchUsers(currentPage, limit, selectedType, value.trim());
    }, 500);

    const handleInputChange = debounce((event, newInputValue) => {
        setSearchTerm(newInputValue);
        const trimmedValue = newInputValue.trim();
        if (trimmedValue === '') {
            // Nếu input trống, gọi API lấy toàn bộ danh sách
            fetchUsers(currentPage, limit, selectedType);
        } else {
            fetchUsers(currentPage, limit, selectedType, trimmedValue);
        }
    }, 500);




    const filteredUsers = users.filter(user =>
        user.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const getRoleText = (role) => {
        const roleMap = {
            'ADMIN': 'Quản trị viên',
            'CUSTOMER': 'Khách hàng',
            'SHIPPER': 'Nhân viên'
        };
        
        return roleMap[role] || role;
    };

    return (
        <div className="user-table">
            <Header isMenuOpen={isMenuOpen} toggleMenu={() => setIsMenuOpen(!isMenuOpen)} title="Tài khoản" />
            {isFormOpen && <FormAddUser onClose={handleCloseForm} onSubmit={handleSubmitForm} />}

            {isDetailsOpen && selectedUser && (
                <div className="overlay-details-user">
                    <FormDetailsUser user={selectedUser} onClose={handleCloseDetails} />
                </div>
            )}
            {isUpdateOpen && (
                <div className="overlay-update-user">
                    <FormUpdateUser user={userToUpdate} onClose={handleCloseUpdate} onSave={handleUpdateUser} />
                </div>
            )}

            <div className={`user-table-row ${isMenuOpen ? 'user-dimmed' : ''}`} ref={boxRef}>
                <div className="user-main-section">
                    <div className="user-box">
                        <div className="header-user-box">
                            <h2>Danh Sách Người Dùng ({total})</h2>
                            <Autocomplete
                                freeSolo
                                options={Array.isArray(users) ? users.map((user) => user.userName) : []}
                                // Assuming userName is the field to search
                                inputValue={searchTerm}
                                onInputChange={handleInputChange}
                                renderInput={(params) => (
                                    <TextField
                                        {...params}
                                        label="Tìm kiếm người dùng..."
                                        variant="outlined"
                                        className="search-user-input"
                                    />
                                )}
                                style={{ width: '400px', borderRadius: '20px' }} // Adjust size as needed
                            />
                            <select value={selectedType} onChange={handleTypeChange} className="type-select" style={{ width: '400px', borderRadius: '20px' }}>
                                <option value="all">Tất cả</option>
                                <option value="ADMIN">Quản trị viên</option>
                                <option value="CUSTOMER">Khách hàng</option>
                                <option value="SHIPPER">Nhân viên</option>
                            </select>
                            <button className="add-user-btn" onClick={handleAddUserClick}>
                                <i className="ti-plus"></i>
                            </button>
                        </div>
                        {error && <div className="error-message">{error}</div>}
                        <table>
                            <thead>
                                <tr>
                                    <th>STT</th>
                                    <th>Tên đăng nhập</th>
                                    <th>Họ và tên</th>
                                    <th>Vai trò</th>
                                    <th>Trạng thái hoạt động</th>
                                    <th></th>
                                </tr>
                            </thead>
                            <tbody>
                                {users.length > 0 ? users.map((user, index) => (
                                    <tr key={user.userId}>
                                        <td>{(currentPage - 1) * limit + index + 1}</td>
                                        <td>{user.userName}</td>
                                        <td>{user.fullName}</td>
                                        <td>{getRoleText(user.role)}</td>
                                        <td>
                                            <label className="switch">
                                                <input
                                                    type="checkbox"
                                                    checked={user.isDelete === false}
                                                    onChange={() => handleSwitchChange(user.userId)}
                                                />
                                                <span className="slider round"></span>
                                            </label>
                                        </td>
                                        <td>
                                            <div className="user-button-container">
                                                <button id="user-update-btn3" onClick={() => handleDetailsClick(user)}>
                                                    <i className="ti-info-alt" style={{ color: 'violet' }}></i> {/* Themify icon for details */}
                                                </button>
                                                <button id="user-update-btn1" onClick={() => handleUpdateClick(user)}>
                                                    <i className="ti-pencil"></i> {/* Themify icon for update */}
                                                </button>

                                            </div>
                                        </td>

                                    </tr>
                                )) : (
                                    <tr>
                                        <td colSpan="6" style={{ textAlign: 'center' }}>Không có kết quả tìm kiếm.</td>
                                    </tr>
                                )}
                            </tbody>

                        </table>
                        <div className="user-admin-pagination">
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
                                    disabled={number === '...'}
                                >
                                    {number}
                                </button>
                            ))}
                            <button
                                className="btn btn-next"
                                onClick={() => handlePageChange(currentPage + 1)}
                                disabled={currentPage === totalPage}
                            >
                                &gt;
                            </button>
                        </div>
                    </div>
                </div>

                <div className="user-stats-section">
                    <GaugeCard
                        percentage={adminPercentage}
                        width='300px'
                        height='150px'
                        data="Quản lý"
                        number1={adminCount}
                        description="người dùng"
                        color='#ffffff'
                        backgroundColor='#FED8D7'
                    />
                    <GaugeCard
                        percentage={customerPercentage}
                        width='300px'
                        height='150px'
                        data="Khách"
                        number1={customerCount}
                        description="người dùng"
                        color='#ffffff'
                        backgroundColor='#F0D8BC'
                    />
                    <GaugeCard
                        percentage={shipperPercentage}
                        width='300px'
                        height='150px'
                        data="Nhân viên"
                        number1={shipperCount}
                        description="người dùng"
                        color='#ffffff'
                        backgroundColor='#CFE9E8'
                    />

                </div>
            </div>
        </div>
    );
};

export default User;
