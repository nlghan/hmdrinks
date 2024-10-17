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
    const [limit, setLimit] = useState(10);
    const [totalPage, setTotalPage] = useState(1);
    const boxRef = useRef(null);

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

    const fetchUsers = async (page, limit) => {
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

            const response = await axios.get(`http://localhost:1010/api/admin/listUser?page=${page}&limit=${limit}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            console.log('Response data:', response.data);

            const data = response.data;
            const userData = data.detailUserResponseList;

            if (!userData || userData.length === 0) {
                throw new Error("Không có dữ liệu người dùng.");
            }

            setUsers(userData);
            setCurrentPage(data.currentPage);
            setTotalPage(data.totalPage);
            setLimit(data.limit);

            const initialSwitchStates = {};
            userData.forEach(user => {
                initialSwitchStates[user.userId] = user.isDelete === false;
            });
            setSwitches(initialSwitchStates);

            const adminCount = userData.filter(user => user.role === 'Admin').length;
            const customerCount = userData.filter(user => user.role === 'Customer').length;
            const shipperCount = userData.filter(user => user.role === 'Shipper').length;
            const totalUsers = userData.length;

            setAdminCount(adminCount);
            setCustomerCount(customerCount);
            setShipperCount(shipperCount);
            setTotalUsers(totalUsers);

        } catch (error) {
            console.error('Error fetching users:', error);
            setError("Không thể lấy thông tin người dùng.");
        }
    };



    useEffect(() => {
        fetchUsers(currentPage, limit);
    }, [currentPage]);

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

    const handleSwitchChange = (userId) => {
        const userToUpdate = users.find(user => user.userId === userId);

        const newIsDeletedStatus = !userToUpdate.isDelete;

        const updatedUsers = users.map((user) =>
            user.userId === userId ? { ...user, isDelete: newIsDeletedStatus } : user
        );

        setUsers(updatedUsers);

        const token = getCookie('access_token');
        if (token) {
            axios.put(`http://localhost:1010/api/admin/update-account`, {
                userId: userId,
                isDeleted: newIsDeletedStatus,
            }, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            }).then(response => {
                console.log("Cập nhật thành công:", response.data);
            }).catch(error => {
                console.error("Lỗi cập nhật:", error);
                setUsers(users);
            });
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

    const handleSearchChange = (event) => {
        const value = event.target.value;
        setSearchTerm(value);
    };
    const filteredUsers = users.filter(user =>
        user.userName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.fullName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase())
    );
    const adminPercentage = totalUsers ? ((adminCount / totalUsers) * 100).toFixed(0) : 0;
    const customerPercentage = totalUsers ? ((customerCount / totalUsers) * 100).toFixed(0) : 0;
    const shipperPercentage = totalUsers ? ((shipperCount / totalUsers) * 100).toFixed(0) : 0;

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
                            <input
                                type="text"
                                placeholder="Tìm kiếm người dùng..."
                                className="search-user-input"
                                onChange={handleSearchChange}
                                id="search-user"
                               
                            />
                            <h2>Danh Sách Người Dùng</h2>
                            <button className="add-user-btn" onClick={handleAddUserClick}>Thêm người dùng +</button>
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
                                {filteredUsers.length > 0 ? filteredUsers.map((user, index) => (
                                    <tr key={user.userId}>
                                        <td>{(currentPage - 1) * limit + index + 1}</td>
                                        <td>{user.userName}</td>
                                        <td>{user.fullName}</td>
                                        <td>{user.role}</td>
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
                                                <button id="user-update-btn3"  onClick={() => handleDetailsClick(user)}>
                                                    <i className="ti-info-alt"></i> {/* Themify icon for details */}
                                                </button>
                                                <button id="user-update-btn1"   onClick={() => handleUpdateClick(user)}>
                                                    <i className="ti-pencil"></i> {/* Themify icon for update */}
                                                </button>
                                                <button id="user-update-btn2" >
                                                    <i className="ti-trash"></i> {/* Themify icon for delete */}
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
                        <div className="user-pagination">
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

                    <div className="user-stat-box1">
                        <div className="user-percentage-circle">
                            <div className="user-inner-circle"></div>
                            <span>{adminPercentage}%</span>
                        </div>
                        <div className="user-stat-boxicon"></div>
                        <div className="user-stat-boxtext1">
                            <h3>ADMIN</h3>
                        </div>
                        <div className="user-stat-boxcount">
                            <h4>{adminCount}</h4>
                        </div>
                        <div className="user-stat-boxdetails">
                            <h5>Last 24 Hours</h5>
                        </div>
                    </div>
                    <div className="user-stat-box2">
                        <div className="user-percentage-circle2">
                            <div className="user-inner-circle2"></div>
                            <span>{customerPercentage}%</span>
                        </div>
                        <div className="user-stat-boxicon"></div>
                        <div className="user-stat-boxtext1">
                            <h3>CUSTOMER</h3>
                        </div>
                        <div className="user-stat-boxcount">
                            <h4>{customerCount}</h4>
                        </div>
                        <div className="user-stat-boxdetails">
                            <h5>Last 24 Hours</h5>
                        </div>
                    </div>
                    <div className="user-stat-box3">
                        <div className="user-percentage-circle3">
                            <div className="user-inner-circle3"></div>
                            <span>{shipperPercentage}%</span>
                        </div>
                        <div className="user-stat-boxicon"></div>
                        <div className="user-stat-boxtext1">
                            <h3>SHIPPER</h3>
                        </div>
                        <div className="user-stat-boxcount">
                            <h4>{shipperCount}</h4>
                        </div>
                        <div className="user-stat-boxdetails">
                            <h5>Last 24 Hours</h5>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default User;
